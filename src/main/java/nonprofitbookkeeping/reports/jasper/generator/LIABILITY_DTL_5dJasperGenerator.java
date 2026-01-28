package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.beans.LIABILITY_DTL_5dBean;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template LIABILITY_DTL_5d.jrxml */
public class LIABILITY_DTL_5dJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<LIABILITY_DTL_5dBean> getReportData()
    {
        Map<String, String> overrides = buildSupplementalOverrides();

        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/LIABILITY_DTL_5d_fieldmap.csv",
                overrides
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load LIABILITY_DTL_5d field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from (select 1) as seed";

        return ReportDataFetcher.queryBeans(LIABILITY_DTL_5dBean.class, sql);
    }

    private static Map<String, String> buildSupplementalOverrides()
    {
        Map<String, String> overrides = new HashMap<>();

        for (int i = 1; i <= 15; i++)
        {
            String suffix = i == 1 ? "" : "_" + i;
            overrides.put("deferred_revenue_event" + suffix,
                supplementalSubquery("DEFERRED_REVENUE", "coalesce(p.name, tsl.description)", i - 1));
            overrides.put("event" + suffix,
                supplementalSubquery("DEFERRED_REVENUE", "coalesce(tsl.reference, tsl.notes)", i - 1));
            overrides.put("prior_amount" + suffix,
                supplementalSubquery("DEFERRED_REVENUE", "cast(tsl.amount as varchar)", i - 1));
            overrides.put("current_amount" + suffix,
                supplementalSubquery("DEFERRED_REVENUE", "cast(tsl.amount as varchar)", i - 1));
        }

        for (int i = 1; i <= 7; i++)
        {
            String suffix = i == 1 ? "" : "_" + i;
            int amountIndex = 15 + i;
            overrides.put("payables_owed_to" + suffix,
                supplementalSubquery("PAYABLE", "coalesce(p.name, tsl.description)", i - 1));
            overrides.put("reason" + suffix,
                supplementalSubquery("PAYABLE", "coalesce(tsl.reference, tsl.notes)", i - 1));
            overrides.put("prior_amount_" + amountIndex,
                supplementalSubquery("PAYABLE", "cast(tsl.amount as varchar)", i - 1));
            overrides.put("current_amount_" + amountIndex,
                supplementalSubquery("PAYABLE", "cast(tsl.amount as varchar)", i - 1));
        }

        for (int i = 1; i <= 7; i++)
        {
            String suffix = i == 1 ? "" : "_" + i;
            int reasonIndex = 7 + i;
            int amountIndex = 22 + i;
            overrides.put("other_liabilities_owed_to" + suffix,
                supplementalSubquery("OTHER_LIABILITY", "coalesce(p.name, tsl.description)", i - 1));
            overrides.put("reason_" + reasonIndex,
                supplementalSubquery("OTHER_LIABILITY", "coalesce(tsl.reference, tsl.notes)", i - 1));
            overrides.put("prior_amount_" + amountIndex,
                supplementalSubquery("OTHER_LIABILITY", "cast(tsl.amount as varchar)", i - 1));
            overrides.put("current_amount_" + amountIndex,
                supplementalSubquery("OTHER_LIABILITY", "cast(tsl.amount as varchar)", i - 1));
        }

        return overrides;
    }

    private static String supplementalSubquery(String kind, String valueExpr, int offset)
    {
        return "(select " + valueExpr +
            " from txn_supplemental_line tsl " +
            "left join person p on p.id = tsl.counterparty_person_id " +
            "where tsl.line_kind = '" + kind + "' " +
            "order by tsl.id limit 1 offset " + offset + ")";
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        Map<String, Object> params = new HashMap<>();
        // TODO populate report parameters such as title or filters
        return params;
    }

    @Override
    protected String getReportPath() throws ActionCancelledException, NoFileCreatedException
    {
        // TODO return the classpath or filesystem path to LIABILITY_DTL_5d.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "LIABILITY_DTL_5d";
    }
}
