package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.BALANCE_3Bean;

/** Jasper generator for JRXML template BALANCE_3.jrxml */
public class BALANCE_3JasperGenerator extends FieldMappedReportGenerator<BALANCE_3Bean>
{
    public BALANCE_3JasperGenerator()
    {
        super();
    }

    public BALANCE_3JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<BALANCE_3Bean> getBeanClass()
    {
        return BALANCE_3Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "BALANCE_3";
    }
}
