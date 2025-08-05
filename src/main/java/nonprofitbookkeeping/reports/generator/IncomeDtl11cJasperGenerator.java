package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.IncomeDtl11cBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class IncomeDtl11cJasperGenerator extends AbstractReportGenerator {

        public IncomeDtl11cJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<IncomeDtl11cBean> getReportData() {
                return Collections.singletonList(new IncomeDtl11cBean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED.jrxml";
        }

        @Override protected String getBaseName() {
                return "IncomeDtl11c";
        }
}
