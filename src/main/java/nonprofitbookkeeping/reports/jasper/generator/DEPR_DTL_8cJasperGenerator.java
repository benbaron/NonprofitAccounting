package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.DEPR_DTL_8cBean;

/** Jasper generator for JRXML template DEPR_DTL_8c.jrxml */
public class DEPR_DTL_8cJasperGenerator extends FieldMappedReportGenerator<DEPR_DTL_8cBean>
{
    public DEPR_DTL_8cJasperGenerator()
    {
        super();
    }

    public DEPR_DTL_8cJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<DEPR_DTL_8cBean> getBeanClass()
    {
        return DEPR_DTL_8cBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "DEPR_DTL_8c";
    }
}
