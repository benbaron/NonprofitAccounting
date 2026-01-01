package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.LIABILITY_DTL_5dBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template LIABILITY_DTL_5d.jrxml */
public class LIABILITY_DTL_5dJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<LIABILITY_DTL_5dBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("deferred_revenue_event", "jt.memo");
        overrides.put("event", "jt.to_from");
        overrides.put("prior_amount", "je.amount");
        overrides.put("current_amount", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/LIABILITY_DTL_5d_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load LIABILITY_DTL_5d field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(LIABILITY_DTL_5dBean.class, sql);
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
        // TODO return the classpath or filesystem path to LIABILITY_DTL_5d.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "LIABILITY_DTL_5d";
    }
}
