package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.EXPENSE_DTL_12aBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template EXPENSE_DTL_12a.jrxml */
public class EXPENSE_DTL_12aJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<EXPENSE_DTL_12aBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put(
            "organization_or_periodical_not_a_kingdom_newsletter_and_date_ad_was_published",
            "jt.to_from"
        );
        overrides.put("amount", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/EXPENSE_DTL_12a_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load EXPENSE_DTL_12a field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(EXPENSE_DTL_12aBean.class, sql);
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
        // TODO return the classpath or filesystem path to EXPENSE_DTL_12a.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "EXPENSE_DTL_12a";
    }
}
