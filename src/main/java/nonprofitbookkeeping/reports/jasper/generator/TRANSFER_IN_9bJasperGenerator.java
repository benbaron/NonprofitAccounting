package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_IN_9bBean;

/** Jasper generator for JRXML template TRANSFER_IN_9b.jrxml */
public class TRANSFER_IN_9bJasperGenerator extends FieldMappedReportGenerator<TRANSFER_IN_9bBean>
{
    public TRANSFER_IN_9bJasperGenerator()
    {
        super();
    }

    public TRANSFER_IN_9bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_IN_9bBean> getBeanClass()
    {
        return TRANSFER_IN_9bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_IN_9b";
    }
}
