#!/usr/bin/env bash

# validate_jrxml.sh - Validate JasperReports JRXML files
#
# Requires xmllint from the 'libxml2-utils' package on Debian/Ubuntu systems.
# Install it with:
#   sudo apt-get install libxml2-utils
#
# Usage:
#   ./scripts/validate_jrxml.sh [path ...]
#
# If no paths are provided, the script searches for *.jrxml files
# under src/main/resources by default.

set -euo pipefail

if ! command -v xmllint >/dev/null 2>&1; then
    echo "Error: xmllint not found. Please install 'libxml2-utils' to run JRXML validation." >&2
    exit 1
fi

if [ "$#" -eq 0 ]; then
    mapfile -t files < <(find src/main/resources -name '*.jrxml')
else
    files=("$@")
fi

ret=0
for f in "${files[@]}"; do
    echo "Validating $f"
    if ! xmllint --noout "$f"; then
        echo "Validation failed for $f" >&2
        ret=1
    fi
done

exit $ret
