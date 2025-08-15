package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the EXPENSE_DTL_12b report page. Wires insurance,
 * other expense, and donation sections into the JasperReports template and
 * calculates subtotals for each section.
 */
public class ExpenseDtl12bBinder {
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
    private static List<InsuranceExpenseRow> buildInsuranceRows() {
        return Arrays.asList(
                new InsuranceExpenseRow("COI – Park Rental", "1001", "2025-02-01", new BigDecimal("225.00")));
    }

    private static List<OtherExpenseRow> buildOtherRows() {
        return Arrays.asList(
                new OtherExpenseRow("Storage Unit", "February rent", new BigDecimal("89.99")));
    }

    private static List<DonationRow> buildDonationRows() {
        return Arrays.asList(
                new DonationRow("501(c)(3) Partner", "123456", "Event proceeds donation", "2001", "2025-03-01", new BigDecimal("200.00")));
    }

    // ---------- Fill & export ----------
    public static JasperPrint fillExpenseDtl12b(String jrxmlOnClasspath,
                                                String orgName,
                                                String reportTitle,
                                                Collection<InsuranceExpenseRow> insuranceRows,
                                                Collection<OtherExpenseRow> otherRows,
                                                Collection<DonationRow> donationRows) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("P_ORG_NAME", orgName);
        params.put("P_REPORT_TITLE", reportTitle);
        params.put("P_EXP20_INSURANCE_ROWS", ds(insuranceRows));
        params.put("P_EXP28_OTHER_ROWS", ds(otherRows));
        params.put("P_EXP29_DONATION_ROWS", ds(donationRows));

        params.put("P_TOTAL_20", sum(insuranceRows, InsuranceExpenseRow::getAmount));
        params.put("P_TOTAL_28", sum(otherRows, OtherExpenseRow::getAmount));
        params.put("P_TOTAL_29", sum(donationRows, DonationRow::getAmount));

        try (InputStream in = ExpenseDtl12bBinder.class.getResourceAsStream(jrxmlOnClasspath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            return JasperFillManager.fillReport(report, params, new JREmptyDataSource(1));
        }
    }

    // ---------- Demo main (compile + fill + export PDF) ----------
    public static void main(String[] args) throws Exception {
        String jrxml = "/reports/EXPENSE_DTL_12b_ROW_BASED.jrxml";
        JasperPrint print = fillExpenseDtl12b(jrxml,
                "Your Group, Inc.",
                "Expense Detail (Part 2)",
                buildInsuranceRows(),
                buildOtherRows(),
                buildDonationRows());
        JasperExportManager.exportReportToPdfFile(print, "EXPENSE_DTL_12b_ROW_BASED.pdf");
        System.out.println("Generated PDF: EXPENSE_DTL_12b_ROW_BASED.pdf");
    }
}
