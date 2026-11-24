import json
import tempfile
import unittest
import zipfile
from pathlib import Path

from maven_inventory import CANARY, CANARY_ID, check_coverage, validate_database


class InventoryTest(unittest.TestCase):
    def setUp(self):
        self.tree = {"children": [
            {"groupId": "app", "artifactId": "runtime", "version": "1", "scope": "compile", "children": [
                {"groupId": "app", "artifactId": "helper", "version": "1", "scope": "provided"}
            ]},
            {"groupId": "app", "artifactId": "tests", "version": "2", "scope": "test", "classifier": "fixtures"},
        ]}
        self.bom = {"bomFormat": "CycloneDX", "components": [
            {"purl": "pkg:maven/app/runtime@1"}, {"purl": "pkg:maven/app/helper@1"},
            {"purl": "pkg:maven/app/tests@2?classifier=fixtures"},
        ]}
        self.report = {"results": [{"packages": [
            {"package": {"ecosystem": "Maven", "name": f"app:{name}", "version": version}}
            for name, version in [("runtime", "1"), ("helper", "1"), ("tests", "2")]
        ]}]}

    def test_complete_inventory(self):
        check_coverage(self.tree, self.bom, self.report)

    def test_missing_transitive_component(self):
        del self.bom["components"][1]
        with self.assertRaisesRegex(ValueError, "SBOM missing"):
            check_coverage(self.tree, self.bom, self.report)

    def test_missing_test_package(self):
        self.report["results"][0]["packages"].pop()
        with self.assertRaisesRegex(ValueError, "Scanner missing"):
            check_coverage(self.tree, self.bom, self.report)

    def test_wrong_classifier(self):
        self.bom["components"][2]["purl"] = "pkg:maven/app/tests@2"
        with self.assertRaisesRegex(ValueError, "SBOM missing"):
            check_coverage(self.tree, self.bom, self.report)

    def test_vulnerability_fails_after_coverage(self):
        self.report["results"][0]["packages"][0]["vulnerabilities"] = [{"id": "TEST-1"}]
        with self.assertRaisesRegex(ValueError, "package/advisory matches"):
            check_coverage(self.tree, self.bom, self.report)


class DatabaseTest(unittest.TestCase):
    def test_missing_and_corrupt_zip(self):
        with tempfile.TemporaryDirectory() as work:
            path = Path(work) / "all.zip"
            with self.assertRaises(OSError):
                validate_database(path)
            path.write_bytes(b"corrupt")
            with self.assertRaises(zipfile.BadZipFile):
                validate_database(path)

    def test_empty_malformed_and_unusable_advisories(self):
        canary = {"id": CANARY_ID, "modified": "2026-01-01T00:00:00Z", "affected": [
            {"package": {"ecosystem": "Maven", "name": ":".join(CANARY[:2])},
             "ranges": [{"type": "ECOSYSTEM", "events": [{"introduced": "0"}, {"fixed": "2.15.0"}]}]}
        ]}
        unusable = {**canary, "affected": []}
        for data in (None, "{", json.dumps(unusable), json.dumps(canary)):
            with self.subTest(data=data), tempfile.TemporaryDirectory() as work:
                path = Path(work) / "all.zip"
                with zipfile.ZipFile(path, "w") as archive:
                    if data is not None:
                        archive.writestr(CANARY_ID + ".json", data)
                if data == json.dumps(canary):
                    validate_database(path)
                else:
                    with self.assertRaises(ValueError):
                        validate_database(path)


if __name__ == "__main__":
    unittest.main()
