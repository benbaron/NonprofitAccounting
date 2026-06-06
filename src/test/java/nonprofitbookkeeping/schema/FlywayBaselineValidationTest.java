package nonprofitbookkeeping.schema;

import nonprofitbookkeeping.core.Database;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates Flyway runtime schema coverage against the current application needs.
 *
 * <p>This is still a transitional gate. It requires the Flyway migrations to
 * create the current minimum runtime tables and columns, including known import
 * staging tables through V005. It also writes a drift/self-DDL report so the
 * remaining schema-authority cleanup can proceed without guessing.</p>
 */
class FlywayBaselineValidationTest
{
    private static final Set<String> REQUIRED_FLYWAY_VERSIONS = orderedSet("1", "2", "3", "4", "5");

    private static final Set<String> REQUIRED_RUNTIME_TABLES = orderedSet(
        "SCHEMA_MIGRATION_HISTORY",
        "FLYWAY_SCHEMA_HISTORY",
        "COMPANY_PROFILE",
        "CHART_OF_ACCOUNTS",
        "ACCOUNT",
        "ACCOUNT_FUND",
        "FUND",
        "COUNTERPARTY",
        "DONOR",
        "PERSON",
        "JOURNAL_TRANSACTION",
        "JOURNAL_ENTRY",
        "TRANSACTION_INFO",
        "TXN_SUPPLEMENTAL_LINE",
        "TXN",
        "TXN_SPLIT",
        "ACTIVITY",
        "MERCHANT",
        "SCHEDULE_KIND",
        "REPORT_SECTION",
        "ACCOUNT_ALIAS",
        "FUND_ALIAS",
        "ACCOUNT_REPORT_SECTION",
        "ACCOUNT_SCHEDULE_REQUIREMENT",
        "ACCOUNT_SUBTYPE_SCHEDULE_DEFAULT",
        "LEGACY_TXN_MAP",
        "FUND_TRANSFER",
        "FUND_TRANSFER_STATUS_TRANSITION",
        "FUND_TRANSFER_INTEGRITY_EVENT",
        "FUND_TRANSFER_REPAIR_QUEUE",
        "RM_DONATION_SUMMARY",
        "RM_GRANT_SUMMARY",
        "RM_FUND_SUMMARY",
        "RM_RECONCILIATION_SUMMARY",
        "RM_DEPRECIATION_SUMMARY",
        "BANK_STATEMENT",
        "BANK_ID_RECORD",
        "BANKING_TRANSACTION_RECORD",
        "LEDGER_RECORD",
        "ASSET_RECORD_DETAIL",
        "INVENTORY_ASSET_LINK",
        "DEPRECIATION_RUN",
        "DEPRECIATION_RECORD",
        "DEPRECIATION_RUN_EVENT",
        "GRANT_RECORD",
        "GRANT_POSTING_LINK",
        "DONATION_RECORD",
        "DONATION_JOURNAL_LINK",
        "OPERATIONAL_LINK_BACKFILL_QUEUE",
        "UNDEPOSITED_FUNDS_ITEM",
        "DOCUMENT",
        "JSON_STORAGE",
        "COMPANY_STORE",
        "SALE_RECORD",
        "ALIAS_REVIEW_QUEUE",
        "SCLX_IMPORT_RUN",
        "SCLX_IMPORT_ERROR",
        "IMPORTED_ORGANIZATION_RECORD",
        "IMPORTED_FUND_RECORD",
        "IMPORTED_EVENT_RECORD",
        "IMPORTED_DOCUMENT_RECORD",
        "IMPORTED_REPORTING_PERIOD_RECORD",
        "IMPORTED_SUPPLY_RECORD",
        "IMPORTED_OTHER_ASSET_ITEM_RECORD",
        "IMPORTED_OUTSTANDING_ITEM_RECORD",
        "IMPORTED_ASSET_RECORD",
        "IMPORTED_BUDGET",
        "IMPORTED_BUDGET_LINE",
        "IMPORTED_BANK_STATEMENT",
        "IMPORTED_BANKING_ITEM"
    );

