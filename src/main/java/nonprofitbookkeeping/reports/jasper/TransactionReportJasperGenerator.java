package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal generator for the TransactionReport JRXML template.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<?> getReportData()
    {
        return Collections.emptyList();
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        return new HashMap<>();
    }

    @Override
    protected String getReportPath()
        throws ActionCancelledException, NoFileCreatedException
    {
        return "nonprofitbookkeeping/reports/TransactionReport.jrxml";
    }

    @Override
    public String getBaseName()
    {
        return "TransactionReport";
    }
}
