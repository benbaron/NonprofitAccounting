package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Funds14Bean;
import nonprofitbookkeeping.reports.datasource.scareports.Funds14Row;

public class Funds14JasperGenerator extends AbstractReportGenerator {

    @Override
    protected Map<String, Object> getReportParameters() {
        return Collections.emptyMap();
    }

    @Override
    protected String getReportPath() {
        return "jrxml/sca-reports/FUNDS_14_AUTO_STYLED.jrxml";
    }

    @Override
    public String getBaseName() {
        return "Funds14";
    }

    /**
     * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
     */
    @Override
    protected List<Funds14Bean> getReportData() {
        Funds14Row row = new Funds14Row();
        row.setFundName("General Fund");
        row.setPurpose("All non-dedicated funds");
        row.setBeginBalance(BigDecimal.ZERO);
        row.setReceipts(BigDecimal.ZERO);
        row.setDisbursements(BigDecimal.ZERO);
        row.setTransfersIn(BigDecimal.ZERO);
        row.setTransfersOut(BigDecimal.ZERO);
        row.setEndBalance(BigDecimal.ZERO);

        Funds14Bean bean = new Funds14Bean();
        bean.setRows(Collections.singletonList(row));
        return Collections.singletonList(bean);
    }
}
