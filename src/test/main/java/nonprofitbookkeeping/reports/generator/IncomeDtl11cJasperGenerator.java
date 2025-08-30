package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class IncomeDtl11cJasperGenerator extends AbstractReportGenerator {

        public IncomeDtl11cJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml";
        }

        @Override public String getBaseName() {
                return "IncomeDtl11c";
        }
}
