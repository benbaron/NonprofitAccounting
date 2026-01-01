package nonprofitbookkeeping.reports.jasper.runtime;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base generator that derives its data requirements from JRXML + fieldmap CSV.
 */
public abstract class FieldMappedReportGenerator<B>
    extends AbstractReportGenerator
{
    private static final Logger LOGGER =
        Logger.getLogger(FieldMappedReportGenerator.class.getName());
    private final ReportContext context;

    protected FieldMappedReportGenerator()
    {
        this(null);
    }

    protected FieldMappedReportGenerator(ReportContext context)
    {
        this.context = context;
    }

    protected abstract Class<B> getBeanClass();

    protected ReportContext getContext()
    {
        return this.context;
    }

    protected String getFieldMapResource()
    {
        return "/nonprofitbookkeeping/reports/" + getBaseName() +
            "_fieldmap.csv";
    }

    @Override
    protected List<B> getReportData()
    {
        FieldMap fieldMap = loadFieldMap();
        JrxmlFieldParser.JrxmlFields jrxmlFields = loadJrxmlFields();

        List<String> orderedFields = resolveFieldOrder(fieldMap, jrxmlFields);
        if (orderedFields.isEmpty())
        {
            return List.of();
        }

        if (!Database.isInitialized())
        {
            LOGGER.warning("Database not initialized; no report data.");
            return List.of();
        }

        QuerySpec spec = buildQuerySpec(orderedFields);
        if (spec.sql().isBlank())
        {
            return List.of();
        }

        try (Connection cx = Database.get().getConnection())
        {
            return JdbcBeanLoader.queryBeans(cx, getBeanClass(), spec.sql(),
                spec.paramSetter());
        }
        catch (SQLException e)
        {
            LOGGER.log(Level.WARNING,
                "Failed to query report data for " + getBaseName(), e);
            return List.of();
        }
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        JrxmlFieldParser.JrxmlFields jrxmlFields = loadJrxmlFields();
        if (jrxmlFields == null || jrxmlFields.parameters().isEmpty())
        {
            return Map.of();
        }

        Map<String, Object> params = new HashMap<>();
        for (String param : jrxmlFields.parameters())
        {
            Object value = resolveParameterValue(param);
            if (value != null)
            {
                params.put(param, value);
            }
        }
        return params;
    }

    @Override
    protected String getReportPath()
        throws ActionCancelledException, NoFileCreatedException
    {
        return bundledReportPath();
    }

    private FieldMap loadFieldMap()
    {
        try
        {
            return FieldMapLoader.loadFromResource(getFieldMapResource());
        }
        catch (IOException e)
        {
            LOGGER.log(Level.WARNING,
                "Fieldmap not found for " + getBaseName(), e);
            return null;
        }
    }

    private JrxmlFieldParser.JrxmlFields loadJrxmlFields()
    {
        String reportPath;
        try
        {
            reportPath = getReportPath();
        }
        catch (ActionCancelledException | NoFileCreatedException e)
        {
            return null;
        }

        if (reportPath == null || reportPath.isBlank())
        {
            return null;
        }

        String resourcePath = reportPath.startsWith("/") ?
            reportPath.substring(1) : reportPath;

        try (InputStream input = openStream(resourcePath))
        {
            return JrxmlFieldParser.parse(input);
        }
        catch (IOException e)
        {
            LOGGER.log(Level.WARNING,
                "JRXML not readable for " + getBaseName(), e);
            return null;
        }
    }

    private InputStream openStream(String resourcePath) throws IOException
    {
        InputStream input = getClass().getClassLoader()
            .getResourceAsStream(resourcePath);

        if (input != null)
        {
            return input;
        }

        return new FileInputStream(resourcePath);
    }

    private List<String> resolveFieldOrder(FieldMap fieldMap,
        JrxmlFieldParser.JrxmlFields jrxmlFields)
    {
        Set<String> ordered = new LinkedHashSet<>();

        if (fieldMap != null)
        {
            for (FieldMapEntry entry : fieldMap.getEntries())
            {
                if (entry.getFieldName() != null &&
                    !entry.getFieldName().isBlank())
                {
                    ordered.add(entry.getFieldName());
                }
            }
        }

        if (jrxmlFields != null)
        {
            ordered.addAll(jrxmlFields.fields().keySet());
        }

        return new ArrayList<>(ordered);
    }

    private QuerySpec buildQuerySpec(List<String> fieldNames)
    {
        StringJoiner select = new StringJoiner(",\n");
        for (String field : fieldNames)
        {
            if (field == null || field.isBlank())
            {
                continue;
            }
            String safe = field.replace("\"", "\"\"");
            select.add("max(case when info.k = '" + safe + "' then info.v end) as " +
                safe);
        }

        if (select.length() == 0)
        {
            return new QuerySpec("", null);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("select\n");
        sql.append(select);
        sql.append("\nfrom journal_transaction t\n");
        sql.append("left join transaction_info info on info.txn_id = t.id\n");
        sql.append("where (? is null or t.date_text >= ?)\n");
        sql.append("  and (? is null or t.date_text <= ?)\n");

        ReportContext ctx = getContext();
        List<String> fundIds = ctx == null ? null : ctx.getFundIds();
        if (fundIds != null && !fundIds.isEmpty())
        {
            sql.append("  and t.associated_fund_name in (");
            appendPlaceholders(sql, fundIds.size());
            sql.append(")\n");
        }

        List<String> accountIds = ctx == null ? null :
            ctx.getAccountIdsForDetailReport();
        if (accountIds != null && !accountIds.isEmpty())
        {
            sql.append("  and exists (\n");
            sql.append("    select 1 from journal_entry je\n");
            sql.append("    where je.txn_id = t.id\n");
            sql.append("      and je.account_number in (");
            appendPlaceholders(sql, accountIds.size());
            sql.append(")\n");
            sql.append("  )\n");
        }

        String memoFilter = ctx == null ? null : ctx.getMemoFilter();
        if (memoFilter != null && !memoFilter.isBlank())
        {
            sql.append("  and lower(t.memo) like ?\n");
        }

        sql.append("group by t.id\n");

        return new QuerySpec(sql.toString(),
            buildParamSetter(ctx, fundIds, accountIds, memoFilter));
    }

    private JdbcBeanLoader.SqlParameterSetter buildParamSetter(
        ReportContext ctx,
        List<String> fundIds,
        List<String> accountIds,
        String memoFilter)
    {
        LocalDate start = ctx == null ? null : ctx.getStartDate();
        LocalDate end = ctx == null ? null : ctx.getEndDate();

        return new JdbcBeanLoader.SqlParameterSetter()
        {
            @Override
            public void setParameters(PreparedStatement ps)
                throws SQLException
            {
                int index = 1;

                if (start != null)
                {
                    ps.setObject(index++, start.toString());
                    ps.setObject(index++, start.toString());
                }
                else
                {
                    ps.setNull(index++, Types.VARCHAR);
                    ps.setNull(index++, Types.VARCHAR);
                }

                if (end != null)
                {
                    ps.setObject(index++, end.toString());
                    ps.setObject(index++, end.toString());
                }
                else
                {
                    ps.setNull(index++, Types.VARCHAR);
                    ps.setNull(index++, Types.VARCHAR);
                }

                if (fundIds != null && !fundIds.isEmpty())
                {
                    for (String fund : fundIds)
                    {
                        ps.setString(index++, fund);
                    }
                }

                if (accountIds != null && !accountIds.isEmpty())
                {
                    for (String account : accountIds)
                    {
                        ps.setString(index++, account);
                    }
                }

                if (memoFilter != null && !memoFilter.isBlank())
                {
                    ps.setString(index++, "%" +
                        memoFilter.toLowerCase(Locale.ROOT) + "%");
                }
            }
        };
    }

    private void appendPlaceholders(StringBuilder sql, int count)
    {
        for (int i = 0; i < count; i++)
        {
            if (i > 0)
            {
                sql.append(", ");
            }
            sql.append("?");
        }
    }

    private Object resolveParameterValue(String paramName)
    {
        if (paramName == null || paramName.isBlank())
        {
            return null;
        }

        ReportContext ctx = getContext();
        if (ctx == null)
        {
            return null;
        }

        String normalized = paramName.toLowerCase(Locale.ROOT);

        if (normalized.contains("start") && normalized.contains("date"))
        {
            return ctx.getStartDate();
        }

        if (normalized.contains("end") && normalized.contains("date"))
        {
            return ctx.getEndDate();
        }

        if (normalized.contains("fund"))
        {
            return join(ctx.getFundIds());
        }

        if (normalized.contains("account"))
        {
            return join(ctx.getAccountIdsForDetailReport());
        }

        if (normalized.contains("memo"))
        {
            return ctx.getMemoFilter();
        }

        if (normalized.contains("report") && normalized.contains("type"))
        {
            return ctx.getReportType();
        }

        if (normalized.contains("output") && normalized.contains("format"))
        {
            return ctx.getOutputFormat();
        }

        return null;
    }

    private String join(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }
        return String.join(", ", values);
    }

    private record QuerySpec(String sql,
        JdbcBeanLoader.SqlParameterSetter paramSetter)
    {
    }
}
