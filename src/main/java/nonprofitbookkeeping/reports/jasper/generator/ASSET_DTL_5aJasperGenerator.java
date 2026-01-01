package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.ASSET_DTL_5aBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template ASSET_DTL_5a.jrxml */
public class ASSET_DTL_5aJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<ASSET_DTL_5aBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("sending_branch_or_reason", "jt.memo");
        overrides.put("amount", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/ASSET_DTL_5a_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load ASSET_DTL_5a field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(ASSET_DTL_5aBean.class, sql);
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
        // TODO return the classpath or filesystem path to ASSET_DTL_5a.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "ASSET_DTL_5a";
    }
}
