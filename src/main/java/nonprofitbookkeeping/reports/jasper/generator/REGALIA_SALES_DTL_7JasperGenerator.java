package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.REGALIA_SALES_DTL_7Bean;

/** Jasper generator for JRXML template REGALIA_SALES_DTL_7.jrxml */
public class REGALIA_SALES_DTL_7JasperGenerator extends FieldMappedReportGenerator<REGALIA_SALES_DTL_7Bean>
{
    public REGALIA_SALES_DTL_7JasperGenerator()
    {
        super();
    }

    public REGALIA_SALES_DTL_7JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<REGALIA_SALES_DTL_7Bean> getBeanClass()
    {
        return REGALIA_SALES_DTL_7Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "REGALIA_SALES_DTL_7";
    }
}
