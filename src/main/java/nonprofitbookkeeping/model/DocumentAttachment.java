package nonprofitbookkeeping.model;

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

