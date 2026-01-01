package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.PRIMARY_ACCOUNT_2aBean;

/** Jasper generator for JRXML template PRIMARY_ACCOUNT_2a.jrxml */
public class PRIMARY_ACCOUNT_2aJasperGenerator extends FieldMappedReportGenerator<PRIMARY_ACCOUNT_2aBean>
{
    public PRIMARY_ACCOUNT_2aJasperGenerator()
    {
        super();
    }

    public PRIMARY_ACCOUNT_2aJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<PRIMARY_ACCOUNT_2aBean> getBeanClass()
    {
        return PRIMARY_ACCOUNT_2aBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "PRIMARY_ACCOUNT_2a";
    }
}
