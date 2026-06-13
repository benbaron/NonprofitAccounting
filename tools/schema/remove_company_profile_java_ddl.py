#!/usr/bin/env python3
"""
Remove duplicate company_profile Java DDL from Database.ensureSchema().

This helper applies issue #676 as an exact-text local patch. It intentionally
fails closed if the expected source text is not found, so it will not make an
approximate or partial edit to Database.java.
"""

from __future__ import annotations

from pathlib import Path
import sys

DATABASE_PATH = Path("src/main/java/nonprofitbookkeeping/core/Database.java")

CALL_TEXT = "\t\t\tensureCompanyProfile(st);\n"

HELPER_TEXT = """\tprivate void ensureCompanyProfile(Statement st) throws SQLException
\t{
\t\tst.execute(\"\"\"
\t\t\t    CREATE TABLE IF NOT EXISTS company_profile(
\t\t\t      id INT PRIMARY KEY,
\t\t\t      name VARCHAR(255),
\t\t\t      address VARCHAR(255),
\t\t\t      phone VARCHAR(64),
\t\t\t      email VARCHAR(255),
\t\t\t      fiscal_year_start VARCHAR(16),
\t\t\t      base_currency VARCHAR(8),
\t\t\t      starting_balance_date VARCHAR(16),
\t\t\t      chart_of_accounts_type VARCHAR(64),
\t\t\t      admin_username VARCHAR(128),
\t\t\t      admin_password VARCHAR(128),
\t\t\t      default_bank_account VARCHAR(128),
\t\t\t      enable_fund_accounting BOOLEAN,
\t\t\t      enable_inventory BOOLEAN,
\t\t\t      enable_multi_currency BOOLEAN,
\t\t\t      legal_structure VARCHAR(128),
\t\t\t      tax_id VARCHAR(128),
\t\t\t      company_file_dir VARCHAR(512),
\t\t\t      company_file_name VARCHAR(255)
\t\t\t    )
\t\t\t\"\"\");
\t\tst.execute(
\t\t\t\"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS legal_structure VARCHAR(128);\");
\t\tst.execute(
\t\t\t\"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS tax_id VARCHAR(128);\");
\t\tst.execute(
\t\t\t\"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS company_file_dir VARCHAR(512);\");
\t\tst.execute(
\t\t\t\"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS company_file_name VARCHAR(255);\");
\t}
\t
"""


def main() -> int:
    if not DATABASE_PATH.exists():
        print(f"ERROR: {DATABASE_PATH} not found; run from the repository root.", file=sys.stderr)
        return 2

    original = DATABASE_PATH.read_text(encoding="utf-8")

    if CALL_TEXT not in original:
        print("ERROR: expected ensureCompanyProfile(st) call was not found.", file=sys.stderr)
        return 3
    if HELPER_TEXT not in original:
        print("ERROR: expected ensureCompanyProfile helper body was not found.", file=sys.stderr)
        return 4

    updated = original.replace(CALL_TEXT, "", 1).replace(HELPER_TEXT, "", 1)

    if updated == original:
        print("ERROR: no changes were made.", file=sys.stderr)
        return 5

    DATABASE_PATH.write_text(updated, encoding="utf-8")
    print("Removed duplicate company_profile Java DDL from Database.ensureSchema().")
    print("Next: run the schema tests and inspect the git diff.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
