package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.BALANCE_3Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template BALANCE_3.jrxml */
public class BALANCE_3JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<BALANCE_3Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("contents_b59",
            "(select count(*) from account)");
        overrides.put("contents_e_3",
            "(select sum(opening_balance) from account)");
        overrides.put("contents_e_4",
            "(select sum(amount) from v_journal_entry)");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/BALANCE_3_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load BALANCE_3 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from company_profile cp";

        return ReportDataFetcher.queryBeans(BALANCE_3Bean.class, sql);
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
        // TODO return the classpath or filesystem path to BALANCE_3.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "BALANCE_3";
    }
}
