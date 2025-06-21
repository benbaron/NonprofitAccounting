# PowerShell script to vendor Maven dependencies offline
$ErrorActionPreference = 'Stop'

# Determine the vendor directory relative to this script
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$vendorDir = Join-Path $scriptDir '..\lib\m2'

if (-not (Test-Path $vendorDir)) {
    New-Item -ItemType Directory -Path $vendorDir | Out-Null
}

# Use Maven to download all dependencies and plugins into the vendored repository
mvn -Dmaven.repo.local=$vendorDir `
    --batch-mode --update-snapshots `
    dependency:go-offline

Write-Host "Vendored Maven repository created at $vendorDir"
