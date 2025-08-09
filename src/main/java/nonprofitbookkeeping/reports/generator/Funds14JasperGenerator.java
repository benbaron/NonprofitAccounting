package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.Funds14Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class Funds14JasperGenerator extends AbstractReportGenerator {

        public Funds14JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<Funds14Bean> getReportData() {
                return Collections.singletonList(new Funds14Bean());
        }

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
