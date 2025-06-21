#!/usr/bin/env bash
set -euo pipefail

# Directory to place the vendored Maven repository
VENDOR_DIR="$(dirname "$0")/../lib/m2"

# Create vendor directory
mkdir -p "$VENDOR_DIR"

# Use Maven to download all dependencies and plugins specified in pom.xml
# into the vendored repository directory.

mvn -Dmaven.repo.local="$VENDOR_DIR" \
    --batch-mode --update-snapshots \
    dependency:go-offline

echo "Vendored Maven repository created at $VENDOR_DIR"
