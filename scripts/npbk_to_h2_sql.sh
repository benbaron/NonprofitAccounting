#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <input.npbk> <output.sql>" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."

cd "${PROJECT_DIR}"

mvn -q \
  -Dexec.mainClass=nonprofitbookkeeping.tools.NpbkToH2ScriptMigrator \
  -Dexec.args="'$1' '$2'" \
  exec:java
