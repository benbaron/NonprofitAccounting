package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class IncomeDtl11cJasperGenerator extends AbstractReportGenerator {

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "IncomeDtl11c";
        }
}
