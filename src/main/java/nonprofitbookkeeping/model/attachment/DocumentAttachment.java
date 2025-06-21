package nonprofitbookkeeping.model.attachment;

/**
 * Represents a record for a file attached to a transaction.
 */
public class DocumentAttachment {
    private long id;
    private String transactionId;
    private String originalName;
    private String storedName;

    public DocumentAttachment(long id, String transactionId, String originalName, String storedName) {
        this.id = id;
        this.transactionId = transactionId;
        this.originalName = originalName;
        this.storedName = storedName;
    }

    public long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getStoredName() {
        return storedName;
    }
}
