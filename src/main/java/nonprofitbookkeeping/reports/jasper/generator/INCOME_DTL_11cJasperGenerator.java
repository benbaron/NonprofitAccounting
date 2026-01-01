package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.INCOME_DTL_11cBean;

/** Jasper generator for JRXML template INCOME_DTL_11c.jrxml */
public class INCOME_DTL_11cJasperGenerator extends FieldMappedReportGenerator<INCOME_DTL_11cBean>
{
    public INCOME_DTL_11cJasperGenerator()
    {
        super();
    }

    public INCOME_DTL_11cJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<INCOME_DTL_11cBean> getBeanClass()
    {
        return INCOME_DTL_11cBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_DTL_11c";
    }
}
