package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the INCOME_DTL_11b report page. Handles adjusted gross
 * event income, net advertising income and other income.
 */
public class IncomeDtl11bBinder {
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
    private static List<AdjustedGrossEventIncomeRow> buildEventRows() {
        return Arrays.asList(
                new AdjustedGrossEventIncomeRow("Coronation", "150 attendees", new BigDecimal("500.00")));
    }

    private static List<NetAdvertisingIncomeRow> buildAdvertisingRows() {
        return Arrays.asList(
                new NetAdvertisingIncomeRow("Newsletter ads", "3 issues", new BigDecimal("120.00")));
    }

    private static List<OtherIncomeRow> buildOtherRows() {
        return Arrays.asList(
                new OtherIncomeRow("Equipment rental", new BigDecimal("60.00")));
    }

    // ---------- Fill & export ----------
    public static JasperPrint fillIncome11b(String jrxmlOnClasspath,
                                            String orgName,
                                            String reportTitle,
                                            Collection<AdjustedGrossEventIncomeRow> eventRows,
                                            Collection<NetAdvertisingIncomeRow> advertisingRows,
                                            Collection<OtherIncomeRow> otherRows) throws Exception {
        List<Income11bRow> rows = new ArrayList<>();

        if (eventRows != null) {
            for (AdjustedGrossEventIncomeRow r : eventRows) {
                Income11bRow row = new Income11bRow();
                row.setCategory("ADJUSTED_GROSS_EVENT_INCOME");
                row.setDescription(r.getEventName());
                row.setItemsOrCount(r.getItemsOrCount());
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        if (advertisingRows != null) {
            for (NetAdvertisingIncomeRow r : advertisingRows) {
                Income11bRow row = new Income11bRow();
                row.setCategory("NET_ADVERTISING_INCOME");
                row.setDescription(r.getDescription());
                row.setItemsOrCount(r.getItemsOrCount());
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        if (otherRows != null) {
            for (OtherIncomeRow r : otherRows) {
                Income11bRow row = new Income11bRow();
                row.setCategory("OTHER_INCOME");
                row.setDescription(r.getDescription());
                row.setItemsOrCount("");
                row.setAmount(r.getAmount());
                rows.add(row);
            }
        }

        BigDecimal eventTotal = sum(eventRows, AdjustedGrossEventIncomeRow::getAmount);
        BigDecimal advertisingTotal = sum(advertisingRows, NetAdvertisingIncomeRow::getAmount);
        BigDecimal otherTotal = sum(otherRows, OtherIncomeRow::getAmount);

        Map<String, Object> params = new HashMap<>();
        params.put("P_ORG_NAME", orgName);
        params.put("P_REPORT_TITLE", reportTitle);
        params.put("P_EVENT_TOTAL", eventTotal);
        params.put("P_ADVERT_TOTAL", advertisingTotal);
        params.put("P_OTHER_TOTAL", otherTotal);

        try (InputStream in = IncomeDtl11bBinder.class.getResourceAsStream(jrxmlOnClasspath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            return JasperFillManager.fillReport(report, params, ds(rows));
        }
    }

    // ---------- Demo main ----------
    public static void main(String[] args) throws Exception {
        String jrxml = "/jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml";
        JasperPrint print = fillIncome11b(jrxml,
                "Sample Org",
                "Income Detail 11b",
                buildEventRows(),
                buildAdvertisingRows(),
                buildOtherRows());
        JasperExportManager.exportReportToPdfFile(print, "INCOME_DTL_11b.pdf");
        System.out.println("Generated PDF: INCOME_DTL_11b.pdf");
    }
}
