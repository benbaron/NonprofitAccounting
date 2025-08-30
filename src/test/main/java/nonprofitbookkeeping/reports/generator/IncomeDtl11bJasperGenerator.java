package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class IncomeDtl11bJasperGenerator extends AbstractReportGenerator {

        public IncomeDtl11bJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml";
        }

        @Override public String getBaseName() {
                return "IncomeDtl11b";
        }
}
