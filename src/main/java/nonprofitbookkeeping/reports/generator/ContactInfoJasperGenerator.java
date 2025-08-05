package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.ContactInfoBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class ContactInfoJasperGenerator extends AbstractReportGenerator {

        public ContactInfoJasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<ContactInfoBean> getReportData() {
                return Collections.singletonList(new ContactInfoBean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/CONTACT_INFO_1.jrxml";
        }

        @Override protected String getBaseName() {
                return "ContactInfo";
        }
}
