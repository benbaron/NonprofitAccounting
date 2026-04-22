package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic JDBC repository scaffold for staging/import tables.
 *
 * @param <TModel> domain record persisted by the repository
 * @param <TId> identifier type used for deletes and lookups
 */
public abstract class AbstractRepository<TModel, TId>
{
    private final String createSql;
    private final String upsertSql;
    private final String listAllSql;
    private final String deleteSql;

    protected AbstractRepository(String createSql, String upsertSql, String listAllSql, String deleteSql)
    {
        this.createSql = createSql;
        this.upsertSql = upsertSql;
        this.listAllSql = listAllSql;
        this.deleteSql = deleteSql;
    }

    public final void upsert(TModel model) throws SQLException
    {
        withConnection(connection -> {
            ensureSchema(connection);
            try (PreparedStatement ps = connection.prepareStatement(upsertSql))
            {
                bindUpsert(ps, model);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public final List<TModel> listAll() throws SQLException
    {
        return withConnection(connection -> {
            ensureSchema(connection);
            List<TModel> rows = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(listAllSql);
                 ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    rows.add(mapRow(rs));
                }
            }
            return rows;
        });
    }

    public final int deleteById(TId id) throws SQLException
    {
        return withConnection(connection -> {
            ensureSchema(connection);
            try (PreparedStatement ps = connection.prepareStatement(deleteSql))
            {
                bindDeleteId(ps, id);
                return ps.executeUpdate();
            }
        });
    }

    protected final <R> R withConnection(SqlFunction<Connection, R> action) throws SQLException
    {
        try (Connection connection = Database.get().getConnection())
        {
            return action.apply(connection);
        }
    }

    protected final <R> R inTransaction(Connection connection, SqlFunction<Connection, R> action) throws SQLException
    {
        boolean auto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try
        {
            R result = action.apply(connection);
            connection.commit();
            return result;
        }
        catch (SQLException ex)
        {
            connection.rollback();
            throw ex;
        }
        finally
        {
            connection.setAutoCommit(auto);
        }
    }

    private void ensureSchema(Connection connection) throws SQLException
    {
        try (Statement statement = connection.createStatement())
        {
            statement.execute(createSql);
            afterEnsureSchema(connection);
        }
    }

    /**
     * Extension point for repositories with multi-table DDL requirements.
     */
    protected void afterEnsureSchema(Connection connection) throws SQLException
    {
        // no-op by default
    }

    protected abstract void bindUpsert(PreparedStatement statement, TModel model) throws SQLException;

    protected abstract TModel mapRow(ResultSet rs) throws SQLException;

    protected void bindDeleteId(PreparedStatement statement, TId id) throws SQLException
    {
        statement.setObject(1, id);
    }

    @FunctionalInterface
    protected interface SqlFunction<T, R>
    {
        R apply(T input) throws SQLException;
    }
}
