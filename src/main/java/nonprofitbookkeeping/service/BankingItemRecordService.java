package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.BankingItemRecord;
import nonprofitbookkeeping.persistence.records.BankingItemRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for BankingItemRecord operations.
 */
public class BankingItemRecordService
{
    private final BankingItemRecordRepository repository;

    public BankingItemRecordService()
    {
        this(new BankingItemRecordRepository());
    }

    public BankingItemRecordService(BankingItemRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(BankingItemRecord record) throws SQLException
    {
        this.repository.upsert(record);
    }

    public List<BankingItemRecord> listAll() throws SQLException
    {
        return this.repository.listAll();
    }

    public int delete(String bankingItemId) throws SQLException
    {
        return this.repository.deleteById(bankingItemId);
    }
}
