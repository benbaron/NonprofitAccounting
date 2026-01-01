package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_IN_9Bean;

/** Jasper generator for JRXML template TRANSFER_IN_9.jrxml */
public class TRANSFER_IN_9JasperGenerator extends FieldMappedReportGenerator<TRANSFER_IN_9Bean>
{
    public TRANSFER_IN_9JasperGenerator()
    {
        super();
    }

    public TRANSFER_IN_9JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_IN_9Bean> getBeanClass()
    {
        return TRANSFER_IN_9Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_IN_9";
    }
}
