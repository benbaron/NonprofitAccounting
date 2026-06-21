package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.EventRecord;
import nonprofitbookkeeping.persistence.records.EventRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for EventRecord operations.
 */
public class EventRecordService
{
    private final EventRecordRepository repository;

    public EventRecordService()
    {
        this(new EventRecordRepository());
    }

    public EventRecordService(EventRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(EventRecord record) throws SQLException
    {
        this.repository.upsert(record);
    }

    public List<EventRecord> listAll() throws SQLException
    {
        return this.repository.listAll();
    }

    public int delete(String eventId) throws SQLException
    {
        return this.repository.deleteById(eventId);
    }
}
