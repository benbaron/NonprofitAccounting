package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.JdbcReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.jasper.beans.DEPR_DTL_8Bean;

/** Skeleton generator for JRXML template DEPR_DTL_8.jrxml */
public class DEPR_DTL_8JasperGenerator extends JdbcReportGenerator<DEPR_DTL_8Bean>
{
    @Override
    protected List<DEPR_DTL_8Bean> getReportData()
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
        // TODO return the classpath or filesystem path to DEPR_DTL_8.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "DEPR_DTL_8";
    }
}
