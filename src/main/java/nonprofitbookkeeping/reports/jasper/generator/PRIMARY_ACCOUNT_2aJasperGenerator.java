package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.PRIMARY_ACCOUNT_2aBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template PRIMARY_ACCOUNT_2a.jrxml */
public class PRIMARY_ACCOUNT_2aJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<PRIMARY_ACCOUNT_2aBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("bank_name", "a.name");
        overrides.put("bank_account_title", "a.account_code");
        overrides.put("bank_account_type", "a.account_type");
        overrides.put("bank_account_number", "a.account_number");
        overrides.put("deposit_date", "jt.date_text");
        overrides.put("amount_of_deposit", "je.amount");
        overrides.put("check_number", "jt.check_number");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/PRIMARY_ACCOUNT_2a_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load PRIMARY_ACCOUNT_2a field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from account a\n" +
            "left join v_journal_entry je on je.account_number = a.account_number\n" +
            "left join v_journal_transaction jt on jt.id = je.txn_id";

        return ReportDataFetcher.queryBeans(PRIMARY_ACCOUNT_2aBean.class, sql);
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
        // TODO return the classpath or filesystem path to PRIMARY_ACCOUNT_2a.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "PRIMARY_ACCOUNT_2a";
    }
}
