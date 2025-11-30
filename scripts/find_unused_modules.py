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


def load_sources() -> tuple[Dict[Path, str], Dict[Path, str]]:
    def read(paths: Iterable[Path]) -> Dict[Path, str]:
        contents: Dict[Path, str] = {}
        for path in paths:
            try:
                contents[path] = path.read_text(encoding="utf-8")
            except UnicodeDecodeError:
                contents[path] = path.read_text(encoding="latin-1")
        return contents

    main_files = MAIN_SRC.rglob(JAVA_GLOB)
    test_files = TEST_SRC.rglob(JAVA_GLOB)
    return read(main_files), read(test_files)


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


@dataclass
class UnusedType:
    entry: TypeEntry
    reason: str


def detect_unused(
    entries: Iterable[TypeEntry],
    main_total: Counter[str],
    main_per_file: dict[Path, Counter[str]],
    test_total: Counter[str],
) -> List[UnusedType]:
    unused: List[UnusedType] = []
    for entry in entries:
        main_refs = main_total.get(entry.name, 0) - main_per_file.get(entry.path, Counter()).get(entry.name, 0)
        test_refs = test_total.get(entry.name, 0)
        total_refs = main_refs + test_refs
        if entry.has_main_method:
            continue

        if total_refs <= 0:
            unused.append(UnusedType(entry=entry, reason="no external references"))
        elif main_refs <= 0 and test_refs > 0:
            unused.append(UnusedType(entry=entry, reason="referenced only from tests"))
    return unused


def main() -> None:
    main_contents, test_contents = load_sources()
    entries = parse_types(main_contents)
    main_total, main_per_file = build_token_counts(main_contents)
    test_total, _ = build_token_counts(test_contents)
    unused = detect_unused(entries, main_total, main_per_file, test_total)

    print("Potentially unused types (no references outside their own file):")
    if not unused:
        print("None found")
        return

    for unused_type in sorted(unused, key=lambda item: str(item.entry.path)):
        entry = unused_type.entry
        print(
            f"- {entry.fqcn} (file: {entry.path.relative_to(ROOT)}; {unused_type.reason})"
        )


if __name__ == "__main__":
    main()
