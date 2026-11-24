#!/usr/bin/env bash
set -euo pipefail

scratch=$(mktemp -d)
trap 'rm -rf -- "$scratch"' EXIT
scanner="$scratch/osv-scanner"
curl --fail --show-error --silent --location --retry 3 --max-time 120 --output "$scanner" \
  https://github.com/google/osv-scanner/releases/download/v2.7.1/osv-scanner_linux_amd64
printf '%s  %s\n' fb7dab19a8bd9bbe5c3189f09d4b7ee1820c9553a23bac202648a56efd2f6ee8 "$scanner" | sha256sum --check --status
chmod 0755 "$scanner"

export OSV_SCANNER_LOCAL_DB_CACHE_DIRECTORY="$scratch/db"
db="$OSV_SCANNER_LOCAL_DB_CACHE_DIRECTORY/osv-scalibr/Maven/all.zip"
mkdir -p "$(dirname "$db")"
curl --fail --show-error --silent --location --retry 3 --max-time 120 --output "$db" \
  https://osv-vulnerabilities.storage.googleapis.com/Maven/all.zip

validation=.github/scripts/maven_inventory.py
python3 "$validation" database "$db" target/maven-osv-evidence.json
python3 "$validation" canary-bom "$scratch/control.cdx.json"
touch "$scratch/osv-scanner.toml"
scan() {
  "$scanner" scan source --offline --no-resolve --all-packages --format=json \
    --config="$scratch/osv-scanner.toml" --lockfile="$1" --output-file="$2"
}

control_status=0
scan "$scratch/control.cdx.json" "$scratch/control.json" || control_status=$?
test "$control_status" -eq 1
python3 "$validation" canary-report "$scratch/control.json"

cp target/bom.json "$scratch/maven.cdx.json"
scan_status=0
scan "$scratch/maven.cdx.json" target/maven-osv-results.json || scan_status=$?
python3 "$validation" coverage target/dependency-tree.json target/bom.json target/maven-osv-results.json
test "$scan_status" -eq 0
