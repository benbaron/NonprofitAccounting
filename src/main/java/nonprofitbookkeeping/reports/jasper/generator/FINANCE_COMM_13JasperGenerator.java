package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.FINANCE_COMM_13Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template FINANCE_COMM_13.jrxml */
public class FINANCE_COMM_13JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<FINANCE_COMM_13Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("sca_name_seneschal", "cp.name");
        overrides.put("number", "cp.tax_id");
        overrides.put("mm_yyyy", "cp.fiscal_year_start");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/FINANCE_COMM_13_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load FINANCE_COMM_13 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from company_profile cp";

        return ReportDataFetcher.queryBeans(FINANCE_COMM_13Bean.class, sql);
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
        // TODO return the classpath or filesystem path to FINANCE_COMM_13.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "FINANCE_COMM_13";
    }
}
