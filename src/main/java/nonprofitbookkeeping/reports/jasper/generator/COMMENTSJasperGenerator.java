package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.COMMENTSBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template COMMENTS.jrxml */
public class COMMENTSJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<COMMENTSBean> getReportData()
    {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("contents_b59", "d.name");
        overrides.put("contents_e_3", "d.content");

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/COMMENTS_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load COMMENTS field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from document d\n" +
            "where d.name = 'comments'";

        return ReportDataFetcher.queryBeans(COMMENTSBean.class, sql);
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
        // TODO return the classpath or filesystem path to COMMENTS.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "COMMENTS";
    }
}
