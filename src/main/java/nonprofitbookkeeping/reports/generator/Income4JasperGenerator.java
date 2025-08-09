package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class Income4JasperGenerator extends AbstractReportGenerator {

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_4_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "Income4";
        }
}
