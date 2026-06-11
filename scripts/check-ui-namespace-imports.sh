#!/usr/bin/env bash
set -euo pipefail

allowlist='src/main/java/org/nonprofitbookkeeping/ui/(MainWindow|MainWindowAlternate|FundsPanel|InventoryPanel|ReportLibraryPanel)\.java:'
violations=$(rg -n "^import nonprofitbookkeeping\.ui\.panels\." src/main/java/org/nonprofitbookkeeping/ui --glob '*.java' | rg -v "$allowlist" || true)
if [[ -n "$violations" ]]; then
  echo "Cross-namespace UI panel imports are forbidden in org.nonprofitbookkeeping.ui outside approved adapter/shell bridges:" >&2
  echo "$violations" >&2
  exit 1
fi

echo "UI namespace import guard passed."
