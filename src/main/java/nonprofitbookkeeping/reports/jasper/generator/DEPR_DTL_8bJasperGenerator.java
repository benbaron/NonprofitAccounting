package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.DEPR_DTL_8bBean;

/** Jasper generator for JRXML template DEPR_DTL_8b.jrxml */
public class DEPR_DTL_8bJasperGenerator extends FieldMappedReportGenerator<DEPR_DTL_8bBean>
{
    public DEPR_DTL_8bJasperGenerator()
    {
        super();
    }

    public DEPR_DTL_8bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<DEPR_DTL_8bBean> getBeanClass()
    {
        return DEPR_DTL_8bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "DEPR_DTL_8b";
    }
}
