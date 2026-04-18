package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.SupplyRecord;
import nonprofitbookkeeping.persistence.records.SupplyRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for SupplyRecord operations.
 */
public class SupplyRecordService
{
    private final SupplyRecordRepository repository;

    public SupplyRecordService()
    {
        this(new SupplyRecordRepository());
    }

    public SupplyRecordService(SupplyRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(SupplyRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<SupplyRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }
}
