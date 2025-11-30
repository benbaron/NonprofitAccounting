#!/usr/bin/env python3
"""
Heuristic detector for potentially unused code modules.

The script scans Java source files under src/main/java, collects declared
classes, interfaces, enums, and records, and counts their references across
both main and test sources. Any types that are not referenced outside their
own file are reported as potentially unused.

Limitations:
* Reflection, ServiceLoader usage, or resource-based lookups are not tracked.
* A type with only internal references (within the same file) will be flagged.
* Entry points with no direct references (e.g., main classes) are filtered by
  detecting "public static void main" methods.
"""

from __future__ import annotations

import re
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List

ROOT = Path(__file__).resolve().parent.parent
MAIN_SRC = ROOT / "src" / "main" / "java"
TEST_SRC = ROOT / "src" / "test" / "java"
JAVA_GLOB = "*.java"

DECLARATION_RE = re.compile(
    r"^(?:public\s+|protected\s+|private\s+)?"
    r"(?:abstract\s+|final\s+)?"
    r"(?:class|interface|enum|record)\s+"
    r"([A-Za-z_][A-Za-z0-9_]*)",
    re.MULTILINE,
)
PACKAGE_RE = re.compile(r"^package\s+([\w\.]+);", re.MULTILINE)
MAIN_METHOD_RE = re.compile(r"public\s+static\s+void\s+main\s*\(")
WORD_RE = re.compile(r"\b[A-Za-z_][A-Za-z0-9_]*\b")


@dataclass
class TypeEntry:
    name: str
    package: str | None
    path: Path
    has_main_method: bool

    @property
    def fqcn(self) -> str:
        return f"{self.package}.{self.name}" if self.package else self.name


def load_sources() -> Dict[Path, str]:
    files = list(MAIN_SRC.rglob(JAVA_GLOB)) + list(TEST_SRC.rglob(JAVA_GLOB))
    contents = {}
    for path in files:
        try:
            contents[path] = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            contents[path] = path.read_text(encoding="latin-1")
    return contents


def parse_types(contents: Dict[Path, str]) -> List[TypeEntry]:
    entries: List[TypeEntry] = []
    for path, text in contents.items():
        if not path.is_relative_to(MAIN_SRC):
            continue
        pkg_match = PACKAGE_RE.search(text)
        package = pkg_match.group(1) if pkg_match else None
        has_main = bool(MAIN_METHOD_RE.search(text))
        for name in DECLARATION_RE.findall(text):
            entries.append(TypeEntry(name=name, package=package, path=path, has_main_method=has_main))
    return entries


def build_token_counts(contents: Dict[Path, str]) -> tuple[Counter[str], dict[Path, Counter[str]]]:
    total_counts: Counter[str] = Counter()
    per_file_counts: dict[Path, Counter[str]] = {}
    for path, text in contents.items():
        tokens = Counter(WORD_RE.findall(text))
        total_counts.update(tokens)
        per_file_counts[path] = tokens
    return total_counts, per_file_counts


def detect_unused(
    entries: Iterable[TypeEntry],
    total_counts: Counter[str],
    per_file_counts: dict[Path, Counter[str]],
) -> List[TypeEntry]:
    unused: List[TypeEntry] = []
    for entry in entries:
        total_refs = total_counts.get(entry.name, 0) - per_file_counts.get(entry.path, Counter()).get(entry.name, 0)
        if total_refs <= 0 and not entry.has_main_method:
            unused.append(entry)
    return unused


def main() -> None:
    contents = load_sources()
    entries = parse_types(contents)
    total_counts, per_file_counts = build_token_counts(contents)
    unused = detect_unused(entries, total_counts, per_file_counts)

    print("Potentially unused types (no references outside their own file):")
    if not unused:
        print("None found")
        return

    for entry in sorted(unused, key=lambda item: str(item.path)):
        print(f"- {entry.fqcn} (file: {entry.path.relative_to(ROOT)})")


if __name__ == "__main__":
    main()
