[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$DbFile,

    [string]$H2Jar,

    [ValidateSet('forward', 'validate', 'rollback', 'all')]
    [string]$Mode = 'all',

    [string]$User = 'sa',
    [string]$Password = ''
)

$ErrorActionPreference = 'Stop'

function Resolve-H2Jar {
    param([string]$ExplicitPath)

    if ($ExplicitPath) {
        if (-not (Test-Path -LiteralPath $ExplicitPath)) {
            throw "H2 jar not found at: $ExplicitPath"
        }
        return (Resolve-Path -LiteralPath $ExplicitPath).Path
    }

    $home = $env:USERPROFILE
    if (-not $home) { $home = $env:HOME }
    if (-not $home) {
        throw "Could not resolve USERPROFILE/HOME. Pass -H2Jar explicitly."
    }

    $glob = Join-Path $home ".m2\repository\com\h2database\h2\*\h2-*.jar"
    $match = Get-ChildItem -Path $glob -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $match) {
        throw "Could not find H2 jar in ~/.m2. Pass -H2Jar explicitly."
    }

    return $match.FullName
}

function Invoke-H2Script {
    param(
        [string]$Jar,
        [string]$JdbcUrl,
        [string]$ScriptPath,
        [string]$DbUser,
        [string]$DbPassword
    )

    if (-not (Test-Path -LiteralPath $ScriptPath)) {
        throw "SQL script not found: $ScriptPath"
    }

    Write-Host "Running: $ScriptPath" -ForegroundColor Cyan

    & java -cp $Jar org.h2.tools.RunScript `
        -url $JdbcUrl `
        -user $DbUser `
        -password $DbPassword `
        -script $ScriptPath

    if ($LASTEXITCODE -ne 0) {
        throw "RunScript failed for $ScriptPath with exit code $LASTEXITCODE"
    }
}

$h2JarPath = Resolve-H2Jar -ExplicitPath $H2Jar
$dbAbs = (Resolve-Path -LiteralPath $DbFile).Path
$jdbcUrl = "jdbc:h2:file:$dbAbs;AUTO_SERVER=TRUE;MODE=MySQL"

Write-Host "H2 jar: $h2JarPath"
Write-Host "JDBC URL: $jdbcUrl"
Write-Host "Mode: $Mode"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$forward = Join-Path $scriptDir 'txn_convergence_forward.sql'
$validate = Join-Path $scriptDir 'txn_convergence_validation.sql'
$rollback = Join-Path $scriptDir 'txn_convergence_rollback.sql'

switch ($Mode) {
    'forward' {
        Invoke-H2Script -Jar $h2JarPath -JdbcUrl $jdbcUrl -ScriptPath $forward -DbUser $User -DbPassword $Password
    }
    'validate' {
        Invoke-H2Script -Jar $h2JarPath -JdbcUrl $jdbcUrl -ScriptPath $validate -DbUser $User -DbPassword $Password
    }
    'rollback' {
        Invoke-H2Script -Jar $h2JarPath -JdbcUrl $jdbcUrl -ScriptPath $rollback -DbUser $User -DbPassword $Password
    }
    'all' {
        Invoke-H2Script -Jar $h2JarPath -JdbcUrl $jdbcUrl -ScriptPath $forward -DbUser $User -DbPassword $Password
        Invoke-H2Script -Jar $h2JarPath -JdbcUrl $jdbcUrl -ScriptPath $validate -DbUser $User -DbPassword $Password
    }
}

Write-Host "Done." -ForegroundColor Green
