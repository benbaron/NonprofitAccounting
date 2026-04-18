package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.ReportingPeriodRecord;
import nonprofitbookkeeping.persistence.records.ReportingPeriodRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for ReportingPeriodRecord operations.
 */
public class ReportingPeriodRecordService
{
    private final ReportingPeriodRecordRepository repository;

    public ReportingPeriodRecordService()
    {
        this(new ReportingPeriodRecordRepository());
    }

    public ReportingPeriodRecordService(ReportingPeriodRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(ReportingPeriodRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<ReportingPeriodRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }

    public int delete(ReportingPeriodRecord record) throws SQLException
    {
        return repository.delete(record);
    }
}
