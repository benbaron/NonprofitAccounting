#!/bin/sh
# Validate all JRXML files under the project using xmllint
set -e
find src -name '*.jrxml' | while IFS= read -r file; do
    echo "Checking $file"
    xmllint --noout "$file"
done
