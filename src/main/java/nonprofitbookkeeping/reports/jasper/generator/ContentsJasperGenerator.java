package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.ContentsBean;

/** Jasper generator for JRXML template Contents.jrxml */
public class ContentsJasperGenerator extends FieldMappedReportGenerator<ContentsBean>
{
    public ContentsJasperGenerator()
    {
        super();
    }

    public ContentsJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<ContentsBean> getBeanClass()
    {
        return ContentsBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "Contents";
    }
}