    private static final Set<String> FLYWAY_OWNED_STAGING_TABLES = orderedSet(
        "SCLX_IMPORT_RUN",
        "SCLX_IMPORT_ERROR",
        "IMPORTED_ORGANIZATION_RECORD",
        "IMPORTED_FUND_RECORD",
        "IMPORTED_EVENT_RECORD",
        "IMPORTED_DOCUMENT_RECORD",
        "IMPORTED_REPORTING_PERIOD_RECORD",
        "IMPORTED_SUPPLY_RECORD",
        "IMPORTED_OTHER_ASSET_ITEM_RECORD",
        "IMPORTED_OUTSTANDING_ITEM_RECORD",
        "IMPORTED_ASSET_RECORD",
        "IMPORTED_BUDGET",
        "IMPORTED_BUDGET_LINE",
        "IMPORTED_BANK_STATEMENT",
        "IMPORTED_BANKING_ITEM"
    );

    private static final Map<String, Set<String>> REQUIRED_RUNTIME_COLUMNS = requiredColumns();

    @TempDir
    Path tempDir;

    @Test
    void flywayCreatesCurrentRuntimeMinimumAndReportsSchemaDrift() throws Exception
    {
        DbSnapshot flyway = createFlywaySnapshot(tempDir.resolve("flyway-baseline"));
        DbSnapshot ensure = createEnsureSchemaSnapshot(tempDir.resolve("ensure-schema"));
        List<SelfDdlReference> selfDdlReferences = findSelfDdlReferences();

        List<String> failures = new ArrayList<>();
        for (String version : REQUIRED_FLYWAY_VERSIONS)
        {
            if (!flyway.successfulFlywayVersions().contains(version))
            {
                failures.add("Flyway migration version did not run successfully: " + version);
            }
        }

        for (String table : REQUIRED_RUNTIME_TABLES)
        {
            if (!flyway.tables().containsKey(table))
            {
                failures.add("Flyway is missing required table: " + table);
                continue;
            }
            Set<String> requiredColumns = REQUIRED_RUNTIME_COLUMNS.getOrDefault(table, Set.of());
            Set<String> actualColumns = flyway.tables().get(table).columns().keySet();
            for (String column : requiredColumns)
            {
                if (!actualColumns.contains(column))
                {
                    failures.add("Flyway is missing required column: " + table + "." + column);
                }
            }
        }

        Path report = writeDriftReport(flyway, ensure, failures, selfDdlReferences);
        assertTrue(failures.isEmpty(), () -> "Flyway does not satisfy current runtime schema minimum. See " + report + "\n" + String.join("\n", failures));
    }

    private DbSnapshot createFlywaySnapshot(Path databaseBase) throws SQLException
    {
        String url = h2Url(databaseBase);
        Flyway.configure()
            .dataSource(url, "sa", "")
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .load()
            .migrate();
        return snapshot("flyway", url);
    }

    private DbSnapshot createEnsureSchemaSnapshot(Path databaseBase) throws SQLException
    {
        Database.init(databaseBase);
        Database.get().ensureSchema();
        return snapshot("ensureSchema", Database.get().getJdbcUrl());
    }

