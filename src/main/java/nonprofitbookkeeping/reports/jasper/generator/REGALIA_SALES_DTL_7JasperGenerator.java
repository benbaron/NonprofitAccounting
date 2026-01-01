package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.JdbcReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.jasper.beans.REGALIA_SALES_DTL_7Bean;

/** Skeleton generator for JRXML template REGALIA_SALES_DTL_7.jrxml */
public class REGALIA_SALES_DTL_7JasperGenerator extends JdbcReportGenerator<REGALIA_SALES_DTL_7Bean>
{
    @Override
    protected List<REGALIA_SALES_DTL_7Bean> getReportData()
    {
        return super.getReportData();
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
