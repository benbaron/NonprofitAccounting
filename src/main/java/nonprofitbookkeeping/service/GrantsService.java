/**
 * nonprofit-scaledger-ribbon.zip_expanded GrantsService.java GrantsService
 */

package nonprofitbookkeeping.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.persistence.JsonStorageRepository;

/**
 * Service class for managing {@link Grant} objects.
 * This service provides an in-memory storage solution for grants
 * and includes methods for grant retrieval, addition, and removal.
 * Each instance of this service manages its own list of grants.
 */
public class GrantsService
{
        /** Logger for this service. */
        private static final Logger LOGGER = Logger.getLogger(GrantsService.class.getName());
	
        /** Logical storage key used for persisting grants in the shared database. */
        private static final String STORAGE_KEY = "grants";
        private static final ObjectMapper MAPPER = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        private static final CollectionType LIST_TYPE =
                MAPPER.getTypeFactory().constructCollectionType(List.class, Grant.class);

        /** In-memory list to store {@link Grant} objects. */
        private List<Grant> grants;
        private final JsonStorageRepository jsonRepository = new JsonStorageRepository();
	
	/**
	 * Constructs a new GrantsService, initializing an empty list for storing grants.
	 */
        public GrantsService()
        {
                this.grants = new ArrayList<>();
        }
	
	/**
	 * Retrieves all grants currently stored in this service instance.
	 *
	 * @return A new {@code List<Grant>} containing all stored grants.
	 *         Returns an empty list if no grants are present. This is a copy,
	 *         so modifications to the returned list do not affect internal storage.
	 */
	public List<Grant> getAllGrants()
	{
		
		// Return a copy to prevent external modification
		if (this.grants == null)
		{ // Should be initialized by constructor, but defensive
			this.grants = new ArrayList<>();
		}
		
		return new ArrayList<>(this.grants);
	}
	
	/**
	 * Adds a new grant to the in-memory storage for this service instance.
	 * If the provided grant is null, or its ID (obtained via {@code getGrantId()})
	 * is null or blank, the grant is not added. This implementation allows
	 * duplicate grants if their IDs are the same (as List allows duplicates).
	 *
	 * @param grant The {@link Grant} object to be added. Must not be null
	 *              and must have a valid, non-blank ID.
	 */
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
	
	/**
	 * Removes a grant from the in-memory storage of this service instance based on its ID.
	 * If the provided grant ID is null or blank, no action is taken.
	 *
	 * @param grantId The unique identifier (grant ID) of the grant to be removed.
	 * @return {@code true} if a grant was removed as a result of this call,
	 *         {@code false} otherwise (including if the grantId is invalid or
	 *         no such grant was found).
	 */
	public boolean removeGrant(String grantId)
	{
		
		if (grantId == null || grantId.trim().isEmpty() || this.grants == null)
		{
			return false;
		}
		
		return this.grants.removeIf(grant -> grantId.equals(grant.getGrantId()));
	}
	
	/**
	 * Clears all grants from the in-memory storage of this service instance.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * for a specific service instance.
	 */
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
         * Saves all grants to the persistent document store.
         *
         * @param companyDirectory retained for backwards compatibility but ignored by the method
         * @throws IOException if writing to the database fails
         */
        public void saveGrants() throws IOException
        {
                saveGrants((File) null);
        }

        /**
         * Saves all grants to the persistent document store.
         *
         * @param companyDirectory retained for backwards compatibility but ignored by the method
         * @throws IOException if writing to the database fails
         */
        public void saveGrants(File companyDirectory) throws IOException
        {

                try
                {
                        String payload = MAPPER.writeValueAsString(getAllGrants());
                        this.jsonRepository.save(STORAGE_KEY, payload);
                        LOGGER.info("Grants saved to database storage key '" + STORAGE_KEY + "'.");
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save grants to database", e);
                }

        }
	
        /**
         * Loads grants from the persistent document store.
         * Existing in-memory grants are cleared before loading new ones.
         *
         * @param companyDirectory retained for backwards compatibility but ignored by the method
         * @throws IOException if reading from the database fails
         */
        public void loadGrants() throws IOException
        {
                if (this.grants == null)
                {
                        this.grants = new ArrayList<>();
                }
                else
                {
                        this.grants.clear();
                }

                try
                {
                        this.jsonRepository.load(STORAGE_KEY)
                                .ifPresent(payload -> {
                                        try
                                        {
                                                List<Grant> loaded = MAPPER.readValue(payload, LIST_TYPE);
                                                this.grants.addAll(loaded);
                                                LOGGER.info("Grants loaded from database storage key '" + STORAGE_KEY
                                                        + "'.");
                                        }
                                        catch (IOException ex)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Failed to deserialize grants JSON from database", ex);
                                        }
                                });
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load grants from database", e);
                }

        }

        /**
         * Loads grants from the persistent document store.
         * Existing in-memory grants are cleared before loading new ones.
         *
         * @param companyDirectory retained for backwards compatibility but ignored by the method
         * @throws IOException if reading from the database fails
         */
        public void loadGrants(File companyDirectory) throws IOException
        {
                loadGrants();
        }

        /**
         * Legacy API retained for compatibility with callers that previously wrote
         * grants into a company zip file. The implementation now simply persists the
         * current in-memory grants to the shared database via {@link JsonStorageRepository}.
         *
         * @param companyFile ignored
         * @throws IOException if persisting grants fails
         */
        public void saveGrantsToZip(File companyFile) throws IOException
        {

                saveGrants();

        }

        /**
         * Legacy API retained for compatibility with callers that previously read grants
         * from a company zip file. The implementation now loads the grants directly from
         * the shared database via {@link JsonStorageRepository}.
         *
         * @param companyFile ignored
         * @throws IOException if loading grants fails
         */
        public void loadGrantsFromZip(File companyFile) throws IOException
        {
                loadGrants();
        }
	
}
