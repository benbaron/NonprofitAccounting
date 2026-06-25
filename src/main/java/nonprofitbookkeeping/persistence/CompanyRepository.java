package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Stores serialized company aggregates and their selection metadata. */
@ApplicationScoped
public class CompanyRepository
{
    public record CompanyRecord(long id, String name, Instant updatedAt,
        Instant lastOpenedAt, boolean archived)
    {
        public CompanyRecord(long id, String name, Instant updatedAt)
        {
            this(id, name, updatedAt, null, false);
        }

        public String status()
        {
            return this.archived ? "Archived" : "Available";
        }
    }

    private static final String INSERT_SQL =
        "INSERT INTO company_store(name, payload, updated_at, archived) " +
            "VALUES (?,?,CURRENT_TIMESTAMP,FALSE)";
    private static final String UPDATE_SQL =
        "UPDATE company_store SET name=?, payload=?, " +
            "updated_at=CURRENT_TIMESTAMP WHERE id=?";
    private static final String SELECT_SQL =
        "SELECT payload FROM company_store WHERE id=?";
    private static final String LIST_SQL =
        "SELECT id, name, updated_at, last_opened_at, archived " +
            "FROM company_store ORDER BY archived, " +
            "COALESCE(last_opened_at, updated_at) DESC, id DESC";
    private static final String DELETE_SQL =
        "DELETE FROM company_store WHERE id=?";

    public long save(Long companyId, Company company)
        throws SQLException, IOException
    {
        Objects.requireNonNull(company, "company");
        byte[] payload = serialize(company);
        String name = deriveName(company);

        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            if (companyId == null)
            {
                try (PreparedStatement ps = connection.prepareStatement(
                    INSERT_SQL, Statement.RETURN_GENERATED_KEYS))
                {
                    ps.setString(1, name);
                    ps.setBytes(2, payload);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys())
                    {
                        if (keys.next())
                        {
                            return keys.getLong(1);
                        }
                    }
                }
                throw new SQLException(
                    "Failed to retrieve generated company id");
            }

            try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL))
            {
                ps.setString(1, name);
                ps.setBytes(2, payload);
                ps.setLong(3, companyId);
                if (ps.executeUpdate() == 0)
                {
                    throw new SQLException("No company found with id=" +
                        companyId);
                }
                return companyId;
            }
        }
    }

    public Company load(long companyId) throws SQLException, IOException
    {
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(SELECT_SQL))
            {
                ps.setLong(1, companyId);
                try (ResultSet rs = ps.executeQuery())
                {
                    if (!rs.next())
                    {
                        throw new SQLException("Company not found: " +
                            companyId);
                    }
                    return deserialize(rs.getBytes(1));
                }
                catch (ClassNotFoundException ex)
                {
                    throw new IOException(
                        "Failed to deserialize company payload", ex);
                }
            }
        }
    }

    public List<CompanyRecord> listCompanies() throws SQLException
    {
        List<CompanyRecord> companies = new ArrayList<>();
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(LIST_SQL);
                ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    companies.add(new CompanyRecord(
                        rs.getLong("id"),
                        rs.getString("name"),
                        instant(rs.getTimestamp("updated_at")),
                        instant(rs.getTimestamp("last_opened_at")),
                        rs.getBoolean("archived")));
                }
            }
        }
        return companies;
    }

    public void markOpened(long companyId) throws SQLException
    {
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE company_store SET last_opened_at=CURRENT_TIMESTAMP " +
                    "WHERE id=?"))
            {
                ps.setLong(1, companyId);
                ps.executeUpdate();
            }
        }
    }

    public void setArchived(long companyId, boolean archived)
        throws SQLException
    {
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE company_store SET archived=? WHERE id=?"))
            {
                ps.setBoolean(1, archived);
                ps.setLong(2, companyId);
                ps.executeUpdate();
            }
        }
    }

    public void delete(long companyId) throws SQLException
    {
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(DELETE_SQL))
            {
                ps.setLong(1, companyId);
                ps.executeUpdate();
            }
        }
    }

    public byte[] exportCompany(long companyId) throws SQLException
    {
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(SELECT_SQL))
            {
                ps.setLong(1, companyId);
                try (ResultSet rs = ps.executeQuery())
                {
                    if (rs.next())
                    {
                        return rs.getBytes(1);
                    }
                }
            }
        }
        throw new SQLException("Company not found: " + companyId);
    }

    public long importCompany(String name, byte[] payload) throws SQLException
    {
        Objects.requireNonNull(payload, "payload");
        try (Connection connection = Database.get().getConnection())
        {
            ensureMetadataColumns(connection);
            try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL,
                Statement.RETURN_GENERATED_KEYS))
            {
                ps.setString(1, name == null ? "Imported Company" : name);
                ps.setBytes(2, payload);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys())
                {
                    if (keys.next())
                    {
                        return keys.getLong(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to import company payload");
    }

    private static void ensureMetadataColumns(Connection connection)
        throws SQLException
    {
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate(
                "ALTER TABLE company_store ADD COLUMN IF NOT EXISTS " +
                    "last_opened_at TIMESTAMP");
            statement.executeUpdate(
                "ALTER TABLE company_store ADD COLUMN IF NOT EXISTS " +
                    "archived BOOLEAN DEFAULT FALSE NOT NULL");
        }
    }

    private static Instant instant(Timestamp timestamp)
    {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static byte[] serialize(Company company) throws IOException
    {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream objects = new ObjectOutputStream(output))
        {
            objects.writeObject(company);
            objects.flush();
            return output.toByteArray();
        }
    }

    private static Company deserialize(byte[] payload)
        throws IOException, ClassNotFoundException
    {
        if (payload == null)
        {
            throw new IOException("Company payload was null");
        }
        try (ByteArrayInputStream input = new ByteArrayInputStream(payload);
            ObjectInputStream objects = new ObjectInputStream(input))
        {
            return (Company) objects.readObject();
        }
    }

    private static String deriveName(Company company)
    {
        if (company.getCompanyProfileModel() != null &&
            company.getCompanyProfileModel().getCompanyName() != null &&
            !company.getCompanyProfileModel().getCompanyName().isBlank())
        {
            return company.getCompanyProfileModel().getCompanyName();
        }
        return company.getName() == null || company.getName().isBlank()
            ? "Untitled Company" : company.getName();
    }
}
