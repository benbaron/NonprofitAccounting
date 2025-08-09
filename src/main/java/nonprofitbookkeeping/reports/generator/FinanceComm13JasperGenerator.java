package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class FinanceComm13JasperGenerator extends AbstractReportGenerator {

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "FinanceComm13";
        }
}
