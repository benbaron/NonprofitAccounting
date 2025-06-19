/**
 * nonprofit-scaledger-ribbon.zip_expanded GrantsService.java GrantsService
 */

package nonprofitbookkeeping.service;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import java.io.File;
import java.io.IOException;
=======
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
>>>>>>> b1f07f2 Extend SQL support
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

import nonprofitbookkeeping.model.Grant;

/**
 * Service class for managing {@link Grant} objects.
 * Grants are persisted using SQL via {@link DatabaseManager}. The service
 * provides methods for grant retrieval, addition, and removal.
 */
public class GrantsService
{
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	/** Shared list storing grants across service instances. */
	private static final List<Grant> SHARED_GRANTS = new ArrayList<>();
	
	/** Logger for this service. */
	private static final Logger LOGGER = Logger.getLogger(GrantsService.class.getName());
	
	/** Filename used to persist grants inside the company zip. */
	private static final String GRANTS_FILENAME = "grants.json";
	
	/** In-memory list to store {@link Grant} objects. */
	private List<Grant> grants;
	
	/**
	 * Constructs a new GrantsService, initializing an empty list for storing grants.
	 */
	public GrantsService()
	{
		this.grants = SHARED_GRANTS;
	}
	
=======
        /** Constructs a new GrantsService. */
        public GrantsService() {
        }

>>>>>>> b1f07f2 Extend SQL support
	/**
	 * Retrieves all grants currently stored in this service instance.
	 *
	 * @return A new {@code List<Grant>} containing all stored grants.
	 *         Returns an empty list if no grants are present. This is a copy,
	 *         so modifications to the returned list do not affect internal storage.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public List<Grant> getAllGrants()
	{
		
		// Return a copy to prevent external modification
		if (this.grants == null)
		{ // Should be initialized by constructor, but defensive
			this.grants = new ArrayList<>();
		}
		
		return new ArrayList<>(this.grants);
	}
	
=======
        public List<Grant> getAllGrants()
        {
                List<Grant> list = new ArrayList<>();
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "SELECT grant_id,grantor,amount,date_awarded,purpose,status FROM grant"))
                {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                Grant g = new Grant();
                                g.setGrantId(rs.getString(1));
                                g.setGrantor(rs.getString(2));
                                g.setAmount(rs.getBigDecimal(3));
                                Date d = rs.getDate(4);
                                if (d != null) g.setDateAwarded(d.toString());
                                g.setPurpose(rs.getString(5));
                                g.setStatus(rs.getString(6));
                                list.add(g);
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error loading grants", e);
                }
                return list;
        }

>>>>>>> b1f07f2 Extend SQL support
	/**
	 * Adds a new grant to the in-memory storage for this service instance.
	 * If the provided grant is null, or its ID (obtained via {@code getGrantId()})
	 * is null or blank, the grant is not added. This implementation allows
	 * duplicate grants if their IDs are the same (as List allows duplicates).
	 *
	 * @param grant The {@link Grant} object to be added. Must not be null
	 *              and must have a valid, non-blank ID.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public void addGrant(Grant grant)
	{
		
		if (grant == null || grant.getGrantId() == null || grant.getGrantId().trim().isEmpty())
		{
			// Optionally, log a warning here
			return;
		}
		
		if (this.grants == null)
		{ // Defensive, should be initialized by constructor
			this.grants = new ArrayList<>();
		}
		
		this.grants.add(grant);
	}
	
=======
        public void addGrant(Grant grant) {
                if (grant == null || grant.getGrantId() == null || grant.getGrantId().trim().isEmpty()) {
                        return;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "MERGE INTO grant(grant_id,grantor,amount,date_awarded,purpose,status) KEY(grant_id) VALUES(?,?,?,?,?,?)"))
                {
                        ps.setString(1, grant.getGrantId());
                        ps.setString(2, grant.getGrantor());
                        ps.setBigDecimal(3, grant.getAmount());
                        if (grant.getDateAwarded() != null && !grant.getDateAwarded().isEmpty()) {
                                ps.setDate(4, Date.valueOf(grant.getDateAwarded()));
                        } else {
                                ps.setDate(4, null);
                        }
                        ps.setString(5, grant.getPurpose());
                        ps.setString(6, grant.getStatus());
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error adding grant", e);
                }
        }

>>>>>>> b1f07f2 Extend SQL support
	/**
	 * Removes a grant from the in-memory storage of this service instance based on its ID.
	 * If the provided grant ID is null or blank, no action is taken.
	 *
	 * @param grantId The unique identifier (grant ID) of the grant to be removed.
	 * @return {@code true} if a grant was removed as a result of this call,
	 *         {@code false} otherwise (including if the grantId is invalid or
	 *         no such grant was found).
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public boolean removeGrant(String grantId)
	{
		
		if (grantId == null || grantId.trim().isEmpty() || this.grants == null)
		{
			return false;
		}
		
		return this.grants.removeIf(grant -> grantId.equals(grant.getGrantId()));
	}
	
=======
        public boolean removeGrant(String grantId) {
                if (grantId == null || grantId.trim().isEmpty()) {
                        return false;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM grant WHERE grant_id=?"))
                {
                        ps.setString(1, grantId);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        throw new RuntimeException("Error removing grant", e);
                }
        }

>>>>>>> b1f07f2 Extend SQL support
	/**
	 * Clears all grants from the in-memory storage of this service instance.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * for a specific service instance.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public void clearGrants()
	{
		
		if (this.grants != null)
		{
			this.grants.clear();
		}
		else
		{
			// Should not happen if initialized by constructor, but defensive
			this.grants = new ArrayList<>();
		}
		
	}
	
	/**
	 * Saves all grants to a JSON file located in the given company directory.
	 *
	 * @param companyDirectory directory where the grants file should be written
	 * @throws IOException if writing fails or the directory is invalid
	 */
	public void saveGrants(File companyDirectory) throws IOException
	{
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File target = new File(companyDirectory, GRANTS_FILENAME);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		try
		{
			mapper.writeValue(target, getAllGrants());
		}
		catch (IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Failed to save grants to " + target.getAbsolutePath(), ex);
			throw ex;
		}
		
	}
	
