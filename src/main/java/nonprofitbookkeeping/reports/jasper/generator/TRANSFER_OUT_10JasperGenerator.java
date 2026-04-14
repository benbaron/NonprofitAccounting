package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_OUT_10Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template TRANSFER_OUT_10.jrxml */
public class TRANSFER_OUT_10JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<TRANSFER_OUT_10Bean> getReportData()
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
                "/nonprofitbookkeeping/reports/TRANSFER_OUT_10_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load TRANSFER_OUT_10 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from v_journal_transaction jt\n" +
            "join v_journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(TRANSFER_OUT_10Bean.class, sql);
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
        // TODO return the classpath or filesystem path to TRANSFER_OUT_10.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_OUT_10";
    }
}
