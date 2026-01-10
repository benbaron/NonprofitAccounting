package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aOtherAssetLineItem;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aPrepaidExpenseLineItem;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aReceivableLineItem;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aReportBean;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aUndepositedFundsEntry;
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
                buildUndepositedFunds(
                    queryBeans(
                        cx,
                        AssetDtl5aUndepositedFundsEntry.class,
                        "select\n" +
                            "jt.memo as sending_branch_or_reason,\n" +
                            "je.amount as amount\n" +
                            "from journal_transaction jt\n" +
                            "join journal_entry je on je.txn_id = jt.id"
                    )
                )
            );
            reportBean.setReceivables(
                queryBeans(
                    cx,
                    AssetDtl5aReceivableLineItem.class,
                    "select\n" +
                        "jt.memo as receivables_owed_from,\n" +
                        "jt.memo as reason,\n" +
                        "jt.memo as sending_branch_or_reason,\n" +
                        "je.amount as prior_amount,\n" +
                        "je.amount as current_amount\n" +
                        "from journal_transaction jt\n" +
                        "join journal_entry je on je.txn_id = jt.id"
                )
            );
            reportBean.setPrepaid_expenses(
                queryBeans(
                    cx,
                    AssetDtl5aPrepaidExpenseLineItem.class,
                    "select\n" +
                        "jt.memo as prepaid_expenses_description,\n" +
                        "je.amount as prior_amount,\n" +
                        "je.amount as current_amount\n" +
                        "from journal_transaction jt\n" +
                        "join journal_entry je on je.txn_id = jt.id"
                )
            );
            reportBean.setOther_assets(
                queryBeans(
                    cx,
                    AssetDtl5aOtherAssetLineItem.class,
                    "select\n" +
                        "jt.memo as other_assets_description,\n" +
                        "jt.memo as reason,\n" +
                        "jt.memo as show_on,\n" +
                        "je.amount as prior_amount,\n" +
                        "je.amount as current_amount\n" +
                        "from journal_transaction jt\n" +
                        "join journal_entry je on je.txn_id = jt.id"
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

    private List<AssetDtl5aUndepositedFundsLineItem> buildUndepositedFunds(
        List<AssetDtl5aUndepositedFundsEntry> entries
    )
    {
        List<AssetDtl5aUndepositedFundsLineItem> rows = new ArrayList<>();

        for (int i = 0; i < entries.size(); i += 2)
        {
            AssetDtl5aUndepositedFundsEntry left = entries.get(i);
            AssetDtl5aUndepositedFundsEntry right =
                i + 1 < entries.size() ? entries.get(i + 1) : null;

            AssetDtl5aUndepositedFundsLineItem row =
                new AssetDtl5aUndepositedFundsLineItem();
            row.setSending_branch_or_reason_left(
                left.getSending_branch_or_reason());
            row.setAmount_left(left.getAmount());

            if (right != null)
            {
                row.setSending_branch_or_reason_right(
                    right.getSending_branch_or_reason());
                row.setSending_branch_or_reason_detail(
                    right.getSending_branch_or_reason());
                row.setAmount_right(right.getAmount());
            }

            rows.add(row);
        }

        return rows;
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
