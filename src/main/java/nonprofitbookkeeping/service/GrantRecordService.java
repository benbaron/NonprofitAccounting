package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.persistence.GrantRecordRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service for grant records persisted in {@code grant_record}.
 */
public class GrantRecordService
{
    private final GrantRecordRepository repository;

    public GrantRecordService()
    {
        this(new GrantRecordRepository());
    }

    public GrantRecordService(GrantRecordRepository repository)
    {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public List<Grant> listAll() throws SQLException
    {
        return this.repository.listStandaloneGrants();
    }

    public void saveAll(List<Grant> grants) throws SQLException
    {
        this.repository.replaceStandaloneGrants(grants == null ? List.of() : grants);
    }

    public void save(Grant grant) throws SQLException
    {
        if (grant == null)
        {
            return;
        }
        List<Grant> grants = new ArrayList<>(listAll());
        grants.removeIf(existing -> existing.getGrantId() != null
            && existing.getGrantId().equals(grant.getGrantId()));
        grants.add(grant);
        saveAll(grants);
    }

    public boolean deleteByGrantId(String grantId) throws SQLException
    {
        if (grantId == null || grantId.isBlank())
        {
            return false;
        }
        List<Grant> grants = new ArrayList<>(listAll());
        boolean removed = grants.removeIf(existing -> grantId.equals(existing.getGrantId()));
        if (removed)
        {
            saveAll(grants);
        }
        return removed;
    }
}
