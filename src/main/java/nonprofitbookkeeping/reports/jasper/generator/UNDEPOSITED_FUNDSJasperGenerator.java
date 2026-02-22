package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.UndepositedFundsItem;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UNDEPOSITED_FUNDSJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<UndepositedFundsItem> getReportData()
    {
        String sql = "select\n" +
            "  date_sent_received,\n" +
            "  date_transfer_or_check,\n" +
            "  date_on_statement,\n" +
            "  name_of_person_business,\n" +
            "  details_notes,\n" +
            "  from_to_card_merchant,\n" +
            "  account_for_payment_or_deposit,\n" +
            "  amount,\n" +
            "  date_reversed,\n" +
            "  reversal_approved_by\n" +
            "from undeposited_funds_item\n" +
            "order by id";

        return ReportDataFetcher.queryRowBasedBeans(
            UndepositedFundsItem.class,
            sql
        );
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        return new HashMap<>();
    }

    @Override
    protected String getReportPath()
        throws ActionCancelledException, NoFileCreatedException
    {
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "UNDEPOSITED_FUNDS";
    }
}
