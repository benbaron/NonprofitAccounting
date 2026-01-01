package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.CONTACT_INFO_1Bean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template CONTACT_INFO_1.jrxml */
public class CONTACT_INFO_1JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<CONTACT_INFO_1Bean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("legal_name", "cp.name");
        overrides.put("street_address", "cp.address");
        overrides.put("internet_or_e_mail_address_required_if_available",
            "cp.email");
        overrides.put("alternate_phone", "cp.phone");
        overrides.put("membership", "cp.chart_of_accounts_type");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/CONTACT_INFO_1_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load CONTACT_INFO_1 field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from company_profile cp";

        return ReportDataFetcher.queryBeans(CONTACT_INFO_1Bean.class, sql);
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
        // TODO return the classpath or filesystem path to CONTACT_INFO_1.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "CONTACT_INFO_1";
    }
}
