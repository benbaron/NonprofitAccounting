package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.TRANSFER_OUT_10Bean;

/** Jasper generator for JRXML template TRANSFER_OUT_10.jrxml */
public class TRANSFER_OUT_10JasperGenerator extends FieldMappedReportGenerator<TRANSFER_OUT_10Bean>
{
    public TRANSFER_OUT_10JasperGenerator()
    {
        super();
    }

    public TRANSFER_OUT_10JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<TRANSFER_OUT_10Bean> getBeanClass()
    {
        return TRANSFER_OUT_10Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "TRANSFER_OUT_10";
    }
}
