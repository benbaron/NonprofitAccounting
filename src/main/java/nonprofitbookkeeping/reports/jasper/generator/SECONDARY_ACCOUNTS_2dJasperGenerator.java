package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.SECONDARY_ACCOUNTS_2dBean;

/** Jasper generator for JRXML template SECONDARY_ACCOUNTS_2d.jrxml */
public class SECONDARY_ACCOUNTS_2dJasperGenerator extends FieldMappedReportGenerator<SECONDARY_ACCOUNTS_2dBean>
{
    public SECONDARY_ACCOUNTS_2dJasperGenerator()
    {
        super();
    }

    public SECONDARY_ACCOUNTS_2dJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<SECONDARY_ACCOUNTS_2dBean> getBeanClass()
    {
        return SECONDARY_ACCOUNTS_2dBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "SECONDARY_ACCOUNTS_2d";
    }
}
