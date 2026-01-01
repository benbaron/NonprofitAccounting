package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.jasper.beans.FreeFormBean;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template FreeForm.jrxml */
public class FreeFormJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<FreeFormBean> getReportData()
    {
        String sql = "select count(*) as placeholder from company_profile";
        return ReportDataFetcher.queryBeans(FreeFormBean.class, sql);
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
        // TODO return the classpath or filesystem path to FreeForm.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "FreeForm";
    }
}
