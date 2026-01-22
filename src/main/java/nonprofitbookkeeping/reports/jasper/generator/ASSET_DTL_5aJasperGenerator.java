package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aOtherAssetLineItem;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aPrepaidExpenseLineItem;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aReceivableLineItem;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aReportBean;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aUndepositedFundsLineItem;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;
import nonprofitbookkeeping.reports.jasper.runtime.JdbcBeanLoader;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

/** Skeleton generator for JRXML template ASSET_DTL_5a.jrxml */
public class ASSET_DTL_5aJasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<AssetDtl5aReportBean> getReportData()
    {
        String selectList;
        try
        {
            selectList = FieldMapSqlBuilder.buildSelectList(
                "/nonprofitbookkeeping/reports/ASSET_DTL_5a_fieldmap.csv",
                null
            );
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(
                "Unable to load ASSET_DTL_5a field map", ex);
        }

        String sql = "select\n" +
            selectList + "\n" +
            "from journal_transaction jt\n" +
            "join journal_entry je on je.txn_id = jt.id\n" +
            "limit 1";

        List<AssetDtl5aReportBean> headerBeans =
            ReportDataFetcher.queryBeans(AssetDtl5aReportBean.class, sql);
        AssetDtl5aReportBean reportBean = headerBeans.get(0);

        try (Connection cx = Database.get().getConnection())
        {
            reportBean.setUndeposited_funds(
                queryBeans(
                    cx,
                    AssetDtl5aUndepositedFundsLineItem.class,
                    "with numbered as (\n" +
                        "  select\n" +
                        "    jt.memo as sending_branch_or_reason,\n" +
                        "    je.amount as amount,\n" +
                        "    row_number() over (order by jt.id, je.id) as rn\n" +
                        "  from journal_transaction jt\n" +
                        "  join journal_entry je on je.txn_id = jt.id\n" +
                        ")\n" +
                        "select\n" +
                        "  left_entry.sending_branch_or_reason as sending_branch_or_reason_left,\n" +
                        "  left_entry.amount as amount_left,\n" +
                        "  right_entry.sending_branch_or_reason as sending_branch_or_reason_right,\n" +
                        "  right_entry.sending_branch_or_reason as sending_branch_or_reason_detail,\n" +
                        "  right_entry.amount as amount_right\n" +
                        "from numbered left_entry\n" +
                        "left join numbered right_entry on right_entry.rn = left_entry.rn + 1\n" +
                        "where left_entry.rn % 2 = 1"
                )
            );
            reportBean.setReceivables(
                queryBeans(
                    cx,
                    AssetDtl5aReceivableLineItem.class,
                    "select\n" +
                        "coalesce(p.name, tsl.description) as receivables_owed_from,\n" +
                        "tsl.reference as reason,\n" +
                        "tsl.notes as sending_branch_or_reason,\n" +
                        "cast(tsl.amount as varchar) as prior_amount,\n" +
                        "cast(tsl.amount as varchar) as current_amount\n" +
                        "from txn_supplemental_line tsl\n" +
                        "left join person p on p.id = tsl.counterparty_person_id\n" +
                        "where tsl.line_kind = 'RECEIVABLE'\n" +
                        "order by tsl.id"
                )
            );
            reportBean.setPrepaid_expenses(
                queryBeans(
                    cx,
                    AssetDtl5aPrepaidExpenseLineItem.class,
                    "select\n" +
                        "tsl.description as prepaid_expenses_description,\n" +
                        "cast(tsl.amount as varchar) as prior_amount,\n" +
                        "cast(tsl.amount as varchar) as current_amount\n" +
                        "from txn_supplemental_line tsl\n" +
                        "where tsl.line_kind = 'PREPAID_EXPENSE'\n" +
                        "order by tsl.id"
                )
            );
            reportBean.setOther_assets(
                queryBeans(
                    cx,
                    AssetDtl5aOtherAssetLineItem.class,
                    "select\n" +
                        "tsl.description as other_assets_description,\n" +
                        "tsl.reference as reason,\n" +
                        "tsl.notes as show_on,\n" +
                        "cast(tsl.amount as varchar) as prior_amount,\n" +
                        "cast(tsl.amount as varchar) as current_amount\n" +
                        "from txn_supplemental_line tsl\n" +
                        "where tsl.line_kind = 'OTHER_ASSET'\n" +
                        "order by tsl.id"
                )
            );
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException(
                "Failed to load ASSET_DTL_5a line items", ex);
        }

        return List.of(reportBean);
    }

    private <T> List<T> queryBeans(
        Connection cx,
        Class<T> beanClass,
        String sql
    ) throws SQLException
    {
        return JdbcBeanLoader.queryBeans(cx, beanClass, sql, null);
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
        // TODO return the classpath or filesystem path to ASSET_DTL_5a.jrxml
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "ASSET_DTL_5a";
    }
}
