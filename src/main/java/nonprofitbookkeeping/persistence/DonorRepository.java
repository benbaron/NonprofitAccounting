
package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.DonorContact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Auto-generated Javadoc
/**
 * Repository responsible for CRUD operations on the {@code donor} table.
 * Donors are keyed by their stable {@link DonorContact#getId() external id}
 * so that UI edits and deletions affect the correct record even when names
 * change.
 */
@ApplicationScoped
public class DonorRepository
{
	
	/** The Constant UPSERT_SQL. */
	private static final String UPSERT_SQL =
		"MERGE INTO donor(external_id, name, email, phone) KEY(external_id) VALUES (?,?,?,?)";
	
	/** The Constant LIST_SQL. */
	private static final String LIST_SQL =
		"SELECT external_id, name, email, phone FROM donor ORDER BY name, external_id";
	
	/** The Constant DELETE_SQL. */
	private static final String DELETE_SQL =
		"DELETE FROM donor WHERE external_id = ?";
	
	/** The Constant FIND_SQL. */
	private static final String FIND_SQL =
		"SELECT external_id, name, email, phone FROM donor WHERE external_id = ?";

	private static final String FIND_NAME_SQL =
		"SELECT name FROM donor WHERE external_id = ?";
	
	/** The Constant DELETE_ALL_SQL. */
	private static final String DELETE_ALL_SQL = "DELETE FROM donor";
	
	/**
	 * Inserts or updates the supplied donor.
	 *
	 * @param donor the donor
	 * @throws SQLException the SQL exception
	 */
	public void upsert(DonorContact donor) throws SQLException
	{
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
		{
			String previousName = findNameByExternalId(c, donor.getId());
			ps.setString(1, donor.getId());
			ps.setString(2, donor.getName());
			ps.setString(3, donor.getEmail());
			ps.setString(4, donor.getPhone());
			ps.executeUpdate();
			if (previousName != null && !previousName.equals(donor.getName()))
			{
				CounterpartySyncAdapter.deleteDonor(c, previousName);
			}
			CounterpartySyncAdapter.syncDonor(c, donor);
		}
		
	}
	
	/**
	 * Deletes the donor identified by {@code externalId}.
	 *
	 * @param externalId the external id
	 * @return {@code true} when a row was removed
	 * @throws SQLException the SQL exception
	 */
	public boolean deleteByExternalId(String externalId) throws SQLException
	{
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(DELETE_SQL))
		{
			String donorName = findNameByExternalId(c, externalId);
			ps.setString(1, externalId);
			boolean deleted = ps.executeUpdate() > 0;
			if (deleted)
			{
				CounterpartySyncAdapter.deleteDonor(c, donorName);
			}
			return deleted;
		}
		
	}
	
	/**
	 * Returns all donors ordered by name.
	 *
	 * @return the list
	 * @throws SQLException the SQL exception
	 */
	public List<DonorContact> list() throws SQLException
	{
		List<DonorContact> donors = new ArrayList<>();
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(LIST_SQL);
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				donors.add(mapRow(rs));
			}
			
		}
		
		return donors;
		
	}
	
	/**
	 * Loads a single donor by its external id.
	 *
	 * @param externalId the external id
	 * @return the optional
	 * @throws SQLException the SQL exception
	 */
	public Optional<DonorContact> findByExternalId(String externalId)
		throws SQLException
	{
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(FIND_SQL))
		{
			ps.setString(1, externalId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				
				if (rs.next())
				{
					return Optional.of(mapRow(rs));
				}
				
			}
			
		}
		
		return Optional.empty();
		
	}
	
	/**
	 * Replaces the stored donors with the supplied collection. Existing rows are removed first.
	 *
	 * @param donors the donors
	 * @throws SQLException the SQL exception
	 */
	public void replaceAll(List<DonorContact> donors) throws SQLException
	{
		
		try (Connection c = Database.get().getConnection())
		{
			c.setAutoCommit(false);
			try
			{
				try (PreparedStatement deleteAll =
					c.prepareStatement(DELETE_ALL_SQL))
				{
					deleteAll.executeUpdate();
				}
				CounterpartySyncAdapter.deleteKind(c, "DONOR");
				
				if (donors != null && !donors.isEmpty())
				{
					
					try (PreparedStatement insert = c.prepareStatement(UPSERT_SQL))
					{
						
						for (DonorContact donor : donors)
						{
							insert.setString(1, donor.getId());
							insert.setString(2, donor.getName());
							insert.setString(3, donor.getEmail());
							insert.setString(4, donor.getPhone());
							insert.addBatch();
							CounterpartySyncAdapter.syncDonor(c, donor);
						}
						
						insert.executeBatch();
					}
					
				}
				
				c.commit();
			}
			catch (SQLException e)
			{
				c.rollback();
				throw e;
			}
			finally
			{
				c.setAutoCommit(true);
			}
		}
		
	}

	private static String findNameByExternalId(Connection c, String externalId)
		throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement(FIND_NAME_SQL))
		{
			ps.setString(1, externalId);
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next() ? rs.getString(1) : null;
			}
		}
	}
	
	/**
	 * Maps the current {@link ResultSet} row to a {@link DonorContact} instance.
	 *
	 * @param rs active result set positioned on the desired row
	 * @return populated donor contact
	 * @throws SQLException if column access fails
	 */
	private static DonorContact mapRow(ResultSet rs) throws SQLException
	{
		DonorContact donor = new DonorContact();
		donor.setId(rs.getString("external_id"));
		donor.setName(rs.getString("name"));
		donor.setEmail(rs.getString("email"));
		donor.setPhone(rs.getString("phone"));
		return donor;
		
	}
	
}
