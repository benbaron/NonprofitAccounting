package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_OUT_10cBean;

/** Jasper generator for JRXML template TRANSFER_OUT_10c.jrxml */
public class TRANSFER_OUT_10cJasperGenerator extends FieldMappedReportGenerator<TRANSFER_OUT_10cBean>
{
    public TRANSFER_OUT_10cJasperGenerator()
    {
        super();
    }

    public TRANSFER_OUT_10cJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_OUT_10cBean> getBeanClass()
    {
        return TRANSFER_OUT_10cBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_OUT_10c";
    }
}
