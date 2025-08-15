package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the EXPENSE_DTL_12a report page. Handles advertising,
 * bad debt, and fee & honoraria sections and supplies the appropriate
 * parameters and data sources to the JasperReports template.
 */
public class ExpenseDtl12aBinder {
    // ---------- Helpers ----------
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

    // ---------- Example data builders (replace with real wiring) ----------
    private static List<AdvertisingExpenseRow> buildAdvertisingRows() {
        return Arrays.asList(
                new AdvertisingExpenseRow("A1", "SCA Times", new BigDecimal("125.00")),
                new AdvertisingExpenseRow("A2", "Local Herald", new BigDecimal("60.00")));
    }

    private static List<BadDebtRow> buildBadDebtRows() {
        return Arrays.asList(
                new BadDebtRow("B1", "John Smith", "Uncollectible site fee", new BigDecimal("25.00")));
    }

    private static List<FeeHonorariumRow> buildFeeRows() {
        return Arrays.asList(
                new FeeHonorariumRow("F1", "Jane Doe", "Musician for court", new BigDecimal("150.00")));
    }

    // ---------- Fill & export ----------
    public static JasperPrint fillExpenseDtl12a(String jrxmlOnClasspath,
                                                String orgName,
                                                String reportTitle,
                                                Collection<AdvertisingExpenseRow> advertisingRows,
                                                Collection<BadDebtRow> badDebtRows,
                                                Collection<FeeHonorariumRow> feeRows) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("P_ORG_NAME", orgName);
        params.put("P_REPORT_TITLE", reportTitle);
        params.put("P_EXP12_ADVERTISING_ROWS", ds(advertisingRows));
        params.put("P_EXP13_BADDEBTS_ROWS", ds(badDebtRows));
        params.put("P_EXP17_FEES_ROWS", ds(feeRows));

        params.put("P_TOTAL_12", sum(advertisingRows, AdvertisingExpenseRow::getAmount));
        params.put("P_TOTAL_13", sum(badDebtRows, BadDebtRow::getAmount));
        params.put("P_TOTAL_17", sum(feeRows, FeeHonorariumRow::getAmount));

        try (InputStream in = ExpenseDtl12aBinder.class.getResourceAsStream(jrxmlOnClasspath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            // Top-level detail band is empty; use a single-row data source
            return JasperFillManager.fillReport(report, params, new JREmptyDataSource(1));
        }
    }

    // ---------- Demo main (compile + fill + export PDF) ----------
    public static void main(String[] args) throws Exception {
        String jrxml = "/reports/EXPENSE_DTL_12a_ROW_BASED.jrxml";
        JasperPrint print = fillExpenseDtl12a(jrxml,
                "Your Group, Inc.",
                "Expense Detail (Part 1)",
                buildAdvertisingRows(),
                buildBadDebtRows(),
                buildFeeRows());
        JasperExportManager.exportReportToPdfFile(print, "EXPENSE_DTL_12a_ROW_BASED.pdf");
        System.out.println("Generated PDF: EXPENSE_DTL_12a_ROW_BASED.pdf");
    }
}
