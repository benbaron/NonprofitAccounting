package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.OtherAssetItemRecord;
import nonprofitbookkeeping.persistence.records.OtherAssetItemRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for OtherAssetItemRecord operations.
 */
public class OtherAssetItemRecordService
{
    private final OtherAssetItemRecordRepository repository;

    public OtherAssetItemRecordService()
    {
        this(new OtherAssetItemRecordRepository());
    }

    public OtherAssetItemRecordService(OtherAssetItemRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(OtherAssetItemRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<OtherAssetItemRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }
}
