#!/usr/bin/env python3
"""Run the H2 schema migrator.

Usage:
  ./scripts/migrate_h2_schema.py <db-path|db.mv.db> [output.sql]

Execution order:
  1) Prefer direct Java launch from an existing built JAR in ./target
  2) Fallback to Maven/maven-wrapper execution
"""

from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path

MIGRATOR_CLASS = "nonprofitbookkeeping.tools.H2SchemaMigrator"


def _quote_for_exec_plugin(value: str) -> str:
    escaped = value.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{escaped}"'


def _windows_cmd() -> str:
    comspec = os.environ.get("ComSpec") or os.environ.get("COMSPEC")
    if comspec:
        return comspec
    return shutil.which("cmd") or "cmd"


def _find_launchable_jar(project_dir: Path) -> Path | None:
    target_dir = project_dir / "target"
    if not target_dir.exists():
        return None

    # Prefer a shaded/fat jar when available.
    preferred = sorted(target_dir.glob("*-shaded.jar"))
    if preferred:
        return preferred[-1]

    # Fallback to the standard project jar.
    candidates = sorted(target_dir.glob("*.jar"))
    for candidate in reversed(candidates):
        name = candidate.name
        if name.endswith("-sources.jar") or name.endswith("-javadoc.jar") or name.startswith("original-"):
            continue
        return candidate

    return None


def _resolve_java_command(project_dir: Path, argv: list[str]) -> list[str] | None:
    java_path = shutil.which("java")
    if not java_path:
        return None

    jar_path = _find_launchable_jar(project_dir)
    if not jar_path:
        return None

    return [java_path, "-cp", str(jar_path), MIGRATOR_CLASS, *argv]


def _resolve_maven_command(project_dir: Path, argv: list[str]) -> list[str]:
    wrapper_candidates = ["mvnw.cmd", "mvnw.bat", "mvnw"] if os.name == "nt" else ["mvnw"]
    for wrapper in wrapper_candidates:
        wrapper_path = project_dir / wrapper
        if not wrapper_path.exists():
            continue

        maven_args = " ".join(_quote_for_exec_plugin(arg) for arg in argv)
        if os.name == "nt":
            return [
                _windows_cmd(),
                "/c",
                str(wrapper_path),
                "-q",
                f"-Dexec.mainClass={MIGRATOR_CLASS}",
                f"-Dexec.args={maven_args}",
                "exec:java",
            ]

        return [
            str(wrapper_path),
            "-q",
            f"-Dexec.mainClass={MIGRATOR_CLASS}",
            f"-Dexec.args={maven_args}",
            "exec:java",
        ]

    mvn_path = shutil.which("mvn")
    if mvn_path:
        maven_args = " ".join(_quote_for_exec_plugin(arg) for arg in argv)
        if os.name == "nt":
            return [
                _windows_cmd(),
                "/c",
                mvn_path,
                "-q",
                f"-Dexec.mainClass={MIGRATOR_CLASS}",
                f"-Dexec.args={maven_args}",
                "exec:java",
            ]
        return [
            mvn_path,
            "-q",
            f"-Dexec.mainClass={MIGRATOR_CLASS}",
            f"-Dexec.args={maven_args}",
            "exec:java",
        ]

    raise FileNotFoundError(
        "No runnable JAR found in ./target and could not find 'mvnw'/'mvnw.cmd'/'mvn' in PATH"
    )


def main(argv: list[str]) -> int:
    if len(argv) not in (1, 2):
        print(f"Usage: {Path(sys.argv[0]).name} <db-path|db.mv.db> [output.sql]", file=sys.stderr)
        return 1

    script_path = Path(__file__).resolve()
    project_dir = script_path.parent.parent

    if not project_dir.exists():
        print(f"Project directory not found: {project_dir}", file=sys.stderr)
        return 2

    command = _resolve_java_command(project_dir, argv)
    if command is None:
        try:
            command = _resolve_maven_command(project_dir, argv)
        except FileNotFoundError as exc:
            print(str(exc), file=sys.stderr)
            return 127

    try:
        completed = subprocess.run(command, cwd=project_dir, check=False)
        return completed.returncode
    except FileNotFoundError as ex:
        print(
            "Failed to launch migration command.\n"
            f"  cwd: {project_dir}\n"
            f"  command: {command}\n"
            f"  missing executable/path: {ex.filename}",
            file=sys.stderr,
        )
        return 127


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
