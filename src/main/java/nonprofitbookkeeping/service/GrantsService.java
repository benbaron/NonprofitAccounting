/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * GrantsService.java
 * GrantsService
 */
package nonprofitbookkeeping.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nonprofitbookkeeping.model.Grant;

/**
 * Service class for managing {@link Grant} objects.
 * Grants are persisted using SQL via {@link DatabaseManager}. The service
 * provides methods for grant retrieval, addition, and removal.
 */
public class GrantsService
{
        /** Constructs a new GrantsService. */
        public GrantsService() {
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

	/**
	 * Clears all grants from the in-memory storage of this service instance.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * for a specific service instance.
	 */
        public void clearGrants() {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM grant"))
                {
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error clearing grants", e);
                }
        }
	
}
