package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the LIABILITY_DTL_5b report page. The binder wires the
 * three liability sections (Deferred Revenue, Payables, and Other Liabilities)
 * into the JasperReports template and demonstrates PDF generation.
 */
public class LiabilityDtl5bBinder
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
        private static List<DeferredRevenueRow> buildDeferredRevenueRows()
        {
                List<DeferredRevenueRow> list = new ArrayList<>();
                list.add(new DeferredRevenueRow("Regional Event", new BigDecimal("100.00"), new BigDecimal("150.00")));
                list.add(new DeferredRevenueRow("Winter Gala", new BigDecimal("200.00"), new BigDecimal("50.00")));
                return list;
        }

        private static List<PayableRow> buildPayableRows()
        {
                List<PayableRow> list = new ArrayList<>();
                list.add(new PayableRow("Acme Supplies", "Site materials", new BigDecimal("75.00"), new BigDecimal("0.00")));
                list.add(new PayableRow("John Doe", "Travel reimbursement", new BigDecimal("0.00"), new BigDecimal("120.00")));
                return list;
        }

        private static List<OtherLiabilityRow> buildOtherLiabilityRows()
        {
                List<OtherLiabilityRow> list = new ArrayList<>();
                list.add(new OtherLiabilityRow("Event Insurance", "Deposit", new BigDecimal("300.00"), new BigDecimal("0.00")));
                return list;
        }

        // ---------- Fill & export ----------
        public static JasperPrint fillLiabilityDtl5b(String jrxmlOnClasspath,
                String orgName,
                String reportTitle,
                Collection<DeferredRevenueRow> deferredRows,
                Collection<PayableRow> payableRows,
                Collection<OtherLiabilityRow> otherRows) throws Exception
        {
                // Flatten rows to generic LiabilityDtl5bRow list expected by the JRXML
                List<LiabilityDtl5bRow> rows = new ArrayList<>();

                if (deferredRows != null)
                {
                        for (DeferredRevenueRow r : deferredRows)
                        {
                                LiabilityDtl5bRow row = new LiabilityDtl5bRow();
                                row.setSection("DEFERRED_REVENUE");
                                row.setItemName(r.getEventName());
                                row.setPriorAmount(r.getPriorAmount());
                                row.setCurrentAmount(r.getCurrentAmount());
                                rows.add(row);
                        }
                }

                if (payableRows != null)
                {
                        for (PayableRow r : payableRows)
                        {
                                LiabilityDtl5bRow row = new LiabilityDtl5bRow();
                                row.setSection("PAYABLES");
                                row.setItemName(r.getOwedTo());
                                row.setReason(r.getReason());
                                row.setPriorAmount(r.getPriorAmount());
                                row.setCurrentAmount(r.getCurrentAmount());
                                rows.add(row);
                        }
                }

                if (otherRows != null)
                {
                        for (OtherLiabilityRow r : otherRows)
                        {
                                LiabilityDtl5bRow row = new LiabilityDtl5bRow();
                                row.setSection("OTHER_LIABILITIES");
                                row.setItemName(r.getOwedTo());
                                row.setReason(r.getReason());
                                row.setPriorAmount(r.getPriorAmount());
                                row.setCurrentAmount(r.getCurrentAmount());
                                rows.add(row);
                        }
                }

                // Totals (may be useful to callers)
                BigDecimal deferredPriorTotal = sum(deferredRows, DeferredRevenueRow::getPriorAmount);
                BigDecimal deferredCurrentTotal = sum(deferredRows, DeferredRevenueRow::getCurrentAmount);
                BigDecimal payablePriorTotal = sum(payableRows, PayableRow::getPriorAmount);
                BigDecimal payableCurrentTotal = sum(payableRows, PayableRow::getCurrentAmount);
                BigDecimal otherPriorTotal = sum(otherRows, OtherLiabilityRow::getPriorAmount);
                BigDecimal otherCurrentTotal = sum(otherRows, OtherLiabilityRow::getCurrentAmount);

                Map<String, Object> params = new HashMap<>();
                params.put("P_ORG_NAME", orgName);
                params.put("P_REPORT_TITLE", reportTitle);
                params.put("P_CONTACT_INFO", "");
                params.put("P_FROM_PAGE_PRIMARY", "");
                params.put("P_FROM_PAGE_SECONDARY", "");

                // Totals as parameters for potential external use
                params.put("P_DEFERRED_PRIOR_TOTAL", deferredPriorTotal);
                params.put("P_DEFERRED_CURRENT_TOTAL", deferredCurrentTotal);
                params.put("P_PAYABLE_PRIOR_TOTAL", payablePriorTotal);
                params.put("P_PAYABLE_CURRENT_TOTAL", payableCurrentTotal);
                params.put("P_OTHER_PRIOR_TOTAL", otherPriorTotal);
                params.put("P_OTHER_CURRENT_TOTAL", otherCurrentTotal);

                try (InputStream in = LiabilityDtl5bBinder.class.getResourceAsStream(jrxmlOnClasspath))
                {
                        JasperReport report = JasperCompileManager.compileReport(in);
                        return JasperFillManager.fillReport(report, params, ds(rows));
                }
        }

        // ---------- Demo main (compile + fill + export PDF) ----------
        public static void main(String[] args) throws Exception
        {
                String jrxml = "/jrxml/sca-reports/LIABILITY_DETAIL_5b_ROW.jrxml";
                List<DeferredRevenueRow> deferred = buildDeferredRevenueRows();
                List<PayableRow> payables = buildPayableRows();
                List<OtherLiabilityRow> others = buildOtherLiabilityRows();

                JasperPrint print = fillLiabilityDtl5b(jrxml, "Sample Org", "Liability Detail Worksheet", deferred, payables, others);
                JasperExportManager.exportReportToPdfFile(print, "LIABILITY_DTL_5b.pdf");
                System.out.println("Generated PDF: LIABILITY_DTL_5b.pdf");
        }
}

