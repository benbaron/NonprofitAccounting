package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Newsletter15Bean;
import nonprofitbookkeeping.reports.datasource.scareports.Newsletter15Row;

public class Newsletter15JasperGenerator extends AbstractReportGenerator {

    @Override
    protected Map<String, Object> getReportParameters() {
        return Collections.emptyMap();
    }

    @Override
    protected String getReportPath() {
        return "jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED.jrxml";
    }

    @Override
    public String getBaseName() {
        return "Newsletter15";
    }

    /**
     * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
     */
    @Override
    protected List<Newsletter15Bean> getReportData() {
        Newsletter15Row row = new Newsletter15Row();
        row.setAdvertiserName("Acme Widgets");
        row.setAdSize("Full");
        row.setIssuesOrVolume("Jan-Feb");
        row.setAmount(BigDecimal.ZERO);
        row.setCheckNo("123");
        row.setCheckDate("2025-01-01");

        Newsletter15Bean bean = new Newsletter15Bean();
        bean.setNewsletterName("Kingdom Chronicle");
        bean.setIssuesPerSubscription(6);
        bean.setPricePerIssue(BigDecimal.ZERO);
        bean.setPricePerSubscription(BigDecimal.ZERO);
        bean.setStartSubscriptionsDue(0);
        bean.setEndSubscriptionsDue(0);
        bean.setExpiring(0);
        bean.setRemaining(0);
        bean.setSubscriptionDue(BigDecimal.ZERO);
        bean.setGrossIncome(BigDecimal.ZERO);
        bean.setAdjustedGrossIncome(BigDecimal.ZERO);
        bean.setRows(Collections.singletonList(row));
        return Collections.singletonList(bean);
    }
}
