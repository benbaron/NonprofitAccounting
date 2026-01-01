package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_IN_9dBean;

/** Jasper generator for JRXML template TRANSFER_IN_9d.jrxml */
public class TRANSFER_IN_9dJasperGenerator extends FieldMappedReportGenerator<TRANSFER_IN_9dBean>
{
    public TRANSFER_IN_9dJasperGenerator()
    {
        super();
    }

    public TRANSFER_IN_9dJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_IN_9dBean> getBeanClass()
    {
        return TRANSFER_IN_9dBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_IN_9d";
    }
}
