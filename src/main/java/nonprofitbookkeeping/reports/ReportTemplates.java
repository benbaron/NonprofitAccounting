package nonprofitbookkeeping.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Registry of available Jasper report templates.
 * Each entry maps a user facing display name to the JRXML template
 * and the generator class responsible for binding data.
 */
public final class ReportTemplates
{
        /** Immutable holder for template metadata. */
        public record TemplateInfo(String displayName,
                String jrxmlPath,
                String generatorClassName,
                ReportType reportType)
        {
                /**
                 * Returns the report type identifier used by {@link nonprofitbookkeeping.service.ReportService}.
                 *
                 * @return report type identifier
                 */
                public String reportTypeKey()
                {
                        return this.reportType.id();

                }
        }

        private static final Map<String, TemplateInfo> TEMPLATES = createTemplates();

        private ReportTemplates()
        {
        }

        private static Map<String, TemplateInfo> createTemplates()
        {
                Map<String, TemplateInfo> map = new LinkedHashMap<>();

                add(map, "jrxml/IncomeStatementAlt.jrxml",
                        generatorClass("IncomeStatementAltJasperGenerator"),
                        ReportType.INCOME_STATEMENT_ALT_JASPER);
                add(map, "jrxml/AccountLedger.jrxml",
                        generatorClass("AccountLedgerJasperGenerator"),
                        ReportType.ACCOUNT_LEDGER_JASPER);
                add(map, "jrxml/BankReconciliation.jrxml",
                        generatorClass("BankReconciliationJasperGenerator"),
                        ReportType.BANK_RECONCILIATION_JASPER);
                add(map, "jrxml/BalanceSheet.jrxml",
                        generatorClass("BalanceSheetJasperGenerator"),
                        ReportType.BALANCE_SHEET_JASPER);
                add(map, "jrxml/ChartOfAccountsAlt.jrxml",
                        generatorClass("ChartOfAccountsJasperGenerator"),
                        ReportType.CHART_OF_ACCOUNTS_JASPER);
                add(map, "jrxml/ContactInfoReport.jrxml",
                        generatorClass("ContactInfoJasperGenerator"),
                        ReportType.SCA_CONTACT_INFO_JASPER);
                add(map, "jrxml/GeneralLedger.jrxml",
                        generatorClass("GeneralLedgerJasperGenerator"),
                        ReportType.GENERAL_LEDGER_JASPER);

                // Alternate Contact Info template used by SCA reports
                add(map, "Contact Info 1 Fixed Labeled",
                        "jrxml/sca-reports/CONTACT_INFO_1_fixed_labeled.jrxml",
                        generatorClass("ContactInfoJasperGenerator"),
                        ReportType.SCA_CONTACT_INFO_JASPER);

                add(map, "jrxml/sca-reports/ASSET_DTL_5a_ROWS.jrxml",
                        generatorClass("AssetDtl5aJasperGenerator"),
                        ReportType.SCA_ASSET_DTL_5A_JASPER);
                add(map, "jrxml/sca-reports/BALANCE_3_FIXED_SEMANTIC_STRINGS_v2.jrxml",
                        generatorClass("Balance3v2JasperGenerator"),
                        ReportType.SCA_BALANCE_3_V2_JASPER);
                add(map, "jrxml/sca-reports/DEPR_DTL_8_ROWS_2SECTIONS.jrxml",
                        generatorClass("DeprDtl8JasperGenerator"),
                        ReportType.SCA_DEPR_DTL_8_JASPER);
                add(map, "jrxml/sca-reports/EXPENSE_DTL_12a_ROW_BASED.jrxml",
                        generatorClass("ExpenseDtl12aJasperGenerator"),
                        ReportType.SCA_EXPENSE_DTL_12A_JASPER);
                add(map, "jrxml/sca-reports/EXPENSE_DTL_12b_ROW_BASED.jrxml",
                        generatorClass("ExpenseDtl12bJasperGenerator"),
                        ReportType.SCA_EXPENSE_DTL_12B_JASPER);
                add(map, "jrxml/sca-reports/FundLedger.jrxml",
                        generatorClass("FundLedgerJasperGenerator"),
                        ReportType.FUND_LEDGER_JASPER);
                add(map, "jrxml/sca-reports/FUNDS_14_AUTO_STYLED_labeled_rowbased.jrxml",
                        generatorClass("Funds14JasperGenerator"),
                        ReportType.SCA_FUNDS_14_JASPER);
                add(map, "jrxml/sca-reports/INCOME_4_AUTO_STYLED_labeled.jrxml",
                        generatorClass("Income4JasperGenerator"),
                        ReportType.SCA_INCOME_4_JASPER);
                add(map, "jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml",
                        generatorClass("IncomeDtl11aJasperGenerator"),
                        ReportType.SCA_INCOME_DTL_11A_JASPER);
                add(map, "jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml",
                        generatorClass("IncomeDtl11bJasperGenerator"),
                        ReportType.SCA_INCOME_DTL_11B_JASPER);
                add(map, "jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml",
                        generatorClass("IncomeDtl11cJasperGenerator"),
                        ReportType.SCA_INCOME_DTL_11C_JASPER);
                add(map, "jrxml/sca-reports/INVENTORY_DTL_6_ROWS.jrxml",
                        generatorClass("InventoryDtl6JasperGenerator"),
                        ReportType.SCA_INVENTORY_DTL_6_JASPER);
                add(map, "jrxml/sca-reports/Ledger_Q1.jrxml",
                        generatorClass("LedgerQ1JasperGenerator"),
                        ReportType.SCA_LEDGER_Q1_JASPER);
                add(map, "jrxml/sca-reports/LIABILITY_DETAIL_5b_ROW.jrxml",
                        generatorClass("LiabilityDtl5bJasperGenerator"),
                        ReportType.SCA_LIABILITY_DTL_5B_JASPER);
                add(map,
                        "jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml",
                        generatorClass("Newsletter15JasperGenerator"),
                        ReportType.SCA_NEWSLETTER_15_JASPER);
                add(map, "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml",
                        generatorClass("PrimaryAccountJasperGenerator"),
                        ReportType.SCA_PRIMARY_ACCOUNT_JASPER);
                add(map, "jrxml/sca-reports/REGALIA_SALES_DTL_7_ROWS_3SECTION.jrxml",
                        generatorClass("RegaliaSalesDtl7JasperGenerator"),
                        ReportType.SCA_REGALIA_SALES_DTL_7_JASPER);
                add(map, "jrxml/sca-reports/SECONDARY_ACCOUNT_2B_fixed_labeled.jrxml",
                        generatorClass("SecondaryAccountJasperGenerator"),
                        ReportType.SCA_SECONDARY_ACCOUNT_JASPER);
                add(map,
                        "jrxml/sca-reports/TRANSFER_IN_9_AUTO_STYLED_fixed_labeled_rowbased.jrxml",
                        generatorClass("TransferIn9JasperGenerator"),
                        ReportType.SCA_TRANSFER_IN_9_JASPER);
                add(map,
                        "jrxml/sca-reports/TRANSFER_OUT_10_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml",
                        generatorClass("TransferOut10JasperGenerator"),
                        ReportType.SCA_TRANSFER_OUT_10_JASPER);
                add(map, "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED_labeled.jrxml",
                        generatorClass("FinanceComm13JasperGenerator"),
                        ReportType.SCA_FINANCE_COMM_13_JASPER);

                return Map.copyOf(map);
        }

