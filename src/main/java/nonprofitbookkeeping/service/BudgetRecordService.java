package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.BudgetRecord;
import nonprofitbookkeeping.persistence.records.BudgetRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for BudgetRecord operations.
 */
public class BudgetRecordService
{
    private final BudgetRecordRepository repository;

    public BudgetRecordService()
    {
        this(new BudgetRecordRepository());
    }

    public BudgetRecordService(BudgetRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(BudgetRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<BudgetRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }
}