	/**
	 * Loads grants from a JSON file located in the given company directory.
	 * Existing in-memory grants are cleared before loading new ones. If the
	 * file does not exist, this method simply returns with an empty list.
	 *
	 * @param companyDirectory directory where the grants file is located
	 * @throws IOException if reading fails or the directory is invalid
	 */
        public void loadGrants(File companyDirectory) throws IOException
        {
                this.grants.clear();
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File target = new File(companyDirectory, GRANTS_FILENAME);
		
		if (!target.exists() || target.length() == 0)
		{
			return; // nothing to load
		}
		
		ObjectMapper mapper = new ObjectMapper();
		CollectionType listType =
			mapper.getTypeFactory().constructCollectionType(List.class, Grant.class);
		
		try
		{
			List<Grant> loaded = mapper.readValue(target, listType);
			this.grants.addAll(loaded);
		}
		catch (IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Failed to load grants from " + target.getAbsolutePath(), ex);
			throw ex;
                }

        }

        /**
         * Placeholder for future database-backed persistence. Currently this
         * method performs no operations but allows callers to persist grants
         * without providing filesystem paths.
         *
         * @param companyId identifier of the company whose grants should be saved
         */
        public void saveGrantsToDatabase(String companyId)
        {
                // TODO: Implement database persistence of grants
        }

        /**
         * Placeholder for loading grants from the database. This implementation
         * currently performs no work and simply clears in-memory grants.
         *
         * @param companyId identifier of the company whose grants should be loaded
         */
        public void loadGrantsFromDatabase(String companyId)
        {
                // TODO: Implement database retrieval of grants
        }
	
	/**
	 * Saves all grants as a {@link ZipEntry} named {@code grants.json}
	 * inside the provided <code>.npbk</code> company file. Existing entries
	 * in the zip are preserved.
	 *
	 * @param companyFile the <code>.npbk</code> file representing the company
	 * @throws IOException if the file is invalid or a write error occurs
	 */
	public void saveGrantsToZip(File companyFile) throws IOException
	{
		
		if (companyFile == null || companyFile.isDirectory())
		{
			throw new IOException("Company file is invalid or not provided.");
		}
		
		File temp = File.createTempFile("grants", ".npbk");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		byte[] json = mapper.writeValueAsBytes(getAllGrants());
		
		try (
			var zis = companyFile.exists() ?
				new java.util.zip.ZipInputStream(new java.io.FileInputStream(companyFile)) : null;
			var zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(temp)))
		{
			byte[] buf = new byte[4096];
			
			if (zis != null)
			{
				java.util.zip.ZipEntry e;
				
				while ((e = zis.getNextEntry()) != null)
				{
					
					if (!GRANTS_FILENAME.equals(e.getName()))
					{
						zos.putNextEntry(new java.util.zip.ZipEntry(e.getName()));
						int len;
						
						while ((len = zis.read(buf)) > 0)
						{ zos.write(buf, 0, len); }
						
						zos.closeEntry();
					}
					
					zis.closeEntry();
				}
				
			}
			
			java.util.zip.ZipEntry newEntry = new java.util.zip.ZipEntry(GRANTS_FILENAME);
			zos.putNextEntry(newEntry);
			zos.write(json);
			zos.closeEntry();
		}
		
		java.nio.file.Files.move(temp.toPath(), companyFile.toPath(),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Loads grants from the {@code grants.json} entry inside the given
	 * <code>.npbk</code> company file. If the entry does not exist, the
	 * current in-memory list is cleared.
	 *
	 * @param companyFile the <code>.npbk</code> company file
	 * @throws IOException if the file is invalid or a read error occurs
	 */
	public void loadGrantsFromZip(File companyFile) throws IOException
	{
		this.grants.clear();
		
		if (companyFile == null || !companyFile.isFile())
		{
			throw new IOException("Company file is invalid or not provided.");
		}
		
		try (var zis = new java.util.zip.ZipInputStream(new java.io.FileInputStream(companyFile)))
		{
			java.util.zip.ZipEntry e;
			
			while ((e = zis.getNextEntry()) != null)
			{
				
				if (GRANTS_FILENAME.equals(e.getName()))
				{
					ObjectMapper mapper = new ObjectMapper();
					CollectionType listType =
						mapper.getTypeFactory().constructCollectionType(List.class, Grant.class);
					List<Grant> loaded = mapper.readValue(zis, listType);
					this.grants.addAll(loaded);
					break;
				}
				
				zis.closeEntry();
			}
			
		}
		
	}
=======
        public void clearGrants() {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM grant"))
                {
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error clearing grants", e);
                }
        }
>>>>>>> b1f07f2 Extend SQL support
	
}
