package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.INCOME_DTL_11aBean;

/** Jasper generator for JRXML template INCOME_DTL_11a.jrxml */
public class INCOME_DTL_11aJasperGenerator extends FieldMappedReportGenerator<INCOME_DTL_11aBean>
{
    public INCOME_DTL_11aJasperGenerator()
    {
        super();
    }

    public INCOME_DTL_11aJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<INCOME_DTL_11aBean> getBeanClass()
    {
        return INCOME_DTL_11aBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_DTL_11a";
    }
}
