package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.REGALIA_SALES_DTL_7bBean;

/** Jasper generator for JRXML template REGALIA_SALES_DTL_7b.jrxml */
public class REGALIA_SALES_DTL_7bJasperGenerator extends FieldMappedReportGenerator<REGALIA_SALES_DTL_7bBean>
{
    public REGALIA_SALES_DTL_7bJasperGenerator()
    {
        super();
    }

    public REGALIA_SALES_DTL_7bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<REGALIA_SALES_DTL_7bBean> getBeanClass()
    {
        return REGALIA_SALES_DTL_7bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "REGALIA_SALES_DTL_7b";
    }
}
