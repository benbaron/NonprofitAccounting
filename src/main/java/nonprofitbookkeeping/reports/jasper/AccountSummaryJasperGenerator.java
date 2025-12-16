package nonprofitbookkeeping.reports.jasper;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.AccountSummaryRowBean;
import nonprofitbookkeeping.reports.runtime.AccountSummaryDataProvider;
import nonprofitbookkeeping.reports.runtime.JdbcBeanLoader;

/**
 * Jasper generator matching a metadata entry such as:
 *
 *   generatorClass=nonprofitbookkeeping.reports.jasper.AccountSummaryJasperGenerator
 *   template=AccountSummary.jrxml
 *   beanClass=nonprofitbookkeeping.reports.datasource.AccountSummaryRowBean
 *
 * Integration with ReportContext:
 *   - If ctx.getBeans() is non-null and non-empty, those beans are used directly
 *     as the Jasper data source (no DB query).
 *   - Otherwise, this class uses AccountSummaryDataProvider + JdbcBeanLoader
 *     to query the database and build AccountSummaryRowBean instances.
 */
public class AccountSummaryJasperGenerator {

    private final DataSource dataSource;
    private final String templateResourcePath;
    private final AccountSummaryDataProvider provider;

    /**
     * @param dataSource           JDBC data source (H2, PostgreSQL, etc.).
     * @param templateResourcePath Classpath resource path to JRXML template,
     *                             e.g. "/reports/AccountSummary.jrxml".
     */
    public AccountSummaryJasperGenerator(
        DataSource dataSource,
        String templateResourcePath
    ) {
        this.dataSource = dataSource;
        this.templateResourcePath = templateResourcePath;
        this.provider = new AccountSummaryDataProvider();
    }

    /**
     * Convenience constructor if the template is at "/AccountSummary.jrxml" on the classpath.
     */
    public AccountSummaryJasperGenerator(DataSource dataSource) {
        this(dataSource, "/AccountSummary.jrxml");
    }

    /**
     * Run the report given a ReportContext and extra Jasper parameters.
     *
     * The logic is:
     *   1) If ctx.getBeans() is present and non-empty, use that list directly
     *      as the JRBeanCollectionDataSource.
     *   2) Otherwise, query the database using the AccountSummaryDataProvider.
     *
     * @param ctx    ReportContext (dates, funds, accounts, pre-built beans, etc.)
     * @param params Jasper parameters (title, org name, etc.)
     * @return JasperPrint ready for export
     * @throws Exception on compilation or fill errors
     */
    public JasperPrint run(ReportContext ctx, Map<String, Object> params)
        throws Exception {

        JRBeanCollectionDataSource ds;

        // 1) If pre-populated beans were provided, use them directly.
        if (ctx != null && ctx.getBeans() != null && !ctx.getBeans().isEmpty()) {
            ds = new JRBeanCollectionDataSource(ctx.getBeans());
        } else {
            // 2) Otherwise, load beans from the database.
            List<AccountSummaryRowBean> rows;
            try (Connection cx = dataSource.getConnection()) {
                rows = JdbcBeanLoader.queryBeans(
                    cx,
                    provider.beanClass(),
                    provider.sql(ctx),
                    provider.parameterSetter(ctx)
                );
            }
            ds = new JRBeanCollectionDataSource(rows);
        }

        JasperReport report = compileTemplate(templateResourcePath);

        Map<String, Object> parameters =
            (params != null) ? new HashMap<>(params) : new HashMap<>();

        return JasperFillManager.fillReport(report, parameters, ds);
    }

    /**
     * Helper to compile the JRXML template from the classpath.
     */
    private JasperReport compileTemplate(String resourcePath) throws JRException {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException(
                    "JRXML template not found on classpath: " + resourcePath);
            }
            return JasperCompileManager.compileReport(in);
        } catch (Exception e) {
            if (e instanceof JRException) {
                throw (JRException) e;
            }
            throw new JRException("Failed to compile JRXML: " + resourcePath, e);
        }
    }

    /**
     * Optional helper if you want a simple "render to PDF file" hook
     * for testing this generator in isolation.
     */
    public void exportToPdf(ReportContext ctx,
                            Map<String, Object> params,
                            String outputPdfPath) throws Exception {

        JasperPrint print = run(ctx, params);
        JasperExportManager.exportReportToPdfFile(print, outputPdfPath);
    }
}
