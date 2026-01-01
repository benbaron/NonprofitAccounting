package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_IN_9cBean;

/** Jasper generator for JRXML template TRANSFER_IN_9c.jrxml */
public class TRANSFER_IN_9cJasperGenerator extends FieldMappedReportGenerator<TRANSFER_IN_9cBean>
{
    public TRANSFER_IN_9cJasperGenerator()
    {
        super();
    }

    public TRANSFER_IN_9cJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_IN_9cBean> getBeanClass()
    {
        return TRANSFER_IN_9cBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_IN_9c";
    }
}
