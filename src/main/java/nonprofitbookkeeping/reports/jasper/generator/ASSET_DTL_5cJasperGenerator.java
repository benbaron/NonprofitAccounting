package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.ASSET_DTL_5cBean;

/** Jasper generator for JRXML template ASSET_DTL_5c.jrxml */
public class ASSET_DTL_5cJasperGenerator extends FieldMappedReportGenerator<ASSET_DTL_5cBean>
{
    public ASSET_DTL_5cJasperGenerator()
    {
        super();
    }

    public ASSET_DTL_5cJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<ASSET_DTL_5cBean> getBeanClass()
    {
        return ASSET_DTL_5cBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "ASSET_DTL_5c";
    }
}
