package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.INVENTORY_DTL_6bBean;

/** Jasper generator for JRXML template INVENTORY_DTL_6b.jrxml */
public class INVENTORY_DTL_6bJasperGenerator extends FieldMappedReportGenerator<INVENTORY_DTL_6bBean>
{
    public INVENTORY_DTL_6bJasperGenerator()
    {
        super();
    }

    public INVENTORY_DTL_6bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<INVENTORY_DTL_6bBean> getBeanClass()
    {
        return INVENTORY_DTL_6bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "INVENTORY_DTL_6b";
    }
}
