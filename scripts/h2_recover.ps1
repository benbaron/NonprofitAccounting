param(
    [Parameter(Mandatory=$true, Position=0)]
    [string]$DbFile,
    [string]$OutputDir = ""
)

function Show-Usage {
    Write-Host "Usage: scripts\h2_recover.ps1 -DbFile C:\path\to\dbfile.mv.db [-OutputDir C:\output]"
    Write-Host ""
    Write-Host "Runs the H2 Recover tool against an .mv.db file and writes a recovery SQL"
    Write-Host "script into the output directory (defaults to the DB file's directory)."
}

if ([string]::IsNullOrWhiteSpace($DbFile)) {
    Show-Usage
    exit 1
}

if (-not (Test-Path $DbFile)) {
    Write-Error "Database file not found: $DbFile"
    exit 1
}

if (-not $DbFile.EndsWith(".mv.db")) {
    Write-Error "Expected an .mv.db file. Got: $DbFile"
    exit 1
}

$dbDir = Split-Path -Path $DbFile -Parent
$dbBase = [System.IO.Path]::GetFileNameWithoutExtension($DbFile)

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = $dbDir
} else {
    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir | Out-Null
    }
}

$h2Version = "2.2.224"
$h2Jar = Join-Path -Path $HOME -ChildPath ".m2\repository\com\h2database\h2\$h2Version\h2-$h2Version.jar"

if (-not (Test-Path $h2Jar)) {
    Write-Error "H2 jar not found at $h2Jar"
    Write-Host "Install dependencies with: mvn -q -DskipTests package"
    exit 1
}

Write-Host "Recovering database:"
Write-Host "  DB file:    $DbFile"
Write-Host "  DB dir:     $dbDir"
Write-Host "  DB name:    $dbBase"
Write-Host "  Output dir: $OutputDir"

& java -cp $h2Jar org.h2.tools.Recover -dir $OutputDir -db $dbBase

Write-Host "Recovery complete."
Write-Host "Look for a file like: $OutputDir\$dbBase.h2.sql"
