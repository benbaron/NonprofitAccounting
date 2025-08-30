package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class ExpenseDtl12bJasperGenerator extends AbstractReportGenerator {

        public ExpenseDtl12bJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/EXPENSE_DTL_12b_ROW_BASED.jrxml";
        }

        @Override public String getBaseName() {
                return "ExpenseDtl12b";
        }
}
