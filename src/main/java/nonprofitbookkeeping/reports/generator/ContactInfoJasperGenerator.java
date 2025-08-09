package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class ContactInfoJasperGenerator extends AbstractReportGenerator {

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
