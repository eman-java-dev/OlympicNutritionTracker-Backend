# Deploy the backend on Render with Railway MySQL

## Prepare MySQL

1. Back up the existing MySQL database. For a new Railway database, restore a verified export containing the existing application schema and records. The application validates the schema at startup.
2. Before the first startup of this version, review and run [the account profile migration](add-account-profile-links.sql) once against the target database. It adds nullable links, unique constraints, and foreign keys. Existing accounts, profiles, and records remain in place. Use a separate schema-management account for the migration.
3. For each verified ATHLETE or COACH account, set the corresponding `users.athlete_profile_id` or `users.coach_profile_id` to the confirmed profile ID. Check the account's stored role and profile independently before linking them. Leave unverified accounts unlinked. ADMIN accounts need neither link. Confirm that existing ADMIN and COACH accounts were provisioned by a trusted operator before granting production access.
4. Rotate any values previously stored in source or Git history. Set a new signing key and replace any exposed MySQL credentials. Existing sessions must sign in again after the signing key changes.
5. In Railway, enable a public TCP proxy for MySQL and copy its external host and port. Configure MySQL TLS with a certificate that matches the hostname used by Render and a CA trusted by the Java runtime. For a private CA, provision a truststore and supply it to Connector/J through its truststore settings. The production profile uses `VERIFY_IDENTITY`. [Railway MySQL](https://docs.railway.com/databases/mysql) · [Connector/J server authentication](https://dev.mysql.com/doc/connector-j/en/connector-j-server-authentication.html)

## Configure Render

Create a Docker web service rooted at this backend directory, using the included Dockerfile. It builds and tests with Java 21 and runs the packaged JAR as a non-root user. Render supplies `PORT`, and Spring binds to it on `0.0.0.0`. Render's default TCP health check can be used. [Render web services](https://render.com/docs/web-services) · [Render health checks](https://render.com/docs/health-checks)

| Environment variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod`, already set by the Dockerfile |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://PUBLIC_TCP_HOST:PUBLIC_TCP_PORT/DATABASE` using the verified external hostname and port |
| `SPRING_DATASOURCE_USERNAME` | Dedicated MySQL application account with data access to the application schema |
| `SPRING_DATASOURCE_PASSWORD` | Password for that account, entered as a runtime secret |
| `JWT_SECRET` | Base64 encoding of at least 32 cryptographically random bytes, entered as a runtime secret |
| `JWT_EXPIRATION_MS` | Optional token lifetime in milliseconds, defaults to 24 hours |
| `CORS_ALLOWED_ORIGINS` | Exact Angular HTTPS origin, or comma-separated origins, with scheme and optional port |

Keep credentials out of the JDBC URL, Docker build arguments, and build logs. Production disables Swagger and the database diagnostic route. [Render Docker environment variables](https://render.com/docs/docker#environment-variable-translation)

Before opening production access, verify startup against a staging copy of the Railway database. Check TLS verification, a login with each role, record access between two different profiles, and the CORS preflight from the Angular origin. Login and registration return `token`, `username`, and `role` using the existing JSON field names.
