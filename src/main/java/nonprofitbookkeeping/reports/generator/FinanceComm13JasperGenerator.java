package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.FinanceComm13Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class FinanceComm13JasperGenerator extends AbstractReportGenerator {

        public FinanceComm13JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<FinanceComm13Bean> getReportData() {
                return Collections.singletonList(new FinanceComm13Bean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED.jrxml";
        }

        @Override protected String getBaseName() {
                return "FinanceComm13";
        }
}
