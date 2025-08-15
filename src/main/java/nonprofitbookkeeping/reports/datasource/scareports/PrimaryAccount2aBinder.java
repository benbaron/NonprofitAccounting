package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility class that wires together the data needed to render the
 * PRIMARY_ACCOUNT_2a report. The binder accepts collections of deposit
 * and check rows, computes the totals for each section and exposes them
 * to the JasperReports engine via parameters.
 */
public class PrimaryAccount2aBinder
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
                                {
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

        // ---------- Example data builders (replace with real wiring) ----------
        private static List<PrimaryAccountDepositRow> buildDepositRows()
        {
                List<PrimaryAccountDepositRow> list = new ArrayList<>();
                list.add(new PrimaryAccountDepositRow("2025-01-05", new BigDecimal("100.00")));
                list.add(new PrimaryAccountDepositRow("2025-01-15", new BigDecimal("250.00")));
                return list;
        }

        private static List<PrimaryAccountCheckRow> buildCheckRows()
        {
                List<PrimaryAccountCheckRow> list = new ArrayList<>();
                list.add(new PrimaryAccountCheckRow("101", "2025-01-07", new BigDecimal("75.00")));
                list.add(new PrimaryAccountCheckRow("102", "2025-01-20", new BigDecimal("150.00")));
                return list;
        }

        // ---------- Fill & export ----------
        public static JasperPrint fillPrimaryAccount2a(String jrxmlOnClasspath,
                String bankName,
                String accountTitle,
                Collection<PrimaryAccountDepositRow> depositRows,
                Collection<PrimaryAccountCheckRow> checkRows) throws Exception
        {
                Map<String, Object> params = new HashMap<>();
                params.put("P_BANK_NAME", bankName);
                params.put("P_ACCOUNT_TITLE", accountTitle);

                // Row collections
                params.put("P_DEPOSIT_ROWS", ds(depositRows));
                params.put("P_CHECK_ROWS", ds(checkRows));

                // Totals
                params.put("P_DEPOSIT_TOTAL", sum(depositRows, PrimaryAccountDepositRow::getAmount));
                params.put("P_CHECK_TOTAL", sum(checkRows, PrimaryAccountCheckRow::getAmount));

                try (InputStream in = PrimaryAccount2aBinder.class.getResourceAsStream(jrxmlOnClasspath))
                {
                        JasperReport report = JasperCompileManager.compileReport(in);
                        return JasperFillManager.fillReport(report, params, new JREmptyDataSource(1));
                }
        }

        // ---------- Demo main (compile + fill + export PDF) ----------
        public static void main(String[] args) throws Exception
        {
                String jrxml = "/jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml";
                List<PrimaryAccountDepositRow> deposits = buildDepositRows();
                List<PrimaryAccountCheckRow> checks = buildCheckRows();

                JasperPrint print = fillPrimaryAccount2a(jrxml, "First Bank", "Checking", deposits, checks);
                JasperExportManager.exportReportToPdfFile(print, "PRIMARY_ACCOUNT_2a.pdf");
                System.out.println("Generated PDF: PRIMARY_ACCOUNT_2a.pdf");
        }
}
