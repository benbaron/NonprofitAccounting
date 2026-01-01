package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.ASSET_DTL_5aBean;

/** Jasper generator for JRXML template ASSET_DTL_5a.jrxml */
public class ASSET_DTL_5aJasperGenerator extends FieldMappedReportGenerator<ASSET_DTL_5aBean>
{
    public ASSET_DTL_5aJasperGenerator()
    {
        super();
    }

    public ASSET_DTL_5aJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<ASSET_DTL_5aBean> getBeanClass()
    {
        return ASSET_DTL_5aBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "ASSET_DTL_5a";
    }
}
