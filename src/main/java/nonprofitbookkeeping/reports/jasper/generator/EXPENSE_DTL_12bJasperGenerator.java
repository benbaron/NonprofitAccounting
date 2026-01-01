package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.EXPENSE_DTL_12bBean;

/** Jasper generator for JRXML template EXPENSE_DTL_12b.jrxml */
public class EXPENSE_DTL_12bJasperGenerator extends FieldMappedReportGenerator<EXPENSE_DTL_12bBean>
{
    public EXPENSE_DTL_12bJasperGenerator()
    {
        super();
    }

    public EXPENSE_DTL_12bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<EXPENSE_DTL_12bBean> getBeanClass()
    {
        return EXPENSE_DTL_12bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "EXPENSE_DTL_12b";
    }
}
