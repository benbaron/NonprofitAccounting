package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class IncomeDtl11aJasperGenerator extends AbstractReportGenerator {

        public IncomeDtl11aJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED.jrxml";
        }

        @Override protected String getBaseName() {
                return "IncomeDtl11a";
        }
}
