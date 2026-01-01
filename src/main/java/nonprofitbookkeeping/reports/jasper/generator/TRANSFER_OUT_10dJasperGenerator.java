package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_OUT_10dBean;

/** Jasper generator for JRXML template TRANSFER_OUT_10d.jrxml */
public class TRANSFER_OUT_10dJasperGenerator extends FieldMappedReportGenerator<TRANSFER_OUT_10dBean>
{
    public TRANSFER_OUT_10dJasperGenerator()
    {
        super();
    }

    public TRANSFER_OUT_10dJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_OUT_10dBean> getBeanClass()
    {
        return TRANSFER_OUT_10dBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_OUT_10d";
    }
}
