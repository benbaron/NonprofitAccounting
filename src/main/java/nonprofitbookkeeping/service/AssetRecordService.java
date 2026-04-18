package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.persistence.records.AssetRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for AssetRecord operations.
 */
public class AssetRecordService
{
    private final AssetRecordRepository repository;

    public AssetRecordService()
    {
        this(new AssetRecordRepository());
    }

    public AssetRecordService(AssetRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(AssetRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<AssetRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }

    public int delete(String assetId) throws SQLException
    {
        return repository.deleteById(assetId);
    }
}
