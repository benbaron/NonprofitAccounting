package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Utility binder for the DEPR_DTL_8 report page. The binder accepts a
 * collection of {@link DepreciationRow} beans, computes totals for each
 * schedule (5-year and 7-year) and exposes them along with the row data to
 * the JasperReports engine.
 */
public class DeprDtl8Binder
{
        // ---------- Helpers ----------
        private static BigDecimal sumForSchedule(Collection<DepreciationRow> rows, String schedule)
        {
                BigDecimal total = BigDecimal.ZERO;

                if (rows != null)
                {
                        for (DepreciationRow r : rows)
                        {
                                if (schedule == null || schedule.equals(r.getSchedule()))
                                {
                                        BigDecimal v = r.getDepreciationThisPeriod();
                                        if (v != null)
                                                total = total.add(v);
                                }
                        }
                }

                return total;
        }

        private static JRBeanCollectionDataSource ds(Collection<?> rows)
        {
                return new JRBeanCollectionDataSource(rows == null ? Collections.emptyList() : rows, false);
        }

        // ---------- Example data builders ----------
        private static List<DepreciationRow> buildRows()
        {
                List<DepreciationRow> list = new ArrayList<>();
                list.add(new DepreciationRow("FIVE_YEAR", "Office Printer", "2023-01-15",
                                new BigDecimal("500.00"), new BigDecimal("100.00"), new BigDecimal("50.00"),
                                new BigDecimal("150.00"), new BigDecimal("350.00"), ""));
                list.add(new DepreciationRow("SEVEN_YEAR", "Work Van", "2022-06-01",
                                new BigDecimal("20000.00"), new BigDecimal("4000.00"), new BigDecimal("2857.14"),
                                new BigDecimal("6857.14"), new BigDecimal("13142.86"), ""));
                return list;
        }

        // ---------- Fill & export ----------
        public static JasperPrint fillDeprDtl8(String jrxmlOnClasspath,
                String orgName,
                String reportTitle,
                Collection<DepreciationRow> rows) throws Exception
        {
                BigDecimal fiveYearTotal = sumForSchedule(rows, "FIVE_YEAR");
                BigDecimal sevenYearTotal = sumForSchedule(rows, "SEVEN_YEAR");
                BigDecimal grandTotal = fiveYearTotal.add(sevenYearTotal);

                Map<String, Object> params = new HashMap<>();
                params.put("P_ORG_NAME", orgName);
                params.put("P_REPORT_TITLE", reportTitle);
                params.put("P_FIVE_YEAR_TOTAL", fiveYearTotal);
                params.put("P_SEVEN_YEAR_TOTAL", sevenYearTotal);
                params.put("P_GRAND_TOTAL", grandTotal);

                try (InputStream in = DeprDtl8Binder.class.getResourceAsStream(jrxmlOnClasspath))
                {
                        JasperReport report = JasperCompileManager.compileReport(in);
                        return JasperFillManager.fillReport(report, params, ds(rows));
                }
        }

        // ---------- Demo main (compile + fill + export PDF) ----------
        public static void main(String[] args) throws Exception
        {
                String jrxml = "/jrxml/sca-reports/DEPR_DTL_8_ROWS_2SECTIONS.jrxml";
                List<DepreciationRow> rows = buildRows();

                JasperPrint print = fillDeprDtl8(jrxml, "Sample Org", "Depreciation Detail Worksheet", rows);
                JasperExportManager.exportReportToPdfFile(print, "DEPR_DTL_8.pdf");
                System.out.println("Generated PDF: DEPR_DTL_8.pdf");
        }
}

