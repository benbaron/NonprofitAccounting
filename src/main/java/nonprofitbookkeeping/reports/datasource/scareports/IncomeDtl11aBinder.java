package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the INCOME_DTL_11a report page. Handles fundraising
 * income, direct contributions, and demo/activity income.
 */
public class IncomeDtl11aBinder {
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

    // ---------- Example data builders ----------
    private static List<FundraisingIncomeInternalRow> buildInternalRows() {
        return Arrays.asList(
                new FundraisingIncomeInternalRow("Crown Tournament", "Bake sale", new BigDecimal("125.00")));
    }

    private static List<FundraisingIncomeExternalRow> buildExternalRows() {
        return Arrays.asList(
                new FundraisingIncomeExternalRow("Local Fair", "Demo booth", new BigDecimal("75.00")));
    }

    private static List<DirectContributionRow> buildContributionRows() {
        return Arrays.asList(
                new DirectContributionRow("Jane Doe", "General fund", new BigDecimal("50.00")));
    }

    private static List<DemoActivityIncomeRow> buildDemoRows() {
        return Arrays.asList(
                new DemoActivityIncomeRow("City Parade", "Fighter demo", new BigDecimal("40.00")));
    }

    // ---------- Fill & export ----------
    public static JasperPrint fillIncome11a(String jrxmlOnClasspath,
                                            String orgName,
                                            String reportTitle,
                                            Collection<FundraisingIncomeInternalRow> internalRows,
                                            Collection<FundraisingIncomeExternalRow> externalRows,
                                            Collection<DirectContributionRow> contributionRows,
                                            Collection<DemoActivityIncomeRow> demoRows) throws Exception {
        List<Income11aRow> rows = new ArrayList<>();

        if (internalRows != null) {
            for (FundraisingIncomeInternalRow r : internalRows) {
                Income11aRow row = new Income11aRow();
                row.setCategory("FUNDRAISING_INTERNAL");
                row.setFromOrPlace(r.getEvent());
                row.setActivity(r.getActivity());
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        if (externalRows != null) {
            for (FundraisingIncomeExternalRow r : externalRows) {
                Income11aRow row = new Income11aRow();
                row.setCategory("FUNDRAISING_EXTERNAL");
                row.setFromOrPlace(r.getPlace());
                row.setActivity(r.getActivity());
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        if (contributionRows != null) {
            for (DirectContributionRow r : contributionRows) {
                Income11aRow row = new Income11aRow();
                row.setCategory("DIRECT_CONTRIBUTIONS");
                row.setFromOrPlace(r.getFrom());
                row.setActivity(r.getDescription());
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        if (demoRows != null) {
            for (DemoActivityIncomeRow r : demoRows) {
                Income11aRow row = new Income11aRow();
                row.setCategory("DEMOS_ACTIVITY_FEES");
                row.setFromOrPlace(r.getFrom());
                row.setActivity(r.getActivity());
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        BigDecimal internalTotal = sum(internalRows, FundraisingIncomeInternalRow::getAmount);
        BigDecimal externalTotal = sum(externalRows, FundraisingIncomeExternalRow::getAmount);
        BigDecimal contributionTotal = sum(contributionRows, DirectContributionRow::getAmount);
        BigDecimal demoTotal = sum(demoRows, DemoActivityIncomeRow::getAmount);

        Map<String, Object> params = new HashMap<>();
        params.put("P_ORG_NAME", orgName);
        params.put("P_REPORT_TITLE", reportTitle);
        params.put("P_INTERNAL_TOTAL", internalTotal);
        params.put("P_EXTERNAL_TOTAL", externalTotal);
        params.put("P_CONTRIB_TOTAL", contributionTotal);
        params.put("P_DEMO_TOTAL", demoTotal);

        try (InputStream in = IncomeDtl11aBinder.class.getResourceAsStream(jrxmlOnClasspath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            return JasperFillManager.fillReport(report, params, ds(rows));
        }
    }

    // ---------- Demo main ----------
    public static void main(String[] args) throws Exception {
        String jrxml = "/jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml";
        JasperPrint print = fillIncome11a(jrxml,
                "Sample Org",
                "Income Detail 11a",
                buildInternalRows(),
                buildExternalRows(),
                buildContributionRows(),
                buildDemoRows());
        JasperExportManager.exportReportToPdfFile(print, "INCOME_DTL_11a.pdf");
        System.out.println("Generated PDF: INCOME_DTL_11a.pdf");
    }
}
