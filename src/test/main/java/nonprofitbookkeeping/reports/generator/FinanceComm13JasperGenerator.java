package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class FinanceComm13JasperGenerator extends AbstractReportGenerator {

        public FinanceComm13JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "FinanceComm13";
        }
}
