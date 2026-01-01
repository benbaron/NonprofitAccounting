package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.SECONDARY_ACCOUNTS_2cBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template SECONDARY_ACCOUNTS_2c.jrxml */
public class SECONDARY_ACCOUNTS_2cJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<SECONDARY_ACCOUNTS_2cBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("bank_name", "a.name");
        overrides.put("account_number", "a.account_number");
        overrides.put("dual_signature_account_type", "a.account_type");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/SECONDARY_ACCOUNTS_2c_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load SECONDARY_ACCOUNTS_2c field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from account a";

        return ReportDataFetcher.queryBeans(SECONDARY_ACCOUNTS_2cBean.class, sql);
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
        // TODO return the classpath or filesystem path to SECONDARY_ACCOUNTS_2c.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "SECONDARY_ACCOUNTS_2c";
    }
}
