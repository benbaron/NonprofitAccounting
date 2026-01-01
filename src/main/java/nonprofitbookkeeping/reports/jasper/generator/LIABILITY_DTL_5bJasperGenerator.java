package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.LIABILITY_DTL_5bBean;

/** Jasper generator for JRXML template LIABILITY_DTL_5b.jrxml */
public class LIABILITY_DTL_5bJasperGenerator extends FieldMappedReportGenerator<LIABILITY_DTL_5bBean>
{
    public LIABILITY_DTL_5bJasperGenerator()
    {
        super();
    }

    public LIABILITY_DTL_5bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<LIABILITY_DTL_5bBean> getBeanClass()
    {
        return LIABILITY_DTL_5bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "LIABILITY_DTL_5b";
    }
}
