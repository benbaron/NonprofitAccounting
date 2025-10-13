# H2 Migration Guide

Legacy versions of NonprofitAccounting distributed company data as `.npbk`
archives that contained a zipped JSON payload.  The runtime application now
expects company imports to be supplied as standard H2 SQL scripts instead.

## Converting Existing `.npbk` Files

Use the migration helper located in `scripts/npbk_to_h2_sql.sh` to convert
legacy archives into scripts that can be imported by the application.

```
./scripts/npbk_to_h2_sql.sh path/to/legacy-file.npbk path/to/output.sql
```

The script invokes the `nonprofitbookkeeping.tools.NpbkToH2ScriptMigrator`
utility which:

1. Loads the `.npbk` archive and hydrates a temporary H2 database.
2. Writes an executable SQL script (including `DROP`, `CREATE`, and `INSERT`
   statements) to the location you specify.

The resulting `output.sql` file can be version-controlled or stored alongside
other H2 assets and imported directly through the application UI.

## Importing Into the Application

1. Open or create an H2 database from the **Database → Open/Create H2 DB…**
   menu.
2. Choose **Database → Import H2 script into DB…** and select the SQL script
   generated above.
3. The importer streams the script into the connected H2 database using
   `RUNSCRIPT`, replacing any existing company data in the process.

This flow removes the legacy zip-reading code from the runtime codebase while
still providing a path to preserve historical `.npbk` data.
