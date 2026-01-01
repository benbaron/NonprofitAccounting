package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.JdbcReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_IN_9cBean;

/** Skeleton generator for JRXML template TRANSFER_IN_9c.jrxml */
public class TRANSFER_IN_9cJasperGenerator extends JdbcReportGenerator<TRANSFER_IN_9cBean>
{
    @Override
    protected List<TRANSFER_IN_9cBean> getReportData()
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
        // TODO return the classpath or filesystem path to TRANSFER_IN_9c.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_IN_9c";
    }
}
