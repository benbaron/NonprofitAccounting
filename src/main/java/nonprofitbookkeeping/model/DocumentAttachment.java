package nonprofitbookkeeping.model;

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
