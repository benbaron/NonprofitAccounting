#!/usr/bin/env python3
"""Run the H2 schema migrator via Maven.

Usage:
  ./scripts/migrate_h2_schema.py <db-path|db.mv.db> [output.sql]
"""

from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path


def _quote_for_exec_plugin(value: str) -> str:
    """Quote one argument for exec-maven-plugin's args parser."""
    escaped = value.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{escaped}"'


def _resolve_maven_command(project_dir: Path) -> list[str]:
    wrapper_candidates = ["mvnw", "mvnw.cmd", "mvnw.bat"]
    for wrapper in wrapper_candidates:
        wrapper_path = project_dir / wrapper
        if not wrapper_path.exists():
            continue

        if os.name == "nt":
            # On Windows, run wrappers through cmd to avoid CreateProcess issues
            # when launching .cmd/.bat files directly from Python.
            if wrapper_path.suffix.lower() in {".cmd", ".bat"}:
                return ["cmd", "/c", str(wrapper_path)]
            if wrapper_path.name == "mvnw":
                return ["cmd", "/c", str(wrapper_path)]

        if os.name != "nt" and os.access(wrapper_path, os.X_OK):
            return [str(wrapper_path)]

    mvn_path = shutil.which("mvn")
    if mvn_path:
        if os.name == "nt":
            return ["cmd", "/c", mvn_path]
        return [mvn_path]

    raise FileNotFoundError("Could not find 'mvnw'/'mvnw.cmd'/'mvn' in PATH")


def main(argv: list[str]) -> int:
    if len(argv) not in (1, 2):
        print(
            f"Usage: {Path(sys.argv[0]).name} <db-path|db.mv.db> [output.sql]",
            file=sys.stderr,
        )
        return 1

    script_path = Path(__file__).resolve()
    project_dir = script_path.parent.parent

    try:
        maven = _resolve_maven_command(project_dir)
    except FileNotFoundError as exc:
        print(str(exc), file=sys.stderr)
        return 127

    maven_args = " ".join(_quote_for_exec_plugin(arg) for arg in argv)
    command = [
        *maven,
        "-q",
        "-Dexec.mainClass=nonprofitbookkeeping.tools.H2SchemaMigrator",
        f"-Dexec.args={maven_args}",
        "exec:java",
    ]

    completed = subprocess.run(command, cwd=project_dir, check=False)
    return completed.returncode


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
