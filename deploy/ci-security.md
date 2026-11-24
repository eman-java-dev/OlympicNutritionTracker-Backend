# Backend CI security coverage

CI runs on pull requests and pushes to `master`. CodeQL traces a clean Maven `test-compile` using Java 21. A separate Java job runs `mvn verify`.

The resolved JSON tree has no scope filter. CycloneDX explicitly includes compile, provided, runtime, system and test scopes. The coverage gate matches each resolved Maven artifact to the SBOM and each resolved package version to the scanner output. OSV identifies Maven advisories by group, artifact and version, so classifiers sharing one version use the same advisory result. Resolution errors, missing inventory, scanner errors and advisory matches fail the job.

The scanner downloads the public Maven advisory ZIP separately and records its UTC retrieval time and SHA-256. It validates the advisory records and checks that the scanner finds a known advisory against a synthetic Maven package before scanning the project in offline mode. The project SBOM and resolved coordinates stay local to the scanner. Maven Central remains the source of build dependencies.

The inventory covers direct and transitive application and test dependencies in the resolved graph. Maven plug-in dependencies, CI tools, the JDK, OS and container packages, and inactive build profiles are outside this scan. Advisory coverage depends on the retrieved snapshot.

Gitleaks scans PR base-to-head and default-branch before-to-after ranges. An all-zero before SHA scans all history reachable from the new tip. Invalid SHAs and missing commits fail the scan. These checks are independent of credential revocation.

[Previous verification and open release gates](verification.md) · [Hosted checks by candidate head](https://github.com/eman-java-dev/OlympicNutritionTracker-Backend/pull/2/checks)
