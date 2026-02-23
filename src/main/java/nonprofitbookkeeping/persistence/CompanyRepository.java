
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * Repository responsible for persisting and retrieving {@link Company} aggregates
 * from the shared H2 database.  The entire aggregate is serialized into a binary
 * payload that is stored inside the {@code company_store} table alongside basic
 * metadata (name and last updated timestamp).
 */
public class CompanyRepository
{
	
	/**
	 * Simple projection representing a stored company row.
	 *
	 * @param id the id
	 * @param name the name
	 * @param updatedAt the updated at
	 */
	public record CompanyRecord(long id, String name, Instant updatedAt)
	{
	}
	
	/** The Constant INSERT_SQL. */
	private static final String INSERT_SQL =
		"INSERT INTO company_store(name, payload, updated_at) VALUES (?,?,CURRENT_TIMESTAMP)";
	
	/** The Constant UPDATE_SQL. */
	private static final String UPDATE_SQL =
		"UPDATE company_store SET name=?, payload=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
	
	/** The Constant SELECT_SQL. */
	private static final String SELECT_SQL =
		"SELECT payload FROM company_store WHERE id=?";
	
	/** The Constant LIST_SQL. */
	private static final String LIST_SQL =
		"SELECT id, name, updated_at FROM company_store ORDER BY updated_at DESC, id DESC";
	
	/** The Constant DELETE_SQL. */
	private static final String DELETE_SQL =
		"DELETE FROM company_store WHERE id=?";
	
	/**
	 * Persists the supplied {@link Company}. When {@code companyId} is {@code null}
	 * a new row is inserted and the generated identifier is returned. When an id is
	 * supplied the existing row is replaced.
	 *
	 * @param companyId optional identifier of the company row to replace
	 * @param company   aggregate to serialize and store
	 * @return the identifier of the stored company row
	 * @throws SQLException if the database update fails
	 * @throws IOException  if the aggregate cannot be serialized
	 */
	public long save(Long companyId, Company company)
		throws SQLException, IOException
	{
		Objects.requireNonNull(company, "company");
		byte[] payload = serialize(company);
		String name = deriveName(company);
		
		try (Connection connection = Database.get().getConnection())
		{
			
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
					throw new SQLException(
						"No company found with id=" + companyId);
				}
				
				return companyId;
			}
			
		}
		
	}
	
	/**
	 * Loads the {@link Company} aggregate for the supplied identifier.
	 *
	 * @param companyId identifier of the company row
	 * @return the deserialized aggregate
	 * @throws SQLException if the database query fails
	 * @throws IOException  if deserialization fails
	 */
	public Company load(long companyId) throws SQLException, IOException
	{
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement ps = connection.prepareStatement(SELECT_SQL))
		{
			ps.setLong(1, companyId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				
				if (!rs.next())
				{
					throw new SQLException("Company not found: " + companyId);
				}
				
				byte[] payload = rs.getBytes(1);
				return deserialize(payload);
			}
			
		}
		catch (ClassNotFoundException e)
		{
			throw new IOException("Failed to deserialize company payload", e);
		}
		
	}
	
	/**
	 * Returns metadata for all stored companies ordered by most recent update.
	 *
	 * @return the list
	 * @throws SQLException the SQL exception
	 */
	public List<CompanyRecord> listCompanies() throws SQLException
	{
		List<CompanyRecord> companies = new ArrayList<>();
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement ps = connection.prepareStatement(LIST_SQL);
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				long id = rs.getLong("id");
				String name = rs.getString("name");
				Timestamp ts = rs.getTimestamp("updated_at");
				companies.add(new CompanyRecord(id, name,
					ts == null ? null : ts.toInstant()));
			}
			
		}
		
		return companies;
		
	}
	
	/**
	 * Deletes the stored company payload.
	 *
	 * @param companyId the company id
	 * @throws SQLException the SQL exception
	 */
	public void delete(long companyId) throws SQLException
	{
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement ps = connection.prepareStatement(DELETE_SQL))
		{
			ps.setLong(1, companyId);
			ps.executeUpdate();
		}
		
	}
	
	/**
	 * Exports the raw serialized payload for the supplied company id.
	 *
	 * @param companyId the company id
	 * @return the byte[]
	 * @throws SQLException the SQL exception
	 */
	public byte[] exportCompany(long companyId) throws SQLException
	{
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement ps = connection.prepareStatement(SELECT_SQL))
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
		
		throw new SQLException("Company not found: " + companyId);
		
	}
	
	/**
	 * Imports a serialized payload that was previously exported via {@link #exportCompany(long)}.
	 *
	 * @param name    display name to associate with the company
	 * @param payload serialized company bytes
	 * @return identifier of the newly inserted company row
	 * @throws SQLException if the database update fails
	 */
	public long importCompany(String name, byte[] payload) throws SQLException
	{
		Objects.requireNonNull(payload, "payload");
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement ps = connection.prepareStatement(INSERT_SQL,
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
		
		throw new SQLException("Failed to import company payload");
		
	}
	
	/**
	 * Serializes a {@link Company} aggregate into a byte array for storage.
	 *
	 * @param company aggregate to serialize
	 * @return serialized payload
	 * @throws IOException if the object cannot be written
	 */
	private static byte[] serialize(Company company) throws IOException
	{
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			oos.writeObject(company);
			oos.flush();
			return baos.toByteArray();
		}
		
	}
	
	/**
	 * Deserializes the stored company payload back into an aggregate instance.
	 *
	 * @param payload serialized bytes
	 * @return reconstructed {@link Company}
	 * @throws IOException            if deserialization fails
	 * @throws ClassNotFoundException if referenced classes cannot be resolved
	 */
	private static Company deserialize(byte[] payload)
		throws IOException, ClassNotFoundException
	{
		
		if (payload == null)
		{
			throw new IOException("Company payload was null");
		}
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(payload);
			ObjectInputStream ois = new ObjectInputStream(bais))
		{
			return (Company) ois.readObject();
		}
		
	}
	
	/**
	 * Derives a friendly display name for the company using profile information when
	 * available. Falls back to the aggregate name or a default placeholder.
	 *
	 * @param company aggregate from which to pull the name
	 * @return resolved company name to persist
	 */
	private static String deriveName(Company company)
	{
		
		if (company.getCompanyProfileModel() != null &&
			company.getCompanyProfileModel().getCompanyName() != null &&
			!company.getCompanyProfileModel().getCompanyName().isBlank())
		{
			return company.getCompanyProfileModel().getCompanyName();
		}
		
		return company.getName() == null || company.getName().isBlank() ?
			"Untitled Company" : company.getName();
		
	}
	
}
