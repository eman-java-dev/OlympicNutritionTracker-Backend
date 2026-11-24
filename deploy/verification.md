# Verification record

Local or hosted candidate checks performed on 24 November 2026. See the draft PR checks for exact workflow revisions.

| Plan IDs | Procedure | Result and limits |
| --- | --- | --- |
| T01, T02, T03, T04, T05, T06, T07, T08, T19 | Java 21, Maven 3.9.11 and `mvn verify` with isolated H2 | PASS. 34 tests include password and Unicode boundaries, legacy login, duplicate registration, token integrity and rotation, scoped API access, throttle capacity and concurrency, and CORS. Effective Render client-source identity remains BLOCKED. |
| T09 | Pause a coach request before record access while the administrator commits reassignment | PASS on H2. The resumed request saw the new scope and left the entry unchanged. Simultaneous writes after access checks on matching production MySQL were NOT RUN. |
| T10, T11 | MySQL 8.0.43 on disposable loopback with `node --test deploy/tests/mysql-schema.test.cjs` | PASS for the fixture. Fresh schema, profile links applied once, repeat no-op, partial-schema rejection, case/accent collation and storage constraints. Production starting schema and settings are BLOCKED. |
| T16 | Packaged backend on loopback with the production profile and a disposable MySQL CA | PASS for local liveness and readiness with the matching hostname. Connector/J accepted the trusted matching host and rejected an untrusted CA and a mismatched hostname. Production TLS and health settings are BLOCKED. |
| T17 | Hosted checks and local redacted Gitleaks history scan | Backend build, candidate diff secret check with synthetic control, CodeQL and OSV ran in GitHub Actions. The first dependency-review job failed because GitHub's dependency graph was unavailable. OSV replaced that job. Generic Gitleaks patterns had no match in local history; targeted redacted review still recorded historical candidates. Active status is BLOCKED. |
| T18 | Logical export from a disposable fixture and restore to a separate schema | PASS for migration classification and all five table counts. Production backup, alert routing, restored workflow and rollback rehearsal are BLOCKED. |

No production credentials, database or deployment were used for these tests. The frontend public HTTPS HEAD request returned 200. A backend public health HEAD request timed out. Neither observation establishes a deployed revision.
