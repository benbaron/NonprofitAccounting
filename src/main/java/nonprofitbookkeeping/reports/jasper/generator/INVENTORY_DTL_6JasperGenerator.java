package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.INVENTORY_DTL_6Bean;

/** Jasper generator for JRXML template INVENTORY_DTL_6.jrxml */
public class INVENTORY_DTL_6JasperGenerator extends FieldMappedReportGenerator<INVENTORY_DTL_6Bean>
{
    public INVENTORY_DTL_6JasperGenerator()
    {
        super();
    }

    public INVENTORY_DTL_6JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<INVENTORY_DTL_6Bean> getBeanClass()
    {
        return INVENTORY_DTL_6Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "INVENTORY_DTL_6";
    }
}
