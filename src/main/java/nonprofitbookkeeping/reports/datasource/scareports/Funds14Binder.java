package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Binder for the FUNDS_14 report page. Accepts a collection of
 * {@link Funds14Row} entries and exposes them along with per-column totals
 * to the JasperReports template.
 */
public class Funds14Binder {
    private static <T> BigDecimal sum(Collection<T> rows, Function<T, BigDecimal> getter) {
        BigDecimal total = BigDecimal.ZERO;
        if (rows != null) {
            for (T r : rows) {
                BigDecimal v = getter.apply(r);
                if (v != null) {
                    total = total.add(v);
                }
            }
        }
        return total;
    }

    private static JRBeanCollectionDataSource ds(Collection<?> rows) {
        return new JRBeanCollectionDataSource(rows == null ? Collections.emptyList() : rows, false);
    }

    // Example data builder
    private static List<Funds14Row> buildRows() {
        List<Funds14Row> list = new ArrayList<>();
        list.add(new Funds14Row("General Fund",
                "All non-dedicated funds",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"),
                new BigDecimal("25.00"),
                new BigDecimal("1075.00")));
        return list;
    }

    public static JasperPrint fillFunds14(String jrxmlOnClasspath,
                                          String orgName,
                                          String reportTitle,
                                          Collection<Funds14Row> rows) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("P_ORG_NAME", orgName);
        params.put("P_REPORT_TITLE", reportTitle);
        params.put("P_FUNDS_ROWS", ds(rows));
        params.put("P_TOTAL_BEGIN", sum(rows, Funds14Row::getBeginBalance));
        params.put("P_TOTAL_RECEIPTS", sum(rows, Funds14Row::getReceipts));
        params.put("P_TOTAL_DISBURSEMENTS", sum(rows, Funds14Row::getDisbursements));
        params.put("P_TOTAL_TRANSFERS_IN", sum(rows, Funds14Row::getTransfersIn));
        params.put("P_TOTAL_TRANSFERS_OUT", sum(rows, Funds14Row::getTransfersOut));
        params.put("P_TOTAL_END", sum(rows, Funds14Row::getEndBalance));

        try (InputStream in = Funds14Binder.class.getResourceAsStream(jrxmlOnClasspath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            return JasperFillManager.fillReport(report, params, new JREmptyDataSource(1));
        }
    }

    // Demo main
    public static void main(String[] args) throws Exception {
        String jrxml = "/jrxml/sca-reports/FUNDS_14_AUTO_STYLED_labeled_rowbased.jrxml";
        JasperPrint print = fillFunds14(jrxml,
                "Your Group, Inc.",
                "Funds 14",
                buildRows());
        JasperExportManager.exportReportToPdfFile(print, "FUNDS_14_ROW_BASED.pdf");
        System.out.println("Generated PDF: FUNDS_14_ROW_BASED.pdf");
    }
}
