package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.INVENTORY_DTL_6Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template INVENTORY_DTL_6.jrxml */
public class INVENTORY_DTL_6JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<INVENTORY_DTL_6Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("lot_item_description_and_year_purchased", "jt.memo");
        overrides.put("suggested_selling_price", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/INVENTORY_DTL_6_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load INVENTORY_DTL_6 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(INVENTORY_DTL_6Bean.class, sql);
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
        // TODO return the classpath or filesystem path to INVENTORY_DTL_6.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "INVENTORY_DTL_6";
    }
}
