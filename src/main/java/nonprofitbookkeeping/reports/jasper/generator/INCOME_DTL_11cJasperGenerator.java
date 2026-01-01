package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.INCOME_DTL_11cBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template INCOME_DTL_11c.jrxml */
public class INCOME_DTL_11cJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<INCOME_DTL_11cBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("name_of_organization_or_individual", "jt.to_from");
        overrides.put("reason", "jt.memo");
        overrides.put("date_received", "jt.date_text");
        overrides.put("amount", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/INCOME_DTL_11c_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load INCOME_DTL_11c field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(INCOME_DTL_11cBean.class, sql);
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
        // TODO return the classpath or filesystem path to INCOME_DTL_11c.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_DTL_11c";
    }
}
