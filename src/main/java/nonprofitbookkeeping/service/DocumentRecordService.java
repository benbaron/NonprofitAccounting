package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.DocumentRecord;
import nonprofitbookkeeping.persistence.records.DocumentRecordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for DocumentRecord operations.
 */
public class DocumentRecordService
{
    private final DocumentRecordRepository repository;

    public DocumentRecordService()
    {
        this(new DocumentRecordRepository());
    }

    public DocumentRecordService(DocumentRecordRepository repository)
    {
        this.repository = repository;
    }

    public void save(DocumentRecord record) throws SQLException
    {
        this.repository.upsert(record);
    }

    public List<DocumentRecord> listAll() throws SQLException
    {
        return this.repository.listAll();
    }

    public int delete(String documentId) throws SQLException
    {
        return this.repository.deleteById(documentId);
    }
}
