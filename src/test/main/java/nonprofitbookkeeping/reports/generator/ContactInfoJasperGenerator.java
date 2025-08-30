package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class ContactInfoJasperGenerator extends AbstractReportGenerator {

        public ContactInfoJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/CONTACT_INFO_1_fixed_labeled.jrxml";
        }

        @Override public String getBaseName() {
                return "ContactInfo";
        }
}
