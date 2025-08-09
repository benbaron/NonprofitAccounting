package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class Funds14JasperGenerator extends AbstractReportGenerator {

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/FUNDS_14_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "Funds14";
        }
}
