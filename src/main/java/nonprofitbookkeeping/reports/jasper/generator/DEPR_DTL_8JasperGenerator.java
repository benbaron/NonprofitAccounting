package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.DEPR_DTL_8Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template DEPR_DTL_8.jrxml */
public class DEPR_DTL_8JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<DEPR_DTL_8Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("oa_ar_or_fr", "jt.to_from");
        overrides.put(
            "equipment_purchases_or_value_2000_each_item_description",
            "jt.memo"
        );
        overrides.put("purchase_year", "jt.date_text");
        overrides.put("qty", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/DEPR_DTL_8_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load DEPR_DTL_8 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from v_journal_transaction jt\n" +
            "join v_journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(DEPR_DTL_8Bean.class, sql);
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
        // TODO return the classpath or filesystem path to DEPR_DTL_8.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "DEPR_DTL_8";
    }
}
