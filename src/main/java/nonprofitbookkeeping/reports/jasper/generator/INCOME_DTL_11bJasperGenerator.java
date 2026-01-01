package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.INCOME_DTL_11bBean;

/** Jasper generator for JRXML template INCOME_DTL_11b.jrxml */
public class INCOME_DTL_11bJasperGenerator extends FieldMappedReportGenerator<INCOME_DTL_11bBean>
{
    public INCOME_DTL_11bJasperGenerator()
    {
        super();
    }

    public INCOME_DTL_11bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<INCOME_DTL_11bBean> getBeanClass()
    {
        return INCOME_DTL_11bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_DTL_11b";
    }
}
