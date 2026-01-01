package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMappedReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.beans.SECONDARY_ACCOUNTS_2bBean;

/** Jasper generator for JRXML template SECONDARY_ACCOUNTS_2b.jrxml */
public class SECONDARY_ACCOUNTS_2bJasperGenerator extends FieldMappedReportGenerator<SECONDARY_ACCOUNTS_2bBean>
{
    public SECONDARY_ACCOUNTS_2bJasperGenerator()
    {
        super();
    }

    public SECONDARY_ACCOUNTS_2bJasperGenerator(ReportContext context)
    {
        super(context);
    }

    @Override
    protected Class<SECONDARY_ACCOUNTS_2bBean> getBeanClass()
    {
        return SECONDARY_ACCOUNTS_2bBean.class;
    }

    @Override
    public String getBaseName()
    {
        return "SECONDARY_ACCOUNTS_2b";
    }
}
