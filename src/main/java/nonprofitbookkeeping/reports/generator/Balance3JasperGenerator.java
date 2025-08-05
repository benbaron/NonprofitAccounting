package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.Balance3Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class Balance3JasperGenerator extends AbstractReportGenerator {

        public Balance3JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<Balance3Bean> getReportData() {
                return Collections.singletonList(new Balance3Bean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/BALANCE_3.jrxml";
        }

        @Override protected String getBaseName() {
                return "Balance3";
        }
}
