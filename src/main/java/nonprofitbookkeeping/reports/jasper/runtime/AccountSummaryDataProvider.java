package nonprofitbookkeeping.reports.runtime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.AccountSummaryRowBean;

/**
 * Example per-report data provider for an Account Summary report.
 *
 * Integration with fieldmap CSV:
 *   - Attempts to load "/reports/AccountSummary_fieldmap.csv" from the classpath.
 *   - If the CSV includes a 6th column (dbExpr), {@link FieldMap#buildSelectListFromDbExprs()}
 *     is used to build the SELECT list.
 *   - If no dbExprs are present or the file is missing, we fall back to a hard-coded
 *     SELECT list that matches AccountSummaryRowBean.
 */
public class AccountSummaryDataProvider
    implements ReportDataProvider<AccountSummaryRowBean> {

    private final FieldMap fieldMap;

    /**
     * Create a provider with an explicit FieldMap (e.g., loaded once at startup).
     */
    public AccountSummaryDataProvider(FieldMap fieldMap) {
        this.fieldMap = fieldMap;
    }

    /**
     * Default constructor: tries to load the fieldmap from the classpath at
     * "/reports/AccountSummary_fieldmap.csv". If not found or invalid, the
     * provider will simply fall back to hard-coded SQL.
     */
    public AccountSummaryDataProvider() {
        FieldMap fm = null;
        try {
            fm = FieldMapLoader.loadFromResource("/reports/AccountSummary_fieldmap.csv");
        } catch (Exception ex) {
            // OK: we will use hard-coded SQL instead.
        }
        this.fieldMap = fm;
    }

    @Override
    public Class<AccountSummaryRowBean> beanClass() {
        return AccountSummaryRowBean.class;
    }

    @Override
    public String sql(ReportContext ctx) {
        // 1) Try to build SELECT from dbExpr in the fieldmap.
        String selectList = null;

        if (fieldMap != null) {
            String fromDbExpr = fieldMap.buildSelectListFromDbExprs();
            if (fromDbExpr != null && !fromDbExpr.trim().isEmpty()) {
                selectList = fromDbExpr;
            }
        }

        // 2) If no dbExprs available, use a hard-coded SELECT that matches the bean.
        if (selectList == null) {
            selectList = """
                a.name            as accountname,
                s.opening_balance as openingbalance,
                s.closing_balance as closingbalance""";
        }

        // 3) Compose the final SQL with a simple date-range filter using JDBC ? parameters.
        //
        // Parameters (in order):
        //   1: startDate (for "? is null or s.tx_date >= ?")
        //   2: startDate
        //   3: endDate
        //   4: endDate
        return "select\n" +
               selectList + "\n" +
               "from v_account_summary s\n" +
               "join accounts a on a.id = s.account_id\n" +
               "where (? is null or s.tx_date >= ?)\n" +
               "  and (? is null or s.tx_date <= ?)\n" +
               "order by a.sort_order";
    }

    @Override
    public JdbcBeanLoader.SqlParameterSetter parameterSetter(ReportContext ctx) {
        final LocalDate start = (ctx != null) ? ctx.getStartDate() : null;
        final LocalDate end   = (ctx != null) ? ctx.getEndDate()   : null;

        return new JdbcBeanLoader.SqlParameterSetter() {
            @Override
            public void setParameters(PreparedStatement ps) throws SQLException {
                if (start != null) {
                    ps.setObject(1, start);
                    ps.setObject(2, start);
                } else {
                    ps.setNull(1, Types.DATE);
                    ps.setNull(2, Types.DATE);
                }

                if (end != null) {
                    ps.setObject(3, end);
                    ps.setObject(4, end);
                } else {
                    ps.setNull(3, Types.DATE);
                    ps.setNull(4, Types.DATE);
                }
            }
        };
    }
}
