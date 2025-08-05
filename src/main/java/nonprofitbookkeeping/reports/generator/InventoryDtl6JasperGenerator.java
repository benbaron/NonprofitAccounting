package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.InventoryDtl6Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class InventoryDtl6JasperGenerator extends AbstractReportGenerator {

        public InventoryDtl6JasperGenerator(ReportContext ctx, ReportService svc) {
        }

        @Override protected List<InventoryDtl6Bean> getReportData() {
                return Collections.singletonList(new InventoryDtl6Bean());
        }

        @Override protected Map<String, Object> getReportParameters() {
                return Collections.emptyMap();
        }

        @Override protected String getReportPath() {
                return "jrxml/sca-reports/INVENTORY_DTL_6_AUTO_STYLED.jrxml";
        }

        @Override protected String getBaseName() {
                return "InventoryDtl6";
        }
}
