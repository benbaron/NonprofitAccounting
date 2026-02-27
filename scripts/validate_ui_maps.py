#!/usr/bin/env python3
"""Validate UI map contracts for nonprofit bookkeeping UI yaml files."""

from __future__ import annotations

import argparse
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml


SKIP_KEY_SCAN = {
    "field_semantics",
    "source_of_truth",
    "diff_summary",
}


@dataclass
class ValidationResult:
    path: Path
    parse_error: str | None
    duplicate_ids: list[str]
    unresolved_targets: list[tuple[str, str]]
    unresolved_triggers: list[tuple[str, str]]
    missing_self_panel_classes: list[str]

    @property
    def ok(self) -> bool:
        return not (
            self.parse_error
            or self.duplicate_ids
            or self.unresolved_targets
            or self.unresolved_triggers
            or self.missing_self_panel_classes
        )


def walk(node: Any, on_dict) -> None:
    if isinstance(node, dict):
        on_dict(node)
        for k, v in node.items():
            if k in SKIP_KEY_SCAN:
                continue
            walk(v, on_dict)
    elif isinstance(node, list):
        for item in node:
            walk(item, on_dict)


def validate_map(path: Path) -> ValidationResult:
    try:
        data = yaml.safe_load(path.read_text())
    except yaml.YAMLError as exc:
        return ValidationResult(
            path=path,
            parse_error=str(exc),
            duplicate_ids=[],
            unresolved_targets=[],
            unresolved_triggers=[],
            missing_self_panel_classes=[],
        )
    ids: list[str] = []
    targets: list[tuple[str, str]] = []
    triggers: list[tuple[str, str]] = []
    missing_self_panel_classes: list[str] = []

    def collect(d: dict[str, Any]) -> None:
        node_id = d.get("id")
        if isinstance(node_id, str):
            ids.append(node_id)
            target = d.get("target")
            trigger = d.get("trigger")
            if isinstance(target, str):
                targets.append((node_id, target))
            if isinstance(trigger, str):
                triggers.append((node_id, trigger))
            if target == node_id and "panel_class" not in d:
                missing_self_panel_classes.append(node_id)

    walk(data, collect)

    duplicate_ids = [k for k, v in Counter(ids).items() if v > 1]
    id_set = set(ids)
    target_registry = set((data.get("target_registry") or {}).keys())
    trigger_registry = set((data.get("trigger_registry") or {}).keys())

    unresolved_targets = [
        (node_id, target)
        for node_id, target in targets
        if target not in id_set and target not in target_registry
    ]
    unresolved_triggers = [
        (node_id, trigger)
        for node_id, trigger in triggers
        if trigger not in trigger_registry
    ]

    return ValidationResult(
        path=path,
        parse_error=None,
        duplicate_ids=duplicate_ids,
        unresolved_targets=unresolved_targets,
        unresolved_triggers=unresolved_triggers,
        missing_self_panel_classes=missing_self_panel_classes,
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--map",
        action="append",
        dest="maps",
        default=[
            "doc/ui/nonprofitbookkeeping-ui-map.yaml",
            "doc/ui/nonprofitbookkeeping-ui-package-map.yaml",
        ],
        help="YAML map file to validate. Can be specified multiple times.",
    )
    args = parser.parse_args()

    # Preserve order while deduping defaults + cli additions.
    seen = set()
    map_paths = []
    for m in args.maps:
        if m in seen:
            continue
        seen.add(m)
        map_paths.append(Path(m))

    results = [validate_map(path) for path in map_paths]

    failed = False
    for result in results:
        print(f"[{result.path}]")
        if result.parse_error:
            print("  parse_error: 1")
            print(f"    parse_error_detail={result.parse_error}")
            failed = True
            continue
        print(f"  duplicate_ids: {len(result.duplicate_ids)}")
        print(f"  unresolved_targets: {len(result.unresolved_targets)}")
        print(f"  unresolved_triggers: {len(result.unresolved_triggers)}")
        print(
            "  missing_self_panel_classes: "
            f"{len(result.missing_self_panel_classes)}"
        )

        if not result.ok:
            failed = True
            if result.duplicate_ids:
                print(f"    duplicate_ids_detail={result.duplicate_ids}")
            if result.unresolved_targets:
                print(
                    "    unresolved_targets_detail="
                    f"{result.unresolved_targets[:20]}"
                )
            if result.unresolved_triggers:
                print(
                    "    unresolved_triggers_detail="
                    f"{result.unresolved_triggers[:20]}"
                )
            if result.missing_self_panel_classes:
                print(
                    "    missing_self_panel_classes_detail="
                    f"{result.missing_self_panel_classes[:20]}"
                )

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
