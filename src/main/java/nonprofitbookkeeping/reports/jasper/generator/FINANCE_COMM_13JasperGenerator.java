package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.FINANCE_COMM_13Bean;

/** Jasper generator for JRXML template FINANCE_COMM_13.jrxml */
public class FINANCE_COMM_13JasperGenerator extends FieldMappedReportGenerator<FINANCE_COMM_13Bean>
{
    public FINANCE_COMM_13JasperGenerator()
    {
        super();
    }

    public FINANCE_COMM_13JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<FINANCE_COMM_13Bean> getBeanClass()
    {
        return FINANCE_COMM_13Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "FINANCE_COMM_13";
    }
}
