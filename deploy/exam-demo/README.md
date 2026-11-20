# Local exam demo for Spring Boot 4.1.2

Use a separate MySQL 8 instance on `127.0.0.1:3307` with a new data directory and the schema `olympic_exam_demo`. The SQL creates the five application tables directly, including both profile links. It needs no source backup, migration, or connection to your existing MySQL instance.

## 1. Approve the disposable instance before provisioning

Confirm that port `3307` is free. From the backend root in PowerShell, initialize a fresh data directory and start the new MySQL process after approval. Leave this window open. The original MySQL service keeps running independently. [MySQL data directory initialization](https://dev.mysql.com/doc/refman/8.0/en/data-directory-initialization.html)

```powershell
$mysqlHome = 'C:/Program Files/MySQL/MySQL Server 8.0'
$demoRoot = Join-Path (Get-Location) 'target/exam-mysql'
if (Test-Path -LiteralPath $demoRoot) { throw 'Choose a new empty exam instance directory.' }
$null = New-Item -ItemType Directory -Path $demoRoot
$data = Join-Path $demoRoot 'data'
$initLog = Join-Path $demoRoot 'initialize.err'
$serverLog = Join-Path $demoRoot 'server.err'

& "$mysqlHome/bin/mysqld.exe" --no-defaults --initialize "--basedir=$mysqlHome" "--datadir=$data" "--log-error=$initLog"
if ($LASTEXITCODE -ne 0) { throw 'Exam MySQL initialization failed. Check the private initialization log.' }
& "$mysqlHome/bin/mysqld.exe" --no-defaults "--basedir=$mysqlHome" "--datadir=$data" "--log-error=$serverLog" --bind-address=127.0.0.1 --port=3307 --mysqlx=OFF
```

MySQL writes an expired temporary root password to `target/exam-mysql/initialize.err`. Open that file privately, away from the recording. Keep the data directory and logs local.

## 2. Approve the schema and seed before running SQL

In a second PowerShell window at the backend root, open the **disposable** MySQL client session. The explicit host, port, and username take precedence over saved login settings. [MySQL client options](https://dev.mysql.com/doc/refman/8.0/en/mysql-command-options.html)

```powershell
$mysql = 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
& $mysql --no-defaults --protocol=TCP --host=127.0.0.1 --port=3307 --ssl-mode=REQUIRED --user=root --password --connect-expired-password
```

Enter the temporary password at the prompt. Privately run `ALTER USER USER() IDENTIFIED BY '<new scratch root password>';` with a new value. Run `SELECT @@port, @@datadir;` and continue only if it reports `3307` and the new `target/exam-mysql/data` directory. Run `SOURCE deploy/exam-demo/01-schema.sql` once in this session. Confirm it finishes without errors.

In the same MySQL client, create the application account using a separate, new password. Replace the placeholder privately. The account has data access only within the exam schema.

```sql
CREATE USER 'exam_backend'@'127.0.0.1' IDENTIFIED BY '<new scratch application password>' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE, DELETE ON olympic_exam_demo.* TO 'exam_backend'@'127.0.0.1';
```

Exit the MySQL client. Run the preparation script in the second PowerShell window using Java 21. Enter the scratch application database password first, then choose a separate exam login password when prompted. The script renders a seed with BCrypt hashes under `target/exam-demo`. It generates a fresh JWT key and configures only this PowerShell session. Existing rendered seeds are reused when opening a new session.

```powershell
.\deploy\exam-demo\Prepare-ExamSession.ps1 -JavaExe 'C:/Users/emey/.jdks/ms-21.0.7/bin/java.exe'
```

Reconnect to the disposable MySQL instance using the same explicit client command, omitting `--connect-expired-password` and using the new root password. Verify the port and data directory again, then run `SOURCE target/exam-demo/02-sample-data.sql` once. Exit the client after the import.

## 3. Approve starting the current backend

In the PowerShell window configured in step 2, confirm the JDBC URL is exactly `jdbc:mysql://127.0.0.1:3307/olympic_exam_demo?sslMode=REQUIRED`. Keep schema validation enabled. Start the **current** backend on `127.0.0.1:8080` using Java 21 and your Maven runner from that window. Use the Maven executable on PATH or the existing Maven wrapper cache. The Spring arguments pin the JDBC URL, loopback binding, and schema validation. If Maven is blocked, pause before switching to an IDE run configuration so the demo settings can be checked there too.

```powershell
$examJdbc = 'jdbc:mysql://127.0.0.1:3307/olympic_exam_demo?sslMode=REQUIRED'
if ($env:SPRING_DATASOURCE_URL -cne $examJdbc -or !$env:JWT_SECRET -or !$env:SPRING_DATASOURCE_PASSWORD) {
    throw 'Run the exam preparation script in this PowerShell window first.'
}
$examArgs = "--spring.config.location=classpath:/application.properties --spring.datasource.url=$examJdbc --spring.datasource.hikari.jdbc-url=$examJdbc --spring.jpa.hibernate.ddl-auto=validate --spring.sql.init.mode=never --server.address=127.0.0.1 --server.port=8080"
$maven = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
if (!$maven) {
    $cachedMaven = Join-Path $env:USERPROFILE '.m2/wrapper/dists/apache-maven-3.9.11-bin'
    $maven = (Get-ChildItem -LiteralPath $cachedMaven -Recurse -File -Filter mvn.cmd -ErrorAction SilentlyContinue |
        Select-Object -First 1).FullName
}
if (!$maven) { throw 'Select a Java 21 Maven installation before starting the exam backend.' }
& $maven spring-boot:run "-Dspring-boot.run.arguments=$examArgs"
```

With local Angular on port `4200`, sign in with the demo login password entered in step 2. Each role has a synthetic account.

| Login | What to check |
| --- | --- |
| `exam_admin` | Both athlete profiles, coaches, consultations, and nutrition entries |
| `exam_coach` | Assigned athlete Avery Sprint and that athlete's nutrition entries and consultations |
| `exam_athlete` | Avery Sprint's linked profile, nutrition entries, and consultations |

Use the application's logout between roles. After recording, stop the demo processes and close the configured PowerShell window to discard its environment secrets.
