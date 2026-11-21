# Deploy the backend on Render with Railway MySQL

## Prepare MySQL

1. Back up the existing MySQL database. For a new Railway database, restore a verified export containing the existing application schema and records. The application validates the schema at startup.
2. Compare the target schema against the application on a staging copy. If both `users.athlete_profile_id` and `users.coach_profile_id` and their constraints are missing from an older schema, review and apply [the account profile migration](add-account-profile-links.sql) once with a separate schema-management account. Skip it when already applied. If only some of the objects exist, review the differences before applying any DDL. A clean Railway MySQL instance needs a full application schema first. Hibernate uses `ddl-auto=validate`, Spring SQL initialization is disabled, and there is no automatic migration runner.
3. For each verified ATHLETE or COACH account, set the corresponding `users.athlete_profile_id` or `users.coach_profile_id` to the confirmed profile ID. Check the account's stored role and profile independently before linking them. Leave unverified accounts unlinked. ADMIN accounts need neither link. Confirm that existing ADMIN and COACH accounts were provisioned by a trusted operator before granting production access.
4. Rotate any values previously stored in source or Git history. Set a new signing key and replace any exposed MySQL credentials. Existing sessions must sign in again after the signing key changes.
5. Use the existing Railway public TCP proxy for MySQL's internal port (normally `3306`). Use the public proxy hostname and assigned public port from Render. MySQL's automatically generated certificate does not identify the public proxy hostname, so production temporarily uses `VERIFY_CA`. Connector/J requires TLS and accepts server certificates that chain to the dedicated CA regardless of hostname. Restore `VERIFY_IDENTITY` when MySQL presents a trusted certificate with the JDBC hostname in its DNS SAN. [Railway TCP Proxy](https://docs.railway.com/networking/tcp-proxy) · [Connector/J TLS modes](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html)

### Private CA truststore

Obtain the MySQL CA certificate through a trusted operator channel and verify its fingerprint before importing it. Prepare a PKCS12 truststore containing the CA certificate and no private keys. The commands below are for a POSIX shell. The first command prompts for the truststore password.

```sh
keytool -importcert -alias railway-mysql-ca -file railway-mysql-ca.pem \
  -storetype PKCS12 -keystore railway-mysql-ca.p12
base64 < railway-mysql-ca.p12 | tr -d '\r\n'
```

Paste the single-line result into Render's `MYSQL_TLS_TRUST_STORE_BASE64`. Supply the same password through `MYSQL_TLS_TRUST_STORE_PASSWORD`. At runtime the container decodes the truststore into a private temporary file. Connector/J requires the dedicated truststore and system truststore fallback is disabled. If Railway rotates or regenerates the CA, verify the replacement independently before updating the truststore. [Connector/J server authentication](https://dev.mysql.com/doc/connector-j/en/connector-j-server-authentication.html)

## Configure Render

Create a Docker web service rooted at this backend directory, using the included Dockerfile. It builds and tests with Java 21 and runs the packaged JAR as a non-root user. Render supplies `PORT`, and Spring binds to it on `0.0.0.0`. Render's default TCP health check can be used. [Render web services](https://render.com/docs/web-services) · [Render health checks](https://render.com/docs/health-checks)

| Environment variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod`, already set by the Dockerfile |
| `MYSQL_HOST` | Railway's public TCP proxy hostname |
| `MYSQL_PORT` | Railway's assigned **public TCP proxy port** |
| `MYSQL_DATABASE` | Name of the migrated application schema |
| `MYSQL_TLS_TRUST_STORE_BASE64` | Single-line base64 of the dedicated PKCS12 CA truststore |
| `MYSQL_TLS_TRUST_STORE_PASSWORD` | Password of that truststore |
| `SPRING_DATASOURCE_USERNAME` | Dedicated MySQL application account with data access to the application schema |
| `SPRING_DATASOURCE_PASSWORD` | Password for that account, entered as a runtime secret |
| `JWT_SECRET` | Base64 encoding of at least 32 cryptographically random bytes, entered as a runtime secret |
| `JWT_EXPIRATION_MS` | Optional token lifetime in milliseconds, defaults to 24 hours |
| `CORS_ALLOWED_ORIGINS` | Exact Angular HTTPS origin, or comma-separated origins, with scheme and optional port |

Remove any old `SPRING_DATASOURCE_URL` or `SPRING_DATASOURCE_HIKARI_JDBC_URL` from the Render service and its linked environment groups so the production profile builds the JDBC URL from `MYSQL_HOST`, `MYSQL_PORT`, and `MYSQL_DATABASE`. Keep credentials out of the JDBC URL, the Dockerfile, and logs. Production disables Swagger and the database diagnostic route. Leave **Health Check Path** blank for the default TCP probe. [Render Docker environment variables](https://render.com/docs/docker-secrets) · [Render health checks](https://render.com/docs/health-checks)

Before opening production access, verify startup against a staging copy of the Railway database. For an independent, read-only TLS check with a private CA, run:

```sh
mysql --host=PUBLIC_TCP_HOST --port=PUBLIC_TCP_PORT --user=APP_USER --password \
  --ssl-mode=VERIFY_CA --ssl-ca=railway-mysql-ca.pem --database=APP_DATABASE \
  --execute="SELECT 1; SHOW SESSION STATUS LIKE 'Ssl_cipher';"
```

After deploying on Render, verify that the logs show a successful database pool connection and completed Spring Boot startup, including Hibernate schema validation. Check a login with each role, record access between two different profiles, and the CORS preflight from the Angular origin. Login and registration return `token`, `username`, and `role` using the existing JSON field names.
