package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.CONTACT_INFO_1Bean;

/** Jasper generator for JRXML template CONTACT_INFO_1.jrxml */
public class CONTACT_INFO_1JasperGenerator extends FieldMappedReportGenerator<CONTACT_INFO_1Bean>
{
    public CONTACT_INFO_1JasperGenerator()
    {
        super();
    }

    public CONTACT_INFO_1JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<CONTACT_INFO_1Bean> getBeanClass()
    {
        return CONTACT_INFO_1Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "CONTACT_INFO_1";
    }
}
