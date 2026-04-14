package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.INCOME_DTL_11aBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template INCOME_DTL_11a.jrxml */
public class INCOME_DTL_11aJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<INCOME_DTL_11aBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("_1a_fundraising_income_internal_event", "jt.memo");
        overrides.put("activity_at_the_event", "jt.to_from");
        overrides.put("amount", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/INCOME_DTL_11a_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load INCOME_DTL_11a field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from v_journal_transaction jt\n" +
            "join v_journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(INCOME_DTL_11aBean.class, sql);
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
        // TODO return the classpath or filesystem path to INCOME_DTL_11a.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_DTL_11a";
    }
}
