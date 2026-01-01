package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMap;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapLoader;
import nonprofitbookkeeping.reports.jasper.runtime.JdbcBeanLoader;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.runtime.ReportSqlFilters;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base report generator that loads report beans via JDBC based on fieldmaps.
 */
public abstract class JdbcReportGenerator<B> extends AbstractReportGenerator
    implements ReportContextAware
{
    private ReportContext context;

    @Override
    public void setReportContext(ReportContext context) {
        this.context = context;
    }

    protected ReportContext getReportContext() {
        return this.context;
    }

    @Override
    protected List<B> getReportData() {
        String selectList = buildSelectList();
        ReportSqlFilters filters = ReportSqlFilters.fromContext(this.context);
        String sql = "select\n" +
            selectList + "\n" +
            getFromClause() + "\n" +
            filters.whereClause() + "\n" +
            getOrderByClause() +
            (allowMultipleRows() ? "" : "\nlimit 1");

        try (Connection cx = Database.get().getConnection()) {
            return JdbcBeanLoader.queryBeans(cx, resolveBeanClass(), sql,
                filters.parameterSetter());
        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Unable to load report data for " + getBaseName(), ex);
        }
    }

    protected String getFromClause() {
        return "from journal_transaction jt\n" +
            "left join journal_entry je on jt.id = je.txn_id\n" +
            "left join account a on a.account_number = je.account_number\n" +
            "left join account_fund af on af.account_number = a.account_number";
    }

    protected String getOrderByClause() {
        return "order by jt.booking_ts, jt.id";
    }

    protected boolean allowMultipleRows() {
        return false;
    }

    protected String getFieldMapResource() {
        return "/nonprofitbookkeeping/reports/" + getBaseName() + "_fieldmap.csv";
    }

    private String buildSelectList() {
        FieldMap fieldMap = loadFieldMap();
        if (fieldMap != null) {
            String selectList = fieldMap.buildSelectListWithFallback("null");
            if (selectList != null && !selectList.isBlank()) {
                return selectList;
            }
        }
        return buildSelectListFromBeanFields();
    }

    private FieldMap loadFieldMap() {
        try {
            return FieldMapLoader.loadFromResource(getFieldMapResource());
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildSelectListFromBeanFields() {
        Class<B> beanClass = resolveBeanClass();
        List<String> columns = new ArrayList<>();
        for (Field field : beanClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            columns.add("null as " + field.getName());
        }
        return String.join(",\n", columns);
    }

    @SuppressWarnings("unchecked")
    private Class<B> resolveBeanClass() {
        String beanClassName = "nonprofitbookkeeping.reports.jasper.beans." +
            getBaseName() + "Bean";
        try {
            return (Class<B>) Class.forName(beanClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                "Unable to locate bean class " + beanClassName, ex);
        }
    }
}
