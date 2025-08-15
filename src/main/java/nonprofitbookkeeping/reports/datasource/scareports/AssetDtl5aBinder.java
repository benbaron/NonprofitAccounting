package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the ASSET_DTL_5a report page. The binder wires the
 * four asset sections (Undeposited Funds, Receivables, Prepaid Expenses,
 * and Other Assets) into the JasperReports template and computes the
 * totals for each section.
 */
public class AssetDtl5aBinder
{
        // ---------- Helpers ----------
        private static <T> BigDecimal sum(Collection<T> rows, Function<T, BigDecimal> getter)
        {
                BigDecimal total = BigDecimal.ZERO;

                if (rows != null)
                {
                        for (T r : rows)
                        {
                                BigDecimal v = getter.apply(r);
                                if (v != null)
                                        total = total.add(v);
                        }
                }

                return total;
        }

        private static JRBeanCollectionDataSource ds(Collection<?> rows)
        {
                return new JRBeanCollectionDataSource(rows == null ? Collections.emptyList() : rows, false);
        }

        // ---------- Example data builders ----------
        private static List<UndepositedFundsRow> buildUndepositedFundRows()
        {
                List<UndepositedFundsRow> list = new ArrayList<>();
                list.add(new UndepositedFundsRow("Event gate cash", new BigDecimal("50.00")));
                list.add(new UndepositedFundsRow("Fundraiser proceeds", new BigDecimal("125.00")));
                return list;
        }

        private static List<ReceivableRow> buildReceivableRows()
        {
                List<ReceivableRow> list = new ArrayList<>();
                list.add(new ReceivableRow("John Doe", "Site advance", new BigDecimal("100.00"), new BigDecimal("0.00")));
                list.add(new ReceivableRow("Jane Smith", "Reimbursement", new BigDecimal("0.00"), new BigDecimal("75.00")));
                return list;
        }

        private static List<PrepaidExpenseRow> buildPrepaidExpenseRows()
        {
                List<PrepaidExpenseRow> list = new ArrayList<>();
                list.add(new PrepaidExpenseRow("Site deposit", new BigDecimal("200.00"), new BigDecimal("0.00")));
                list.add(new PrepaidExpenseRow("Storage rent", new BigDecimal("50.00"), new BigDecimal("50.00")));
                return list;
        }

        private static List<OtherAssetRow> buildOtherAssetRows()
        {
                List<OtherAssetRow> list = new ArrayList<>();
                list.add(new OtherAssetRow("Security deposit", new BigDecimal("0.00"), new BigDecimal("500.00")));
                return list;
        }

