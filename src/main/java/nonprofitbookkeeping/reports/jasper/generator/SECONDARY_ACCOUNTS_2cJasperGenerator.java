package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.SECONDARY_ACCOUNTS_2cBean;

/** Jasper generator for JRXML template SECONDARY_ACCOUNTS_2c.jrxml */
public class SECONDARY_ACCOUNTS_2cJasperGenerator extends FieldMappedReportGenerator<SECONDARY_ACCOUNTS_2cBean>
{
    public SECONDARY_ACCOUNTS_2cJasperGenerator()
    {
        super();
    }

    public SECONDARY_ACCOUNTS_2cJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<SECONDARY_ACCOUNTS_2cBean> getBeanClass()
    {
        return SECONDARY_ACCOUNTS_2cBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "SECONDARY_ACCOUNTS_2c";
    }
}
