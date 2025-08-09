package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class Income4JasperGenerator extends AbstractReportGenerator {

        public Income4JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_4_AUTO_STYLED.jrxml";
        }

        @Override protected String getBaseName() {
                return "Income4";
        }
}
