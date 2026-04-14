package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.FUNDS_14Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template FUNDS_14.jrxml */
public class FUNDS_14JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<FUNDS_14Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("general_fund_2_0", "cp.default_bank_account");
        overrides.put("end_of_period_all_non_dedicated_funds",
            "(select sum(amount) from v_journal_entry)");
        overrides.put("contents_b59",
            "(select count(*) from account)");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/FUNDS_14_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load FUNDS_14 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from company_profile cp";

        return ReportDataFetcher.queryBeans(FUNDS_14Bean.class, sql);
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
        // TODO return the classpath or filesystem path to FUNDS_14.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "FUNDS_14";
    }
}
