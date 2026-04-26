package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.BankStatementRecord;
import nonprofitbookkeeping.persistence.records.BankStatementRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for BankStatementRecord operations.
 */
public class BankStatementRecordService
{
    private final BankStatementRecordRepository repository;

    public BankStatementRecordService()
    {
        this(new BankStatementRecordRepository());
    }

    public BankStatementRecordService(BankStatementRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(BankStatementRecord record) throws SQLException
    {
        this.repository.upsert(record);
    }

    public List<BankStatementRecord> listAll() throws SQLException
    {
        return this.repository.listAll();
    }

    public int delete(String importId) throws SQLException
    {
        return this.repository.deleteByImportId(importId);
    }
}
