#!/usr/bin/env python3
"""
Remove duplicate party/profile table-shape Java DDL from Database.ensureSchema().

This helper applies the conservative first slice of issue #678. It removes only
exact CREATE/ALTER table-shape statements for donor/person from
ensurePeopleAndCounterparty(). It deliberately leaves data repair, index, and
constraint statements in place for separate review.

The script fails closed if the expected source text is not found.
"""

from __future__ import annotations

from pathlib import Path
import sys

DATABASE_PATH = Path("src/main/java/nonprofitbookkeeping/core/Database.java")

BLOCKS_TO_REMOVE = [
    """\t\tst.execute(\"\"\"
\t\t\t    CREATE TABLE IF NOT EXISTS donor(
\t\t\t      id BIGINT AUTO_INCREMENT PRIMARY KEY,
\t\t\t      external_id VARCHAR(64) UNIQUE,
\t\t\t      name VARCHAR(255),
\t\t\t      email VARCHAR(255),
\t\t\t      phone VARCHAR(64)
\t\t\t    )
\t\t\t\"\"\");
""",
    """\t\tst.execute(\"ALTER TABLE donor ADD COLUMN IF NOT EXISTS external_id VARCHAR(64);\");
\t\tst.execute(\"ALTER TABLE donor ADD COLUMN IF NOT EXISTS email VARCHAR(255);\");
\t\tst.execute(\"ALTER TABLE donor ADD COLUMN IF NOT EXISTS phone VARCHAR(64);\");
\t\tst.execute(\"ALTER TABLE donor ADD COLUMN IF NOT EXISTS name VARCHAR(255);\");
""",
    """\t\tst.execute(\"\"\"
\t\t\t    CREATE TABLE IF NOT EXISTS person(
\t\t\t      id BIGINT AUTO_INCREMENT PRIMARY KEY,
\t\t\t      name VARCHAR(255) NOT NULL,
\t\t\t      email VARCHAR(255),
\t\t\t      phone VARCHAR(64),
\t\t\t      type VARCHAR(32) NOT NULL DEFAULT 'DONOR'
\t\t\t    )
\t\t\t\"\"\");
""",
    """\t\tst.execute(\"ALTER TABLE person ADD COLUMN IF NOT EXISTS email VARCHAR(255);\");
\t\tst.execute(\"ALTER TABLE person ADD COLUMN IF NOT EXISTS phone VARCHAR(64);\");
\t\tst.execute(\"ALTER TABLE person ADD COLUMN IF NOT EXISTS type VARCHAR(32) DEFAULT 'DONOR';\");
""",
]


def main() -> int:
    if not DATABASE_PATH.exists():
        print(f"ERROR: {DATABASE_PATH} not found; run from the repository root.", file=sys.stderr)
        return 2

    original = DATABASE_PATH.read_text(encoding="utf-8")
    updated = original

    missing = []
    for block in BLOCKS_TO_REMOVE:
        if block not in updated:
            missing.append(block.splitlines()[0] if block.splitlines() else "<empty block>")
        else:
            updated = updated.replace(block, "", 1)

    if missing:
        print("ERROR: expected party/profile DDL text was not found.", file=sys.stderr)
        for item in missing:
            print(f"  missing near: {item}", file=sys.stderr)
        return 3

    if updated == original:
        print("ERROR: no changes were made.", file=sys.stderr)
        return 4

    DATABASE_PATH.write_text(updated, encoding="utf-8")
    print("Removed duplicate donor/person Java table-shape DDL from ensurePeopleAndCounterparty().")
    print("Left data repair, index, and constraint statements for separate review.")
    print("Next: run the party/profile schema tests and inspect the git diff.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
