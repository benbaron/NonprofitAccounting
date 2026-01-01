package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.NEWSLETTER_15Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template NEWSLETTER_15.jrxml */
public class NEWSLETTER_15JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<NEWSLETTER_15Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("newsletter_name", "cp.name");
        overrides.put("gross_income", "(select sum(amount) from journal_entry)");
        overrides.put("rate_1_price_of_one_subscription",
            "cp.base_currency");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/NEWSLETTER_15_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load NEWSLETTER_15 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from company_profile cp";

        return ReportDataFetcher.queryBeans(NEWSLETTER_15Bean.class, sql);
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        Map<String, Object> params = new HashMap<>();
        // TODO populate report parameters such as title or filters
        return params;
    }

    @Override
    protected String getReportPath() throws ActionCancelledException, NoFileCreatedException
    {
        // TODO return the classpath or filesystem path to NEWSLETTER_15.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "NEWSLETTER_15";
    }
}
