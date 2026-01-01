package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.INCOME_4Bean;

/** Jasper generator for JRXML template INCOME_4.jrxml */
public class INCOME_4JasperGenerator extends FieldMappedReportGenerator<INCOME_4Bean>
{
    public INCOME_4JasperGenerator()
    {
        super();
    }

    public INCOME_4JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<INCOME_4Bean> getBeanClass()
    {
        return INCOME_4Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_4";
    }
}
