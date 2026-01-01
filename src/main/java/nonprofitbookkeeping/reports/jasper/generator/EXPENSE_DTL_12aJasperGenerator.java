package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.EXPENSE_DTL_12aBean;

/** Jasper generator for JRXML template EXPENSE_DTL_12a.jrxml */
public class EXPENSE_DTL_12aJasperGenerator extends FieldMappedReportGenerator<EXPENSE_DTL_12aBean>
{
    public EXPENSE_DTL_12aJasperGenerator()
    {
        super();
    }

    public EXPENSE_DTL_12aJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<EXPENSE_DTL_12aBean> getBeanClass()
    {
        return EXPENSE_DTL_12aBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "EXPENSE_DTL_12a";
    }
}