    private DbSnapshot snapshot(String name, String url) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(url, "sa", ""))
        {
            DatabaseMetaData meta = connection.getMetaData();
            Map<String, TableDef> tables = new TreeMap<>();
            try (ResultSet rs = meta.getTables(null, "PUBLIC", "%", new String[] {"TABLE", "VIEW"}))
            {
                while (rs.next())
                {
                    String tableName = normalize(rs.getString("TABLE_NAME"));
                    String tableType = rs.getString("TABLE_TYPE");
                    tables.put(tableName, new TableDef(tableName, tableType, new TreeMap<>()));
                }
            }

            for (String table : new ArrayList<>(tables.keySet()))
            {
                try (ResultSet rs = meta.getColumns(null, "PUBLIC", table, "%"))
                {
                    while (rs.next())
                    {
                        String columnName = normalize(rs.getString("COLUMN_NAME"));
                        ColumnDef column = new ColumnDef(
                            columnName,
                            normalize(rs.getString("TYPE_NAME")),
                            rs.getInt("COLUMN_SIZE"),
                            rs.getInt("DECIMAL_DIGITS"),
                            rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable
                        );
                        tables.get(table).columns().put(columnName, column);
                    }
                }
            }

            Set<String> versions = new TreeSet<>();
            if (tables.containsKey("FLYWAY_SCHEMA_HISTORY"))
            {
                try (ResultSet rs = connection.createStatement().executeQuery(
                    "SELECT version FROM flyway_schema_history WHERE success = true AND version IS NOT NULL"))
                {
                    while (rs.next())
                    {
                        versions.add(rs.getString(1));
                    }
                }
            }

            return new DbSnapshot(name, tables, versions);
        }
    }

    private List<SelfDdlReference> findSelfDdlReferences() throws IOException
    {
        Path sourceRoot = Path.of("src", "main", "java");
        if (!Files.exists(sourceRoot))
        {
            return List.of();
        }

        Pattern createTablePattern = Pattern.compile("CREATE\\s+TABLE(?:\\s+IF\\s+NOT\\s+EXISTS)?\\s+([A-Za-z0-9_]+)", Pattern.CASE_INSENSITIVE);
        List<SelfDdlReference> references = new ArrayList<>();
        try (var paths = Files.walk(sourceRoot))
        {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList())
            {
                String text = Files.readString(path, StandardCharsets.UTF_8);
                Matcher matcher = createTablePattern.matcher(text);
                while (matcher.find())
                {
                    String table = normalize(matcher.group(1));
                    references.add(new SelfDdlReference(path.toString(), table, FLYWAY_OWNED_STAGING_TABLES.contains(table) || REQUIRED_RUNTIME_TABLES.contains(table)));
                }
            }
        }
        return references;
    }

    private Path writeDriftReport(DbSnapshot flyway, DbSnapshot ensure, List<String> failures, List<SelfDdlReference> selfDdlReferences) throws IOException
    {
        Path report = Path.of("target", "schema-drift", "flyway-vs-ensure-schema.md");
        Files.createDirectories(report.getParent());

        StringBuilder out = new StringBuilder();
        out.append("# Flyway runtime schema validation report\n\n");
        out.append("This report is generated by `FlywayBaselineValidationTest`.\n\n");
        out.append("## Successful Flyway versions\n\n");
        appendSet(out, "Versions", flyway.successfulFlywayVersions());

        out.append("## Validation failures\n\n");
        if (failures.isEmpty())
        {
            out.append("No minimum-runtime failures.\n\n");
        }
        else
        {
            for (String failure : failures)
            {
                out.append("- ").append(failure).append('\n');
            }
            out.append('\n');
        }

        Set<String> missingFromFlyway = new TreeSet<>(ensure.tables().keySet());
        missingFromFlyway.removeAll(flyway.tables().keySet());
        Set<String> extraInFlyway = new TreeSet<>(flyway.tables().keySet());
        extraInFlyway.removeAll(ensure.tables().keySet());

        appendSet(out, "Tables present in ensureSchema but missing from Flyway", missingFromFlyway);
        appendSet(out, "Tables present in Flyway but missing from ensureSchema", extraInFlyway);

        out.append("## Repository/self-DDL inventory\n\n");
        if (selfDdlReferences.isEmpty())
        {
            out.append("No CREATE TABLE references found under `src/main/java`.\n\n");
        }
        else
        {
            out.append("| File | Table | Covered by current Flyway/runtime required set |\n");
            out.append("|---|---|---:|\n");
            for (SelfDdlReference reference : selfDdlReferences)
            {
                out.append("| `").append(reference.file()).append("` | `")
                    .append(reference.table()).append("` | ")
                    .append(reference.coveredByFlywayGate() ? "yes" : "no")
                    .append(" |\n");
            }
            out.append('\n');
        }

        out.append("## Column drift\n\n");
        for (String table : intersect(ensure.tables().keySet(), flyway.tables().keySet()))
        {
            TableDef ensureTable = ensure.tables().get(table);
            TableDef flywayTable = flyway.tables().get(table);
            Set<String> missingColumns = new TreeSet<>(ensureTable.columns().keySet());
            missingColumns.removeAll(flywayTable.columns().keySet());
            Set<String> extraColumns = new TreeSet<>(flywayTable.columns().keySet());
            extraColumns.removeAll(ensureTable.columns().keySet());

            if (missingColumns.isEmpty() && extraColumns.isEmpty())
            {
                continue;
            }

            out.append("### ").append(table).append("\n\n");
            appendSet(out, "Missing from Flyway", missingColumns);
            appendSet(out, "Extra in Flyway", extraColumns);
        }

        Files.writeString(report, out.toString());
        return report;
    }

    private static void appendSet(StringBuilder out, String title, Set<String> values)
    {
        out.append("### ").append(title).append("\n\n");
        if (values.isEmpty())
        {
            out.append("None.\n\n");
            return;
        }
        for (String value : values)
        {
            out.append("- `").append(value).append("`\n");
        }
        out.append('\n');
    }

    private static Set<String> intersect(Set<String> left, Set<String> right)
    {
        Set<String> out = new TreeSet<>(left);
        out.retainAll(right);
        return out;
    }

    private static String h2Url(Path databaseBase)
    {
        return "jdbc:h2:file:" + databaseBase.toAbsolutePath() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    }

    private static String normalize(String value)
    {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private static Set<String> orderedSet(String... values)
    {
        Set<String> out = new LinkedHashSet<>();
        for (String value : values)
        {
            out.add(normalize(value));
        }
        return out;
    }

    private static Map<String, Set<String>> requiredColumns()
    {
        Map<String, Set<String>> columns = new LinkedHashMap<>();
        columns.put("ACCOUNT", orderedSet("ACCOUNT_NUMBER", "ID", "CODE", "CHART_ID", "NORMAL_BALANCE", "IS_POSTING", "IS_ACTIVE"));
        columns.put("JOURNAL_TRANSACTION", orderedSet("ID", "DATE_TEXT", "MEMO", "TO_FROM", "CHECK_NUMBER", "RECONCILED", "BUDGET_TRACKING", "ASSOCIATED_FUND_NAME"));
        columns.put("JOURNAL_ENTRY", orderedSet("ID", "TXN_ID", "AMOUNT", "ACCOUNT_NUMBER", "ACCOUNT_SIDE", "ACCOUNT_NAME", "FUND_NUMBER"));
        columns.put("TRANSACTION_INFO", orderedSet("TXN_ID", "K", "V"));
        columns.put("TXN", orderedSet("ID", "TXN_DATE", "PAYEE_ID", "MEMO", "BANK_ACCOUNT_ID", "CREATED_AT", "UPDATED_AT"));
        columns.put("TXN_SPLIT", orderedSet("ID", "TXN_ID", "ACCOUNT_ID", "FUND_ID", "ACTIVITY_ID", "MERCHANT_ID", "NMR_FLAG", "NOTES", "AMOUNT_SIGNED"));
        columns.put("FUND", orderedSet("ID", "CODE", "NAME", "FUND_TYPE", "IS_ACTIVE", "CREATED_AT", "UPDATED_AT"));
        columns.put("COUNTERPARTY", orderedSet("ID", "DISPLAY_NAME", "KIND", "EMAIL", "PHONE", "IS_ACTIVE"));
        columns.put("DONATION_RECORD", orderedSet("DONATION_ID", "AMOUNT", "CASH_ACCOUNT_NUMBER", "REVENUE_ACCOUNT_NUMBER", "JOURNAL_TXN_ID"));
        columns.put("GRANT_RECORD", orderedSet("GRANT_RECORD_ID", "GRANT_ID", "AMOUNT", "JOURNAL_TXN_ID", "CANONICAL_TXN_ID"));
        columns.put("BANKING_TRANSACTION_RECORD", orderedSet("BANKING_RECORD_ID", "BANK_ID_RECORD_ID", "JOURNAL_TXN_ID", "TRANSACTION_DATE", "AMOUNT", "MATCH_STATUS"));
        columns.put("BANK_STATEMENT", orderedSet("ID", "BANK_NAME", "STATEMENT_DATE", "BANK_ID_RECORD_ID", "STATUS"));
        columns.put("FUND_TRANSFER", orderedSet("ID", "TRANSFER_DATE", "FROM_FUND_ID", "TO_FUND_ID", "AMOUNT", "STATUS", "POSTED_TXN_ID"));
        columns.put("DEPRECIATION_RUN", orderedSet("DEPRECIATION_RUN_ID", "RUN_DATE", "RUN_STATUS", "POSTED_TXN_ID"));
        columns.put("DEPRECIATION_RECORD", orderedSet("DEPRECIATION_RECORD_ID", "DEPRECIATION_RUN_ID", "ASSET_RECORD_ID", "NET_DEPRECIATION"));
        columns.put("SCLX_IMPORT_RUN", orderedSet("ID", "SOURCE_NAME", "IMPORTED_AT", "STATUS"));
        columns.put("SCLX_IMPORT_ERROR", orderedSet("ID", "IMPORT_RUN_ID", "SEVERITY", "MESSAGE"));
        columns.put("IMPORTED_ORGANIZATION_RECORD", orderedSet("ORGANIZATION_ID", "NAME"));
        columns.put("IMPORTED_FUND_RECORD", orderedSet("FUND_ID", "NAME", "RESTRICTED"));
        columns.put("IMPORTED_EVENT_RECORD", orderedSet("EVENT_ID", "NAME"));
        columns.put("IMPORTED_DOCUMENT_RECORD", orderedSet("DOCUMENT_ID"));
        columns.put("IMPORTED_REPORTING_PERIOD_RECORD", orderedSet("PERIOD_KEY", "START_DATE", "END_DATE"));
        columns.put("IMPORTED_SUPPLY_RECORD", orderedSet("SUPPLY_ID"));
        columns.put("IMPORTED_OTHER_ASSET_ITEM_RECORD", orderedSet("OTHER_ASSET_ITEM_ID"));
        columns.put("IMPORTED_OUTSTANDING_ITEM_RECORD", orderedSet("OUTSTANDING_ITEM_ID"));
        columns.put("IMPORTED_ASSET_RECORD", orderedSet("ASSET_ID", "ACCUMULATED_DEPRECIATION", "ITEM_TYPE"));
        columns.put("IMPORTED_BUDGET", orderedSet("BUDGET_ID"));
        columns.put("IMPORTED_BUDGET_LINE", orderedSet("BUDGET_ID", "LINE_ORDINAL"));
        columns.put("IMPORTED_BANK_STATEMENT", orderedSet("IMPORT_ID"));
        columns.put("IMPORTED_BANKING_ITEM", orderedSet("BANKING_ITEM_ID"));
        return columns;
    }

    private record DbSnapshot(String name, Map<String, TableDef> tables, Set<String> successfulFlywayVersions) {}

    private record TableDef(String name, String type, Map<String, ColumnDef> columns) {}

    private record ColumnDef(String name, String type, int size, int scale, boolean nullable) {}

    private record SelfDdlReference(String file, String table, boolean coveredByFlywayGate) {}
}
