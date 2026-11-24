"""Validate the Maven inventory and offline OSV database used by CI."""

import argparse
import hashlib
import json
import re
import zipfile
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import unquote

SCOPES = {"compile", "provided", "runtime", "system", "test"}
CANARY = ("org.apache.logging.log4j", "log4j-core", "2.14.1")
CANARY_ID = "GHSA-jfh8-c2jp-5v3q"


def require(condition, message):
    if not condition:
        raise ValueError(message)


def valid_text(value):
    return isinstance(value, str) and bool(value.strip())


def valid_time(value):
    require(valid_text(value), "Missing advisory timestamp")
    timestamp = datetime.fromisoformat(value.replace("Z", "+00:00"))
    require(timestamp.tzinfo is not None, "Advisory timestamp has no timezone")


def validate_database(path):
    with zipfile.ZipFile(path) as archive:
        entries = [entry for entry in archive.infolist() if not entry.is_dir()]
        require(entries, "Empty Maven advisory database")
        names = set()
        has_canary = False
        for entry in entries:
            require(re.fullmatch(r"[A-Za-z0-9._-]+\.json", entry.filename), "Unexpected archive entry")
            require(entry.filename not in names, "Duplicate advisory archive entry")
            names.add(entry.filename)
            advisory = json.loads(archive.read(entry))
            identifier = advisory["id"]
            require(entry.filename == identifier + ".json", "Advisory ID does not match entry name")
            valid_time(advisory["modified"])
            affected = advisory.get("affected", [])
            require(isinstance(affected, list), "Invalid affected list")
            withdrawn = advisory.get("withdrawn")
            if withdrawn is not None:
                valid_time(withdrawn)
            usable = False
            for record in affected:
                package = record.get("package", {})
                if package.get("ecosystem") != "Maven":
                    continue
                name = package.get("name", "")
                require(isinstance(name, str) and re.fullmatch(r"[^:\s]+:[^:\s]+", name),
                        "Invalid Maven advisory package")
                versions = record.get("versions", [])
                require(isinstance(versions, list) and all(map(valid_text, versions)),
                        "Invalid affected Maven versions")
                ranges = record.get("ranges", [])
                require(isinstance(ranges, list), "Invalid affected Maven ranges")
                usable |= bool(versions)
                for version_range in ranges:
                    kind = version_range.get("type")
                    require(kind in {"ECOSYSTEM", "SEMVER", "GIT"}, "Unknown OSV range type")
                    events = version_range.get("events", [])
                    require(isinstance(events, list) and bool(events), "Missing OSV range events")
                    for event in events:
                        require(isinstance(event, dict) and len(event) == 1, "Invalid OSV range event")
                        key, value = next(iter(event.items()))
                        require(key in {"introduced", "fixed", "last_affected", "limit"} and valid_text(value),
                                "Invalid OSV range event")
                    usable |= kind != "GIT" and "introduced" in events[0]
                if identifier == CANARY_ID and not withdrawn and name == ":".join(CANARY[:2]):
                    has_canary |= usable
        require(has_canary, "Database does not contain the expected live Maven advisory")
    print(f"Validated {len(names)} advisory records")


def tree_packages(tree):
    dependencies = set()
    seen_scopes = set()

    def visit(node):
        for dependency in node.get("children", []):
            scope = dependency["scope"]
            require(scope in SCOPES, f"Unexpected Maven scope: {scope}")
            group, artifact, version = (dependency[key] for key in ("groupId", "artifactId", "version"))
            require(all(map(valid_text, (group, artifact, version))), "Incomplete Maven coordinate")
            classifier = dependency.get("classifier", "")
            require(isinstance(classifier, str), "Invalid Maven classifier")
            dependencies.add((group, artifact, version, classifier))
            seen_scopes.add(scope)
            visit(dependency)

    visit(tree)
    require(dependencies and {"compile", "test"} <= seen_scopes,
            "Resolved graph is empty or missing application or test dependencies")
    return dependencies


def sbom_packages(bom):
    require(bom.get("bomFormat") == "CycloneDX", "Expected a CycloneDX SBOM")
    packages = set()
    for component in bom["components"]:
        purl = component["purl"]
        match = re.fullmatch(r"pkg:maven/([^/]+)/([^/@]+)@([^?#]+)(?:\?([^#]+))?", purl)
        require(match is not None, f"Invalid Maven component PURL: {purl}")
        group, artifact, version = (unquote(part) for part in match.groups()[:3])
        qualifiers = dict(part.split("=", 1) for part in (match[4] or "").split("&") if part)
        packages.add((group, artifact, version, unquote(qualifiers.get("classifier", ""))))
    return packages


def scanner_packages(report):
    for result in report["results"]:
        for entry in result["packages"]:
            package = entry["package"]
            require(package["ecosystem"] == "Maven", "Unexpected scanner ecosystem")
            group, artifact = package["name"].split(":", 1)
            version = package["version"]
            require(all(map(valid_text, (group, artifact, version))), "Invalid scanner coordinate")
            yield (group, artifact, version), entry.get("vulnerabilities", [])


def check_coverage(tree, bom, report):
    resolved = tree_packages(tree)
    missing = resolved - sbom_packages(bom)
    require(not missing, f"SBOM missing resolved dependencies: {sorted(missing)}")
    scan = dict(scanner_packages(report))
    missing = {package[:3] for package in resolved} - scan.keys()
    require(not missing, f"Scanner missing resolved packages: {sorted(missing)}")
    print(f"Coverage verified for {len(resolved)} resolved artifacts across {len(scan)} scanner packages")
    findings = [(package, advisory["id"]) for package, advisories in scan.items() for advisory in advisories]
    for (group, artifact, version), advisory in sorted(findings):
        print(f"{group}:{artifact}:{version} {advisory}")
    require(not findings, f"Scan reported {len(findings)} package/advisory matches")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("operation", choices=["database", "coverage", "canary-bom", "canary-report"])
    parser.add_argument("files", nargs="+", type=Path)
    args = parser.parse_args()
    paths = args.files
    if args.operation == "database":
        archive, metadata = paths
        retrieved = datetime.now(timezone.utc).isoformat()
        with archive.open("rb") as stream:
            digest = hashlib.file_digest(stream, "sha256").hexdigest()
        validate_database(archive)
        evidence = {"retrieved_at_utc": retrieved, "sha256": digest}
        metadata.write_text(json.dumps(evidence) + "\n", encoding="utf-8")
        print(json.dumps(evidence))
    elif args.operation == "canary-bom":
        group, name, version = CANARY
        component = {"type": "library", "group": group, "name": name, "version": version,
                     "purl": f"pkg:maven/{group}/{name}@{version}"}
        bom = {"bomFormat": "CycloneDX", "specVersion": "1.5", "version": 1, "components": [component]}
        paths[0].write_text(json.dumps(bom), encoding="utf-8")
    elif args.operation == "canary-report":
        report = json.loads(paths[0].read_bytes())
        require(any(package == CANARY and any(item["id"] == CANARY_ID for item in advisories)
                    for package, advisories in scanner_packages(report)),
                "Offline scanner did not find the database control advisory")
        print("Offline scanner found the expected Maven control advisory")
    else:
        check_coverage(*(json.loads(path.read_bytes()) for path in paths))


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, TypeError, KeyError, AttributeError, zipfile.BadZipFile) as error:
        raise SystemExit(f"Dependency verification failed: {error}") from error
