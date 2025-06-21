package nonprofitbookkeeping.model;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
/**
 * Represents a document that has been attached to a transaction. It stores the
 * associated transaction ID along with both the stored and original file names.
 */
public class DocumentAttachment
{
/** Auto-generated identifier. */
private int id;
/** Transaction identifier this document belongs to. */
private String transactionId;
/** Name of the file stored on disk. */
private String storedFileName;
/** Original filename selected by the user. */
private String originalFileName;

public DocumentAttachment()
{
}

public DocumentAttachment(int id, String transactionId, String storedFileName,
String originalFileName)
{
this.id = id;
this.transactionId = transactionId;
this.storedFileName = storedFileName;
this.originalFileName = originalFileName;
}

public int getId()
{
return this.id;
}

public void setId(int id)
{
this.id = id;
}

public String getTransactionId()
{
return this.transactionId;
}

public void setTransactionId(String transactionId)
{
this.transactionId = transactionId;
}

public String getStoredFileName()
{
return this.storedFileName;
}

public void setStoredFileName(String storedFileName)
{
this.storedFileName = storedFileName;
}

public String getOriginalFileName()
{
return this.originalFileName;
}

public void setOriginalFileName(String originalFileName)
{
this.originalFileName = originalFileName;
}
}

=======
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

/**
 * Simple entity representing a document attachment stored on disk.
 */
@Entity
@Table(name = "document_attachment")
public class DocumentAttachment {
    @Id
    @Column(name = "document_id")
    private String documentId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "upload_time")
    private long uploadTime;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public long getUploadTime() { return uploadTime; }
    public void setUploadTime(long uploadTime) { this.uploadTime = uploadTime; }
}
>>>>>>> a0d4b45 Remove binary document and zip files
