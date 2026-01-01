package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.COMMENTSBean;

/** Jasper generator for JRXML template COMMENTS.jrxml */
public class COMMENTSJasperGenerator extends FieldMappedReportGenerator<COMMENTSBean>
{
    public COMMENTSJasperGenerator()
    {
        super();
    }

    public COMMENTSJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<COMMENTSBean> getBeanClass()
    {
        return COMMENTSBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "COMMENTS";
    }
}
