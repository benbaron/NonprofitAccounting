package nonprofitbookkeeping.schema;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that Flyway can create the current minimum runtime schema.
 *
 * <p>Flyway versions are normalized before comparison because migration files
 * such as {@code V001__...sql} may be recorded by Flyway/H2 as {@code 001},
 * {@code 004.0}, or another equivalent numeric representation.</p>
 */
class FlywayBaselineValidationTest
{
    private static final Set<String> REQUIRED_FLYWAY_VERSIONS = orderedSet("1", "2", "3", "4", "5", "6", "7", "8", "9");

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

    private static final Map<String, Set<String>> REQUIRED_RUNTIME_COLUMNS = requiredColumns();

    @TempDir
    Path tempDir;

    @Test
    void flywayCreatesCurrentRuntimeMinimum() throws Exception
    {
        DbSnapshot flyway = createFlywaySnapshot(tempDir.resolve("flyway-baseline"));

        List<String> failures = new ArrayList<>();
        for (String version : REQUIRED_FLYWAY_VERSIONS)
        {
            if (!flyway.successfulFlywayVersions().contains(version))
            {
                failures.add("Flyway migration version did not run successfully: " + version
                    + " (actual successful versions: " + flyway.successfulFlywayVersions() + ")");
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

        assertTrue(failures.isEmpty(), () -> String.join("\n", failures));
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
        return snapshot(url);
    }

    private DbSnapshot snapshot(String url) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(url, "sa", ""))
        {
            DatabaseMetaData meta = connection.getMetaData();
            Map<String, TableDef> tables = new TreeMap<>();
            Map<String, String> actualTableNames = new TreeMap<>();
            try (ResultSet rs = meta.getTables(null, "PUBLIC", "%", new String[] {"TABLE", "VIEW"}))
            {
                while (rs.next())
                {
                    String actualTableName = rs.getString("TABLE_NAME");
                    String tableName = normalize(actualTableName);
                    tables.put(tableName, new TableDef(tableName, new TreeMap<>()));
                    actualTableNames.put(tableName, actualTableName);
                }
            }

            for (String table : new ArrayList<>(tables.keySet()))
            {
                try (ResultSet rs = meta.getColumns(null, "PUBLIC", actualTableNames.get(table), "%"))
                {
                    while (rs.next())
                    {
                        tables.get(table).columns().put(normalize(rs.getString("COLUMN_NAME")), true);
                    }
                }
            }

            Set<String> versions = new TreeSet<>();
            String flywayHistoryTable = actualTableNames.get("FLYWAY_SCHEMA_HISTORY");
            if (flywayHistoryTable != null)
            {
                try (Statement st = connection.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM " + quoteIdentifier(flywayHistoryTable)))
                {
                    while (rs.next())
                    {
                        if (rs.getBoolean("success"))
                        {
                            versions.add(normalizeFlywayVersion(rs.getString("version")));
                        }
                    }
                }
            }

            return new DbSnapshot(tables, versions);
        }
    }

    private static String h2Url(Path databaseBase)
    {
        return "jdbc:h2:file:" + databaseBase.toAbsolutePath() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    }

    private static String normalize(String value)
    {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private static String normalizeFlywayVersion(String value)
    {
        if (value == null || value.isBlank())
        {
            return "";
        }
        String normalized = value.strip().replaceFirst("^0+(?=\\d)", "");
        normalized = normalized.replaceFirst("\\.0+$", "");
        return normalized.isBlank() ? "0" : normalized;
    }

    private static String quoteIdentifier(String value)
    {
        return "\"" + value.replace("\"", "\"\"") + "\"";
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
        columns.put("COMPANY_PROFILE", orderedSet(
            "ID",
            "NAME",
            "ADDRESS",
            "PHONE",
            "EMAIL",
            "FISCAL_YEAR_START",
            "BASE_CURRENCY",
            "STARTING_BALANCE_DATE",
            "CHART_OF_ACCOUNTS_TYPE",
            "ADMIN_USERNAME",
            "ADMIN_PASSWORD",
            "DEFAULT_BANK_ACCOUNT",
            "ENABLE_FUND_ACCOUNTING",
            "ENABLE_INVENTORY",
            "ENABLE_MULTI_CURRENCY",
            "LEGAL_STRUCTURE",
            "TAX_ID",
            "COMPANY_FILE_DIR",
            "COMPANY_FILE_NAME"
        ));
        columns.put("ACCOUNT", orderedSet("ACCOUNT_NUMBER", "ID", "CODE", "CHART_ID", "NORMAL_BALANCE", "IS_POSTING", "IS_ACTIVE"));
        columns.put("JOURNAL_TRANSACTION", orderedSet("ID", "DATE_TEXT", "MEMO", "TO_FROM", "CHECK_NUMBER", "RECONCILED", "BUDGET_TRACKING", "ASSOCIATED_FUND_NAME"));
        columns.put("JOURNAL_ENTRY", orderedSet("ID", "TXN_ID", "AMOUNT", "ACCOUNT_NUMBER", "ACCOUNT_SIDE", "ACCOUNT_NAME", "FUND_NUMBER"));
        columns.put("TRANSACTION_INFO", orderedSet("TXN_ID", "K", "V"));
        columns.put("TXN", orderedSet("ID", "TXN_DATE", "PAYEE_ID", "MEMO", "BANK_ACCOUNT_ID", "CREATED_AT", "UPDATED_AT"));
        columns.put("TXN_SPLIT", orderedSet("ID", "TXN_ID", "ACCOUNT_ID", "FUND_ID", "ACTIVITY_ID", "MERCHANT_ID", "NMR_FLAG", "NOTES", "AMOUNT_SIGNED"));
        columns.put("FUND", orderedSet("ID", "CODE", "NAME", "FUND_TYPE", "IS_ACTIVE", "CREATED_AT", "UPDATED_AT"));
        columns.put("COUNTERPARTY", orderedSet("ID", "DISPLAY_NAME", "KIND", "EMAIL", "PHONE", "NOTES", "IS_ACTIVE"));
        columns.put("MERCHANT", orderedSet("ID", "NAME", "NOTES", "IS_ACTIVE"));
        columns.put("DONATION_RECORD", orderedSet("DONATION_ID", "AMOUNT", "CASH_ACCOUNT_NUMBER", "REVENUE_ACCOUNT_NUMBER", "JOURNAL_TXN_ID"));
        columns.put("GRANT_RECORD", orderedSet("GRANT_RECORD_ID", "GRANT_ID", "AMOUNT", "JOURNAL_TXN_ID", "CANONICAL_TXN_ID"));
        columns.put("BANKING_TRANSACTION_RECORD", orderedSet("BANKING_RECORD_ID", "BANK_ID_RECORD_ID", "JOURNAL_TXN_ID", "TRANSACTION_DATE", "AMOUNT", "MATCH_STATUS"));
        columns.put("BANK_STATEMENT", orderedSet("ID", "BANK_NAME", "STATEMENT_DATE", "BANK_ID_RECORD_ID", "STATUS"));
        columns.put("FUND_TRANSFER", orderedSet("ID", "TRANSFER_DATE", "FROM_FUND_ID", "TO_FUND_ID", "AMOUNT", "STATUS", "POSTED_TXN_ID"));
        columns.put("DEPRECIATION_RUN", orderedSet("DEPRECIATION_RUN_ID", "RUN_DATE", "RUN_STATUS", "POSTED_TXN_ID"));
        columns.put("DEPRECIATION_RECORD", orderedSet("DEPRECIATION_RECORD_ID", "DEPRECIATION_RUN_ID", "ASSET_RECORD_ID", "NET_DEPRECIATION"));
        columns.put("SCLX_IMPORT_RUN", orderedSet("ID", "SOURCE_NAME", "IMPORTED_AT", "STATUS"));
        columns.put("SCLX_IMPORT_ERROR", orderedSet("ID", "IMPORT_RUN_ID", "SEVERITY", "MESSAGE"));
        columns.put("ALIAS_REVIEW_QUEUE", orderedSet("ID", "ALIAS_DOMAIN", "ALIAS_TEXT", "NORMALIZATION_KEY", "CANDIDATE_COUNT", "REASON", "STATUS", "CREATED_AT", "RESOLVER", "RESOLUTION_NOTE"));
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

    private record DbSnapshot(Map<String, TableDef> tables, Set<String> successfulFlywayVersions) {}

    private record TableDef(String name, Map<String, Boolean> columns) {}
}
