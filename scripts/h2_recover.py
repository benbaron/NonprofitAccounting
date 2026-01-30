#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
import subprocess
import sys
from pathlib import Path


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description=(
            "Run the H2 Recover tool against an .mv.db file and write a recovery SQL "
            "script into the output directory (defaults to the DB file's directory)."
        )
    )
    parser.add_argument(
        "db_file",
        type=Path,
        help="Path to the .mv.db file to recover.",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=None,
        help="Directory to write the recovery SQL output (defaults to DB directory).",
    )
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()

    db_file = args.db_file
    if not db_file.exists():
        print(f"Database file not found: {db_file}", file=sys.stderr)
        return 1
    if not db_file.name.endswith(".mv.db"):
        print(f"Expected an .mv.db file. Got: {db_file}", file=sys.stderr)
        return 1

    db_dir = db_file.parent.resolve()
    db_base = db_file.name.removesuffix(".mv.db")

    output_dir = args.output_dir.resolve() if args.output_dir else db_dir
    output_dir.mkdir(parents=True, exist_ok=True)

    h2_version = "2.2.224"
    h2_jar = (
        Path.home()
        / ".m2"
        / "repository"
        / "com"
        / "h2database"
        / "h2"
        / h2_version
        / f"h2-{h2_version}.jar"
    )

    if not h2_jar.exists():
        print(f"H2 jar not found at {h2_jar}", file=sys.stderr)
        print("Install dependencies with: mvn -q -DskipTests package", file=sys.stderr)
        return 1

    print("Recovering database:")
    print(f"  DB file:    {db_file}")
    print(f"  DB dir:     {db_dir}")
    print(f"  DB name:    {db_base}")
    print(f"  Output dir: {output_dir}")

    env = os.environ.copy()
    cmd = [
        "java",
        "-cp",
        str(h2_jar),
        "org.h2.tools.Recover",
        "-dir",
        str(output_dir),
        "-db",
        db_base,
    ]
    subprocess.run(cmd, check=True, env=env)

    print("Recovery complete.")
    print(f"Look for a file like: {output_dir}/{db_base}.h2.sql")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
