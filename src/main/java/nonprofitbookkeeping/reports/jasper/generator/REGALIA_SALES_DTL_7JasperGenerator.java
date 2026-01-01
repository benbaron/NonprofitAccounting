package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.REGALIA_SALES_DTL_7Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template REGALIA_SALES_DTL_7.jrxml */
public class REGALIA_SALES_DTL_7JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<REGALIA_SALES_DTL_7Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("regalia_other_sales_detail", "jt.memo");
        overrides.put("do_not_enter_any_more_items_under_regalia",
            "jt.to_from");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/REGALIA_SALES_DTL_7_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load REGALIA_SALES_DTL_7 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(REGALIA_SALES_DTL_7Bean.class, sql);
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
        // TODO return the classpath or filesystem path to REGALIA_SALES_DTL_7.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "REGALIA_SALES_DTL_7";
    }
}
