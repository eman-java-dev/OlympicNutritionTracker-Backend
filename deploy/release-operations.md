# Release operations

## Verified source baseline

The 24 November 2026 review fetched backend `master` at `a43a5e4ecd88d187cdaf7644c73e258c7252dbef` and frontend `main` at `7cc77b18965ab95ac0e3a8d283802050dcb928dd`. Both match the execution plan. The implementation worktrees are based on these revisions. The older local backend `master` at `b5f99627bd0d57948100ef55b44b6742a5a28b0b` had existing work. Its configured origin identifies a different repository. Use the explicitly named `OlympicNutritionTracker-Backend` remote for review.

The remote backend branch `fix/render-railway-mysql-tls` points to `7380bcde0eae979d5ad2bc1cce1e5d9cd3aeaa34`. It has anonymous GET `/health` and `/healthz`, constructs the JDBC URL using `MYSQL_HOST`, `MYSQL_PORT` and `MYSQL_DATABASE`, and uses a private truststore with `VERIFY_CA`. This candidate restores the health aliases, uses `SPRING_DATASOURCE_URL` and retains `VERIFY_IDENTITY`. Before deployment, get Render's exact linked branches, live commits/deploy IDs, auto-deploy and preview settings, effective health path, active profile, environment variable names and environment-group associations. Obtain the Railway endpoint hostname and certificate SANs. Do not include secret values in evidence.

## Credential containment

A private, redacted local review recorded historical source candidates. Active status and revocation require provider evidence. Review deployments, environment groups, users/grants and build artifacts with their operators. Check whether the frontend demo identities correspond to provisioned accounts. Never probe production with a historical value.

1. The Railway administrator verifies schema-scoped DML grants and creates a new application identity with only SELECT, INSERT, UPDATE and DELETE on the application schema. Use a separate, temporary migration identity for reviewed DDL. Prepare a second current credential for recovery if a cutover fails after revocation.
2. The Render operator prepares the reviewed image and exact current environment-group associations. Supply the new database credential and a fresh Base64 JWT signing key of at least 32 random bytes through the secret store. Rehearse candidate startup, database hostname verification and application-role workflows on an isolated database. Verify that tokens signed by the previous synthetic key fail with the replacement key.
3. At the approved cutover, update the backend runtime and verify the expected commit, liveness, readiness, login and scoped access. Coordinate all running instances so obsolete JWT keys stop serving traffic. Expect users to sign in again.
4. Revoke the superseded DB login as soon as the new instances and role checks pass. Use operator-side grants and deployment evidence to record revocation. Verify no instance or configuration still references the prior values. Disable or reset actual provisioned demo accounts if their passwords were exposed.
5. Abort before cutover if ownership, TLS, target identity, grants or isolated checks are unclear. On failure after cutover, route to a reviewed compatible artifact with current secrets and current group associations. Never use the superseded JWT key or a revoked database account for recovery. Render's native rollback can reuse per-service settings and group associations from the target build. Verify these before using it. Record the recovered revision, settings and role smoke results. [Render rollbacks](https://render.com/docs/rollbacks)

## Database change gate

Use a read-only schema inventory for the production version, collation, SQL mode, tables, columns, indexes, FK definitions, grants and migration state. Compare against a disposable copy with equivalent MySQL settings. Confirm that every candidate and rollback artifact can operate on the post-migration schema. Do not run the exam fixture against production.

Run [the read-only V1 check](check-account-profile-links.sql) in the explicitly selected database. Only `V1_ABSENT` is a candidate for the reviewed one-statement [V1 migration](add-account-profile-links.sql), after the full schema has passed inventory and the backup has been restored in isolation. `V1_COMPLETE` is a no-op. `V1_PARTIAL_OR_DRIFTED` or `BASE_SCHEMA_REVIEW` stops the run. Capture `SHOW CREATE TABLE` privately and review a state-specific repair. Serialize migration execution using one operator during the approved change window. After DDL, rerun the read-only check and start the application with `ddl-auto=validate` and `spring.sql.init.mode=never`. The preflight classifies the profile-link objects and their prerequisites. It does not inventory unrelated schema drift.

For the local fixture, initialize a fresh MySQL 8 data directory named `target/mysql-sandbox-13317`, bind it only to `127.0.0.1:13317`, disable MySQL X and initialize with a disposable root account. Set `MYSQL_TEST_CLIENT` to the path of the official MySQL client. Run `node --test deploy/tests/mysql-schema.test.cjs` from the repo root. The script checks the disposable data-directory suffix before creating its fixture database. Stop the disposable server as soon as verification finishes.

## Monitoring, backups and release evidence

Render uses the configured health check in both routing and restart decisions. `/health` and `/healthz` are DB-independent. Evaluate `/readyz` separately against the desired routing and restart policy. Confirm effective proxy sanitization with staging traffic and Render configuration. The app's account throttle ignores forwarded headers. Gaps in source throttling and shared multi-instance enforcement remain open pending verified topology. [Render health checks](https://render.com/docs/health-checks)

Assign an operator for availability, elevated 5xx, authentication throttling and backup failures. Route notifications to the operator's approved on-call channel. Rehearse delivery using synthetic staging events. Confirm log retention. The application emits a generated request ID and samples HTTP 429 and 5xx events once per category per 30-second window. Use provider metrics for rates. The production profile suppresses Hibernate JDBC exception text, which may include values. Confirm backup encryption, access, retention and recovery time/data-loss objectives with the owner. Restore into an isolated equivalent target, compare inventory and representative records, rehearse the migration, then run role and data-integrity checks. Record the recovery duration and the backup's point in time. If provider volume backups cannot restore into a separate environment, rehearse an authorized logical export and restore. [Railway backups](https://docs.railway.com/volumes/backups)

| Identity | Minimum access for this work | Verification |
| --- | --- | --- |
| GitHub CI token | Contents read for tests. Security events write only for analysis upload, when used. | Committed workflow permissions. Account policy and default protections unverified. |
| Coding agent | Workspace reads and candidate-branch writes on the two repositories. | Isolated worktrees and managed sandbox. Effective GitHub installation scopes unverified. |
| Render operator | Read service/deploy settings for evidence. Deploy and secret administration separately approved. | Live account settings unverified. |
| Railway operator | Read inventory and backup metadata. Provisioning and restore separately approved. | Live account settings unverified. |
| MySQL runtime / migration | Schema-scoped DML / temporary reviewed DDL. | Disposable fixture validated separately. Production grants unverified. |

Before approval, record exact artifact hashes, target services, verified permissions, TLS results, ordered credential and DDL actions, expected interruption, stop criteria, recovery artifact and drill evidence. Production access and approval are pending.
