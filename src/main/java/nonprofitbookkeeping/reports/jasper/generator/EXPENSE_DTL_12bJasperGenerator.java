package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.EXPENSE_DTL_12bBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template EXPENSE_DTL_12b.jrxml */
public class EXPENSE_DTL_12bJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<EXPENSE_DTL_12bBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("organization_or_person_ar", "jt.to_from");
        overrides.put("check_ar", "jt.check_number");
        overrides.put("check_date_ar", "jt.date_text");
        overrides.put("amount_ar", "je.amount");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/EXPENSE_DTL_12b_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load EXPENSE_DTL_12b field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id";

        return ReportDataFetcher.queryBeans(EXPENSE_DTL_12bBean.class, sql);
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
        // TODO return the classpath or filesystem path to EXPENSE_DTL_12b.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "EXPENSE_DTL_12b";
    }
}
