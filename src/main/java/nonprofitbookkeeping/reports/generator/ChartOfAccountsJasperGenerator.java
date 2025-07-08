package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.ChartOfAccountsRowBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generator for the Chart of Accounts report.
 */
public class ChartOfAccountsJasperGenerator extends AbstractReportGenerator {

    @Override
    protected List<ChartOfAccountsRowBean> getReportData() {
        return Collections.emptyList();
    }

    @Override
    protected Map<String, Object> getReportParameters() {
        return Collections.emptyMap();
    }

    @Override
    protected String getReportPath() {
        return "jrxml/ChartOfAccounts.jrxml";
    }

    @Override
    public File generateAndExportReport(String format) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(getReportPath())) {
            if (in == null) {
                throw new FileNotFoundException("Report template not found: " + getReportPath());
            }
            JasperReport report = JasperCompileManager.compileReport(in);
            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(getReportData());
            JasperPrint print = JasperFillManager.fillReport(report, getReportParameters(), ds);

            File outDir = new File(getOutputDirectory());
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            String base = "Chart_of_Accounts_" + LocalDate.now();
            File out = new File(outDir, base + ("html".equalsIgnoreCase(format) ? ".html" : ".pdf"));
            if ("html".equalsIgnoreCase(format)) {
                return exportToHTML(print, out.getAbsolutePath());
            }
            return exportToPDF(print, out.getAbsolutePath());
        }
    }
}
