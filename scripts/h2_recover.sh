#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/h2_recover.sh /path/to/dbfile.mv.db [--output-dir DIR]

Runs the H2 Recover tool against an .mv.db file and writes a recovery SQL
script into the output directory (defaults to the DB file's directory).

Example:
  scripts/h2_recover.sh /path/to/test7.mv.db
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" || "${1:-}" == "" ]]; then
  usage
  exit 0
fi

db_file="$1"
shift || true

output_dir=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --output-dir)
      output_dir="$2"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ ! -f "$db_file" ]]; then
  echo "Database file not found: $db_file" >&2
  exit 1
fi

if [[ "$db_file" != *.mv.db ]]; then
  echo "Expected an .mv.db file. Got: $db_file" >&2
  exit 1
fi

db_dir="$(cd "$(dirname "$db_file")" && pwd)"
db_base="$(basename "$db_file" .mv.db)"

if [[ -z "$output_dir" ]]; then
  output_dir="$db_dir"
else
  mkdir -p "$output_dir"
fi

h2_version="2.2.224"
h2_jar="${HOME}/.m2/repository/com/h2database/h2/${h2_version}/h2-${h2_version}.jar"

if [[ ! -f "$h2_jar" ]]; then
  echo "H2 jar not found at ${h2_jar}" >&2
  echo "Install dependencies with: mvn -q -DskipTests package" >&2
  exit 1
fi

echo "Recovering database:"
echo "  DB file:    ${db_file}"
echo "  DB dir:     ${db_dir}"
echo "  DB name:    ${db_base}"
echo "  Output dir: ${output_dir}"

java -cp "$h2_jar" org.h2.tools.Recover -dir "$output_dir" -db "$db_base"

echo "Recovery complete."
echo "Look for a file like: ${output_dir}/${db_base}.h2.sql"
