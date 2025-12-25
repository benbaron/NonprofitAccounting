package nonprofitbookkeeping.reports.jasper.runtime;

/**
 * SPI for a report-specific data provider backed by JDBC.
 *
 * @param <B> bean type used as the Jasper data source row
 */
public interface ReportDataProvider<B> {

    /**
     * @return the bean class used for this report's rows.
     */
    Class<B> beanClass();

    /**
     * Build the SQL string for this report given the current report context.
     * You can use start/end date, funds, accounts, etc. from the context.
     */
    String sql(ReportContext ctx);

    /**
     * Build a parameter setter to bind PreparedStatement parameters based on
     * the report context (dates, funds, etc.).
     */
    JdbcBeanLoader.SqlParameterSetter parameterSetter(ReportContext ctx);
}
