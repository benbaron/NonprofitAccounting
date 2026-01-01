package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.JdbcReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.jasper.beans.PRIMARY_ACCOUNT_2aBean;

/** Skeleton generator for JRXML template PRIMARY_ACCOUNT_2a.jrxml */
public class PRIMARY_ACCOUNT_2aJasperGenerator extends JdbcReportGenerator<PRIMARY_ACCOUNT_2aBean>
{
    @Override
    protected List<PRIMARY_ACCOUNT_2aBean> getReportData()
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
        // TODO return the classpath or filesystem path to PRIMARY_ACCOUNT_2a.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "PRIMARY_ACCOUNT_2a";
    }
}
