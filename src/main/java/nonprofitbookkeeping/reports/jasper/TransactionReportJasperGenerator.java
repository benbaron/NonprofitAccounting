package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.jasper.beans.TransactionReportBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

/**
 * Jasper generator for the TransactionReport template.
 */
public class TransactionReportJasperGenerator
    extends FieldMappedReportGenerator<TransactionReportBean>
{
    public TransactionReportJasperGenerator()
    {
        super();
    }

    public TransactionReportJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TransactionReportBean> getBeanClass()
    {
        return TransactionReportBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TransactionReport";
    }
}
