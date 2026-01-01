package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.LIABILITY_DTL_5dBean;

/** Jasper generator for JRXML template LIABILITY_DTL_5d.jrxml */
public class LIABILITY_DTL_5dJasperGenerator extends FieldMappedReportGenerator<LIABILITY_DTL_5dBean>
{
    public LIABILITY_DTL_5dJasperGenerator()
    {
        super();
    }

    public LIABILITY_DTL_5dJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<LIABILITY_DTL_5dBean> getBeanClass()
    {
        return LIABILITY_DTL_5dBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "LIABILITY_DTL_5d";
    }
}
