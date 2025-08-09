package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class ExpenseDtl12bJasperGenerator extends AbstractReportGenerator {

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/EXPENSE_DTL_12b_AUTO_STYLED.jrxml";
        }

        @Override public String getBaseName() {
                return "ExpenseDtl12b";
        }
}
