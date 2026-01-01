package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.FUNDS_14Bean;

/** Jasper generator for JRXML template FUNDS_14.jrxml */
public class FUNDS_14JasperGenerator extends FieldMappedReportGenerator<FUNDS_14Bean>
{
    public FUNDS_14JasperGenerator()
    {
        super();
    }

    public FUNDS_14JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<FUNDS_14Bean> getBeanClass()
    {
        return FUNDS_14Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "FUNDS_14";
    }
}