        private static void add(Map<String, TemplateInfo> map, String jrxmlPath,
                String generatorClassName,
                ReportType type)
        {
                String base = simpleName(generatorClassName)
                        .replaceFirst("JasperGenerator$", "");
                String display = toDisplayName(base);
                add(map, display, jrxmlPath, generatorClassName, type);

        }

        private static void add(Map<String, TemplateInfo> map, String displayName,
                String jrxmlPath,
                String generatorClassName,
                ReportType type)
        {
                map.put(displayName,
                        new TemplateInfo(displayName, jrxmlPath, generatorClassName, type));

        }

        private static String simpleName(String className)
        {
                int lastDot = className.lastIndexOf('.');
                return lastDot >= 0 ? className.substring(lastDot + 1) : className;

        }

        private static String generatorClass(String simpleName)
        {
                return "nonprofitbookkeeping.reports.jasper." + simpleName;

        }

        private static String toDisplayName(String base)
        {
                String withSpaces = base.replaceAll("([a-z])([A-Z])", "$1 $2");
                String[] parts = withSpaces.split("\\s+");
                StringBuilder sb = new StringBuilder();

                for (String part : parts)
                {
                        if (part.isEmpty())
                        {
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
        public static Map<String, TemplateInfo> templates()
        {
                return TEMPLATES;

        }

}
