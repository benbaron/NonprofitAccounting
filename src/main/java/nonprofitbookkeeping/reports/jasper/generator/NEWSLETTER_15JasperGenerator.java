package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.NEWSLETTER_15Bean;

/** Jasper generator for JRXML template NEWSLETTER_15.jrxml */
public class NEWSLETTER_15JasperGenerator extends FieldMappedReportGenerator<NEWSLETTER_15Bean>
{
    public NEWSLETTER_15JasperGenerator()
    {
        super();
    }

    public NEWSLETTER_15JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<NEWSLETTER_15Bean> getBeanClass()
    {
        return NEWSLETTER_15Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "NEWSLETTER_15";
    }
}
