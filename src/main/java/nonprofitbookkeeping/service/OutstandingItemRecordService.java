package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.OutstandingItemRecord;
import nonprofitbookkeeping.persistence.records.OutstandingItemRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for OutstandingItemRecord operations.
 */
public class OutstandingItemRecordService
{
    private final OutstandingItemRecordRepository repository;

    public OutstandingItemRecordService()
    {
        this(new OutstandingItemRecordRepository());
    }

    public OutstandingItemRecordService(OutstandingItemRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(OutstandingItemRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<OutstandingItemRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }
}
