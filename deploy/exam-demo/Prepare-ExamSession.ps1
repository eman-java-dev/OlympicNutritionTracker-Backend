[CmdletBinding()]
param(
    [string]$JavaExe,
    [string]$CryptoJar
)

$ErrorActionPreference = 'Stop'
$backend = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
if (!$JavaExe) {
    if ($env:JAVA_HOME) {
        $JavaExe = Join-Path $env:JAVA_HOME 'bin/java.exe'
    } else {
        $installedJava = Get-Command java.exe -ErrorAction SilentlyContinue
        if ($installedJava) { $JavaExe = $installedJava.Source }
    }
}
if (!$JavaExe -or !(Test-Path -LiteralPath $JavaExe -PathType Leaf)) {
    throw 'Set JAVA_HOME to Java 21 or pass the full path using -JavaExe.'
}

if (!$CryptoJar) {
    $cryptoPath = 'org/springframework/security/spring-security-crypto/7.1.2/spring-security-crypto-7.1.2.jar'
    $candidates = @(
        (Join-Path $backend "target/backend-offline-maven-cache/$cryptoPath"),
        (Join-Path $env:USERPROFILE ".m2/repository/$cryptoPath")
    )
    $CryptoJar = $candidates | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } | Select-Object -First 1
}
if (!$CryptoJar -or !(Test-Path -LiteralPath $CryptoJar -PathType Leaf)) {
    throw 'Pass the existing spring-security-crypto-7.1.2.jar using -CryptoJar.'
}

function Read-PrivateValue([string]$Prompt) {
    $secure = Read-Host $Prompt -AsSecureString
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        $secure.Dispose()
    }
}

$outputDir = Join-Path $backend 'target/exam-demo'
$seed = Join-Path $outputDir '02-sample-data.sql'
$dbPassword = Read-PrivateValue 'Enter the password for the disposable MySQL account exam_backend'
if ([string]::IsNullOrEmpty($dbPassword)) {
    throw 'Enter the password for the disposable database account.'
}

if (!(Test-Path -LiteralPath $seed -PathType Leaf)) {
    $demoPassword = Read-PrivateValue 'Choose a new exam login password for the three synthetic accounts'
    if ($dbPassword -ceq $demoPassword) {
        throw 'Use a different password for the three demo logins.'
    }

    $previousEncoding = $OutputEncoding
    try {
        $OutputEncoding = [Text.UTF8Encoding]::new($false)
        $hash = $demoPassword | & $JavaExe -cp $CryptoJar (Join-Path $PSScriptRoot 'ExamPasswordHash.java')
        if ($LASTEXITCODE -ne 0 -or $hash -isnot [string] -or $hash -cnotmatch '^\$2[aby]\$\d{2}\$[./A-Za-z0-9]{53}$') {
            throw 'BCrypt generation failed.'
        }
    } finally {
        $OutputEncoding = $previousEncoding
        $demoPassword = $null
    }

    $null = New-Item -ItemType Directory -Path $outputDir -Force
    $template = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '02-sample-data.sql.template'))
    [IO.File]::WriteAllText($seed, $template.Replace('__EXAM_DEMO_BCRYPT_HASH__', $hash), [Text.UTF8Encoding]::new($false))
    "Rendered seed: $seed"
} else {
    "Using existing seed: $seed"
}

$key = [byte[]]::new(32)
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $rng.GetBytes($key)
    $env:JWT_SECRET = [Convert]::ToBase64String($key)
} finally {
    [Array]::Clear($key, 0, $key.Length)
    $rng.Dispose()
}

$env:JAVA_HOME = (Get-Item -LiteralPath $JavaExe).Directory.Parent.FullName
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3307/olympic_exam_demo?sslMode=REQUIRED'
$env:SPRING_DATASOURCE_HIKARI_JDBC_URL = $env:SPRING_DATASOURCE_URL
$env:SPRING_DATASOURCE_USERNAME = 'exam_backend'
$env:SPRING_DATASOURCE_PASSWORD = $dbPassword
$env:SPRING_PROFILES_ACTIVE = 'exam'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'validate'
$env:SPRING_SQL_INIT_MODE = 'never'
$env:SERVER_ADDRESS = '127.0.0.1'
$env:SERVER_PORT = '8080'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:4200,http://127.0.0.1:4200'
$env:SPRINGDOC_API_DOCS_ENABLED = 'false'
$env:SPRINGDOC_SWAGGER_UI_ENABLED = 'false'
'Demo environment variables are set for this PowerShell session. This script only prepares local files and configuration.'
