package nonprofitbookkeeping.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nonprofitbookkeeping.reports.generator.*;
import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Registry of available Jasper report templates.
 * Each entry maps a user facing display name to the JRXML template
 * and the generator class responsible for binding data.
 */
public final class ReportTemplates {

    /** Immutable holder for template metadata. */
    public record TemplateInfo(String displayName,
                               String jrxmlPath,
                               Class<? extends AbstractReportGenerator> binderClass,
                               ReportType reportType) {
        /**
         * Returns the report type identifier used by {@link nonprofitbookkeeping.service.ReportService}.
         *
         * @return report type identifier
         */
        public String reportTypeKey() {
            return this.reportType.id();
        }
    }

    private static final Map<String, TemplateInfo> TEMPLATES = createTemplates();

    private ReportTemplates() {}

    private static Map<String, TemplateInfo> createTemplates() {
        Map<String, TemplateInfo> map = new LinkedHashMap<>();

        add(map, "jrxml/IncomeStatementAlt.jrxml", IncomeStatementAltJasperGenerator.class,
                ReportType.INCOME_STATEMENT_ALT_JASPER);
        add(map, "jrxml/AccountLedger.jrxml", AccountLedgerJasperGenerator.class,
                ReportType.ACCOUNT_LEDGER_JASPER);
        add(map, "jrxml/BankReconciliation.jrxml", BankReconciliationJasperGenerator.class,
                ReportType.BANK_RECONCILIATION_JASPER);
        add(map, "jrxml/BalanceSheet.jrxml", BalanceSheetJasperGenerator.class,
                ReportType.BALANCE_SHEET_JASPER);
        add(map, "jrxml/ChartOfAccountsAlt.jrxml", ChartOfAccountsJasperGenerator.class,
                ReportType.CHART_OF_ACCOUNTS_JASPER);
        add(map, "jrxml/ContactInfoReport.jrxml", ContactInfoJasperGenerator.class,
                ReportType.SCA_CONTACT_INFO_JASPER);
        add(map, "jrxml/GeneralLedger.jrxml", GeneralLedgerJasperGenerator.class,
                ReportType.GENERAL_LEDGER_JASPER);

        // Alternate Contact Info template used by SCA reports
        add(map, "Contact Info 1 Fixed Labeled",
                "jrxml/sca-reports/CONTACT_INFO_1_fixed_labeled.jrxml",
                ContactInfoJasperGenerator.class,
                ReportType.SCA_CONTACT_INFO_JASPER);

        add(map, "jrxml/sca-reports/ASSET_DTL_5a_ROWS.jrxml", AssetDtl5aJasperGenerator.class,
                ReportType.SCA_ASSET_DTL_5A_JASPER);
        add(map, "jrxml/sca-reports/BALANCE_3_FIXED_SEMANTIC_STRINGS_v2.jrxml", Balance3v2JasperGenerator.class,
                ReportType.SCA_BALANCE_3_V2_JASPER);
        add(map, "jrxml/sca-reports/DEPR_DTL_8_ROWS_2SECTIONS.jrxml", DeprDtl8JasperGenerator.class,
                ReportType.SCA_DEPR_DTL_8_JASPER);
        add(map, "jrxml/sca-reports/EXPENSE_DTL_12a_ROW_BASED.jrxml", ExpenseDtl12aJasperGenerator.class,
                ReportType.SCA_EXPENSE_DTL_12A_JASPER);
        add(map, "jrxml/sca-reports/EXPENSE_DTL_12b_ROW_BASED.jrxml", ExpenseDtl12bJasperGenerator.class,
                ReportType.SCA_EXPENSE_DTL_12B_JASPER);
        add(map, "jrxml/sca-reports/FundLedger.jrxml", FundLedgerJasperGenerator.class,
                ReportType.FUND_LEDGER_JASPER);
        add(map, "jrxml/sca-reports/FUNDS_14_AUTO_STYLED_labeled_rowbased.jrxml", Funds14JasperGenerator.class,
                ReportType.SCA_FUNDS_14_JASPER);
        add(map, "jrxml/sca-reports/INCOME_4_AUTO_STYLED_labeled.jrxml", Income4JasperGenerator.class,
                ReportType.SCA_INCOME_4_JASPER);
        add(map, "jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml", IncomeDtl11aJasperGenerator.class,
                ReportType.SCA_INCOME_DTL_11A_JASPER);
        add(map, "jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml", IncomeDtl11bJasperGenerator.class,
                ReportType.SCA_INCOME_DTL_11B_JASPER);
        add(map, "jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml", IncomeDtl11cJasperGenerator.class,
                ReportType.SCA_INCOME_DTL_11C_JASPER);
        add(map, "jrxml/sca-reports/INVENTORY_DTL_6_ROWS.jrxml", InventoryDtl6JasperGenerator.class,
                ReportType.SCA_INVENTORY_DTL_6_JASPER);
        add(map, "jrxml/sca-reports/Ledger_Q1.jrxml", LedgerQ1JasperGenerator.class,
                ReportType.SCA_LEDGER_Q1_JASPER);
        add(map, "jrxml/sca-reports/LIABILITY_DETAIL_5b_ROW.jrxml", LiabilityDtl5bJasperGenerator.class,
                ReportType.SCA_LIABILITY_DTL_5B_JASPER);
        add(map, "jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml", Newsletter15JasperGenerator.class,
                ReportType.SCA_NEWSLETTER_15_JASPER);
        add(map, "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml", PrimaryAccountJasperGenerator.class,
                ReportType.SCA_PRIMARY_ACCOUNT_JASPER);
        add(map, "jrxml/sca-reports/REGALIA_SALES_DTL_7_ROWS_3SECTION.jrxml", RegaliaSalesDtl7JasperGenerator.class,
                ReportType.SCA_REGALIA_SALES_DTL_7_JASPER);
        add(map, "jrxml/sca-reports/SECONDARY_ACCOUNT_2B_fixed_labeled.jrxml", SecondaryAccountJasperGenerator.class,
                ReportType.SCA_SECONDARY_ACCOUNT_JASPER);
        add(map, "jrxml/sca-reports/TRANSFER_IN_9_AUTO_STYLED_fixed_labeled_rowbased.jrxml", TransferIn9JasperGenerator.class,
                ReportType.SCA_TRANSFER_IN_9_JASPER);
        add(map, "jrxml/sca-reports/TRANSFER_OUT_10_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml", TransferOut10JasperGenerator.class,
                ReportType.SCA_TRANSFER_OUT_10_JASPER);
        add(map, "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED_labeled.jrxml", FinanceComm13JasperGenerator.class,
                ReportType.SCA_FINANCE_COMM_13_JASPER);

        return Map.copyOf(map);
    }

    private static void add(Map<String, TemplateInfo> map, String jrxmlPath,
                             Class<? extends AbstractReportGenerator> binder,
                             ReportType type) {
        String base = binder.getSimpleName().replaceFirst("JasperGenerator$", "");
        String display = toDisplayName(base);
        add(map, display, jrxmlPath, binder, type);
    }

    private static void add(Map<String, TemplateInfo> map, String displayName,
                             String jrxmlPath,
                             Class<? extends AbstractReportGenerator> binder,
                             ReportType type) {
        map.put(displayName, new TemplateInfo(displayName, jrxmlPath, binder, type));
    }

    private static String toDisplayName(String base) {
        String withSpaces = base.replaceAll("([a-z])([A-Z])", "$1 $2");
        String[] parts = withSpaces.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1).toLowerCase())
              .append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * Provides the map of available templates keyed by their display names.
     *
     * @return immutable mapping of display names to template info
     */
    public static Map<String, TemplateInfo> templates() {
        return TEMPLATES;
    }
}

