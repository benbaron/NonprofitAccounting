/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * GrantsService.java
 * GrantsService
 */
package nonprofitbookkeeping.service;

import java.util.ArrayList;
import java.util.List;
import nonprofitbookkeeping.model.Grant;

/**
 * Service class for managing {@link Grant} objects.
 * This service provides an in-memory storage solution for grants
 * and includes methods for grant retrieval, addition, and removal.
 * Each instance of this service manages its own list of grants.
 */
public class GrantsService
{
	/**
	 * In-memory list to store Grant objects for this instance.
	 */
	private List<Grant> grants;

	/**
	 * Constructs a new GrantsService, initializing an empty list for storing grants.
	 */
	public GrantsService() {
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
		if (this.grants == null) { // Should be initialized by constructor, but defensive
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
	public void addGrant(Grant grant) {
		if (grant == null || grant.getGrantId() == null || grant.getGrantId().trim().isEmpty()) {
			// Optionally, log a warning here
			return;
		}
		if (this.grants == null) { // Defensive, should be initialized by constructor
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
	public boolean removeGrant(String grantId) {
		if (grantId == null || grantId.trim().isEmpty() || this.grants == null) {
			return false;
		}
		return this.grants.removeIf(grant -> grantId.equals(grant.getGrantId()));
	}

	/**
	 * Clears all grants from the in-memory storage of this service instance.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * for a specific service instance.
	 */
	public void clearGrants() {
		if (this.grants != null) {
			this.grants.clear();
		} else {
			// Should not happen if initialized by constructor, but defensive
			this.grants = new ArrayList<>();
		}
	}
	
}
