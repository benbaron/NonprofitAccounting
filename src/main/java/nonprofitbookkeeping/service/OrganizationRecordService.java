package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.OrganizationRecord;
import nonprofitbookkeeping.persistence.records.OrganizationRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for OrganizationRecord operations.
 */
public class OrganizationRecordService
{
    private final OrganizationRecordRepository repository;

    public OrganizationRecordService()
    {
        this(new OrganizationRecordRepository());
    }

    public OrganizationRecordService(OrganizationRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(OrganizationRecord record) throws SQLException
    {
        this.repository.upsert(record);
    }

    public List<OrganizationRecord> listAll() throws SQLException
    {
        return this.repository.listAll();
    }

    public int delete(String organizationId) throws SQLException
    {
        return this.repository.deleteById(organizationId);
    }
}
