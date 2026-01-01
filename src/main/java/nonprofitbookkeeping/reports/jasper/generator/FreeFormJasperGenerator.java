package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.FreeFormBean;

/** Jasper generator for JRXML template FreeForm.jrxml */
public class FreeFormJasperGenerator extends FieldMappedReportGenerator<FreeFormBean>
{
    public FreeFormJasperGenerator()
    {
        super();
    }

    public FreeFormJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<FreeFormBean> getBeanClass()
    {
        return FreeFormBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "FreeForm";
    }
}