        // ---------- Fill & export ----------
        public static JasperPrint fillAssetDtl5a(String jrxmlOnClasspath,
                String orgName,
                String reportTitle,
                Collection<UndepositedFundsRow> undepositedRows,
                Collection<ReceivableRow> receivableRows,
                Collection<PrepaidExpenseRow> prepaidRows,
                Collection<OtherAssetRow> otherAssetRows) throws Exception
        {
                // Totals for each section
                BigDecimal undepositedTotal = sum(undepositedRows, UndepositedFundsRow::getAmount);
                BigDecimal receivablePriorTotal = sum(receivableRows, ReceivableRow::getPriorAmount);
                BigDecimal receivableCurrentTotal = sum(receivableRows, ReceivableRow::getCurrentAmount);
                BigDecimal prepaidPriorTotal = sum(prepaidRows, PrepaidExpenseRow::getPriorAmount);
                BigDecimal prepaidCurrentTotal = sum(prepaidRows, PrepaidExpenseRow::getCurrentAmount);
                BigDecimal otherPriorTotal = sum(otherAssetRows, OtherAssetRow::getPriorAmount);
                BigDecimal otherCurrentTotal = sum(otherAssetRows, OtherAssetRow::getCurrentAmount);

                // Build summary rows for the main table
                List<AssetDtl5aRow> summaryRows = new ArrayList<>();

                AssetDtl5aRow undepRow = new AssetDtl5aRow();
                undepRow.setLabel("Undeposited Funds");
                undepRow.setEnd(undepositedTotal);
                summaryRows.add(undepRow);

                AssetDtl5aRow recRow = new AssetDtl5aRow();
                recRow.setLabel("Receivables");
                recRow.setStart(receivablePriorTotal);
                recRow.setEnd(receivableCurrentTotal);
                recRow.setDiff(receivableCurrentTotal.subtract(receivablePriorTotal));
                summaryRows.add(recRow);

                AssetDtl5aRow preRow = new AssetDtl5aRow();
                preRow.setLabel("Prepaid Expenses");
                preRow.setStart(prepaidPriorTotal);
                preRow.setEnd(prepaidCurrentTotal);
                preRow.setDiff(prepaidCurrentTotal.subtract(prepaidPriorTotal));
                summaryRows.add(preRow);

                AssetDtl5aRow otherRow = new AssetDtl5aRow();
                otherRow.setLabel("Other Assets");
                otherRow.setStart(otherPriorTotal);
                otherRow.setEnd(otherCurrentTotal);
                otherRow.setDiff(otherCurrentTotal.subtract(otherPriorTotal));
                summaryRows.add(otherRow);

                Map<String, Object> params = new HashMap<>();
                params.put("P_ORG_NAME", orgName);
                params.put("P_REPORT_TITLE", reportTitle);
                params.put("P_REPORT_SUBTITLE", "");
                params.put("P_CONTACT_INFO", "");
                params.put("P_FROM_PAGE_PRIMARY", "");
                params.put("P_FROM_PAGE_SECONDARY", "");

                // Row collections for potential subreports
                params.put("P_UNDEPOSITED_FUNDS_ROWS", ds(undepositedRows));
                params.put("P_RECEIVABLE_ROWS", ds(receivableRows));
                params.put("P_PREPAID_EXPENSE_ROWS", ds(prepaidRows));
                params.put("P_OTHER_ASSET_ROWS", ds(otherAssetRows));

                // Totals as parameters
                params.put("P_UNDEPOSITED_FUNDS_TOTAL", undepositedTotal);
                params.put("P_RECEIVABLE_PRIOR_TOTAL", receivablePriorTotal);
                params.put("P_RECEIVABLE_CURRENT_TOTAL", receivableCurrentTotal);
                params.put("P_PREPAID_PRIOR_TOTAL", prepaidPriorTotal);
                params.put("P_PREPAID_CURRENT_TOTAL", prepaidCurrentTotal);
                params.put("P_OTHER_PRIOR_TOTAL", otherPriorTotal);
                params.put("P_OTHER_CURRENT_TOTAL", otherCurrentTotal);

                try (InputStream in = AssetDtl5aBinder.class.getResourceAsStream(jrxmlOnClasspath))
                {
                        JasperReport report = JasperCompileManager.compileReport(in);
                        return JasperFillManager.fillReport(report, params, ds(summaryRows));
                }
        }

        // ---------- Demo main (compile + fill + export PDF) ----------
        public static void main(String[] args) throws Exception
        {
                String jrxml = "/jrxml/sca-reports/ASSET_DTL_5a_ROWS.jrxml";
                List<UndepositedFundsRow> undep = buildUndepositedFundRows();
                List<ReceivableRow> recs = buildReceivableRows();
                List<PrepaidExpenseRow> prepaids = buildPrepaidExpenseRows();
                List<OtherAssetRow> others = buildOtherAssetRows();

                JasperPrint print = fillAssetDtl5a(jrxml, "Sample Org", "Asset Detail Worksheet", undep, recs, prepaids, others);
                JasperExportManager.exportReportToPdfFile(print, "ASSET_DTL_5a.pdf");
                System.out.println("Generated PDF: ASSET_DTL_5a.pdf");
        }
}
