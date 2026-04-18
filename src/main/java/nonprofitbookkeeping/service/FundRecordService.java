package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.FundRecord;
import nonprofitbookkeeping.persistence.records.FundRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for FundRecord operations.
 */
public class FundRecordService
{
    private final FundRecordRepository repository;

    public FundRecordService()
    {
        this(new FundRecordRepository());
    }

    public FundRecordService(FundRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(FundRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<FundRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }
}
