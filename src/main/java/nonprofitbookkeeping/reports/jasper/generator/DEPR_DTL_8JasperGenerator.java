package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.DEPR_DTL_8Bean;

/** Jasper generator for JRXML template DEPR_DTL_8.jrxml */
public class DEPR_DTL_8JasperGenerator extends FieldMappedReportGenerator<DEPR_DTL_8Bean>
{
    public DEPR_DTL_8JasperGenerator()
    {
        super();
    }

    public DEPR_DTL_8JasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<DEPR_DTL_8Bean> getBeanClass()
    {
        return DEPR_DTL_8Bean.class;
    }

    @Override
    public String getBaseName()
    {
        return "DEPR_DTL_8";
    }
}
