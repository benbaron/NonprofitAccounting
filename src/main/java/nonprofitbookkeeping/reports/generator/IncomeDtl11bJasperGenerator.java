package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.IncomeDtl11bBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class IncomeDtl11bJasperGenerator extends AbstractReportGenerator {

        public IncomeDtl11bJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<IncomeDtl11bBean> getReportData() {
                return Collections.singletonList(new IncomeDtl11bBean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "IncomeDtl11b";
        }
}
