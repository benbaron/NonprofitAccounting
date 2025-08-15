package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Binder for the NEWSLETTER_15 report page. Handles the single advertising
 * table and exposes numerous scalar parameters describing the newsletter
 * subscription statistics.
 */
public class Newsletter15Binder {
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
    private static Newsletter15Bean buildBean() {
        Newsletter15Bean bean = new Newsletter15Bean();
        bean.setNewsletterName("Kingdom Chronicle");
        bean.setIssuesPerSubscription(6);
        bean.setPricePerIssue(new BigDecimal("2.00"));
        bean.setPricePerSubscription(new BigDecimal("12.00"));
        bean.setStartSubscriptionsDue(10);
        bean.setEndSubscriptionsDue(15);
        bean.setExpiring(2);
        bean.setRemaining(8);
        bean.setSubscriptionDue(new BigDecimal("30.00"));
        bean.setGrossIncome(new BigDecimal("50.00"));
        bean.setAdjustedGrossIncome(new BigDecimal("45.00"));
        bean.getRows().add(new Newsletter15Row("Acme Widgets", "Full", "Jan-Feb", new BigDecimal("50.00"), "123", "2025-01-01"));
        return bean;
    }

    public static JasperPrint fillNewsletter15(String jrxmlOnClasspath,
                                               String orgName,
                                               String reportTitle,
                                               Newsletter15Bean bean) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("P_ORG_NAME", orgName);
        params.put("P_REPORT_TITLE", reportTitle);
        params.put("P_NEWSLETTER_NAME", bean.getNewsletterName());
        params.put("P_ISSUES_PER_SUB", bean.getIssuesPerSubscription());
        params.put("P_PRICE_PER_ISSUE", bean.getPricePerIssue());
        params.put("P_PRICE_PER_SUB", bean.getPricePerSubscription());
        params.put("P_START_SUBS_DUE", bean.getStartSubscriptionsDue());
        params.put("P_END_SUBS_DUE", bean.getEndSubscriptionsDue());
        params.put("P_EXPIRING", bean.getExpiring());
        params.put("P_REMAINING", bean.getRemaining());
        params.put("P_SUBSCRIPTION_DUE", bean.getSubscriptionDue());
        params.put("P_GROSS_INCOME", bean.getGrossIncome());
        params.put("P_ADJ_GROSS_INCOME", bean.getAdjustedGrossIncome());
        params.put("P_AD_ROWS", ds(bean.getRows()));
        params.put("P_TOTAL_ADS", sum(bean.getRows(), Newsletter15Row::getAmount));

        try (InputStream in = Newsletter15Binder.class.getResourceAsStream(jrxmlOnClasspath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            return JasperFillManager.fillReport(report, params, new JREmptyDataSource(1));
        }
    }

    // Demo main
    public static void main(String[] args) throws Exception {
        String jrxml = "/jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml";
        Newsletter15Bean bean = buildBean();
        JasperPrint print = fillNewsletter15(jrxml,
                "Your Group, Inc.",
                "Newsletter 15",
                bean);
        JasperExportManager.exportReportToPdfFile(print, "NEWSLETTER_15_ROW_BASED.pdf");
        System.out.println("Generated PDF: NEWSLETTER_15_ROW_BASED.pdf");
    }
}
