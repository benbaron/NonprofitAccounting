package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.Income4Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class Income4JasperGenerator extends AbstractReportGenerator {

        public Income4JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<Income4Bean> getReportData() {
                return Collections.singletonList(new Income4Bean());
        }

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
