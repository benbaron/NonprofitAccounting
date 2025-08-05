package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.PrimaryAccountReconciliationBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class PrimaryAccountReconciliationJasperGenerator extends AbstractReportGenerator {

        public PrimaryAccountReconciliationJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<PrimaryAccountReconciliationBean> getReportData() {
                return Collections.singletonList(new PrimaryAccountReconciliationBean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml";
        }

        @Override protected String getBaseName() {
                return "PrimaryAccountReconciliation";
        }
}
