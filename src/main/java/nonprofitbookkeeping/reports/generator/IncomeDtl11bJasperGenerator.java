package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class IncomeDtl11bJasperGenerator extends AbstractReportGenerator {

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED.jrxml";
        }

        @Override protected String getBaseName() {
                return "IncomeDtl11b";
        }
}
