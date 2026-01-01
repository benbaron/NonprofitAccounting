package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_OUT_10bBean;

/** Jasper generator for JRXML template TRANSFER_OUT_10b.jrxml */
public class TRANSFER_OUT_10bJasperGenerator extends FieldMappedReportGenerator<TRANSFER_OUT_10bBean>
{
    public TRANSFER_OUT_10bJasperGenerator()
    {
        super();
    }

    public TRANSFER_OUT_10bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_OUT_10bBean> getBeanClass()
    {
        return TRANSFER_OUT_10bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_OUT_10b";
    }
}
