package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_IN_9bBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template TRANSFER_IN_9b.jrxml */
public class TRANSFER_IN_9bJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<TRANSFER_IN_9bBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("within_the_kingdom", "jt.to_from");
        overrides.put("check", "jt.check_number");
        overrides.put("check_date", "jt.date_text");
        overrides.put("amount", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/TRANSFER_IN_9b_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load TRANSFER_IN_9b field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(TRANSFER_IN_9bBean.class, sql);
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
        // TODO return the classpath or filesystem path to TRANSFER_IN_9b.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_IN_9b";
    }
}
