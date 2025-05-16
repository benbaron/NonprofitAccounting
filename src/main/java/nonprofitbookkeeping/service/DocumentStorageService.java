
package nonprofitbookkeeping.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * DocumentStorageService manages attachment and retrieval of document files
 * associated with transactions.
 * <p>
 * Documents are stored in a designated directory on disk (in the user's home
 * directory, under "NonprofitDocuments"). The attach method copies files into that
 * directory and assigns a unique filename based on transaction ID and timestamp.
 * </p>
 */
public class DocumentStorageService
{
	
	// Base directory for storing documents.
	private static final File DOCUMENT_BASE_DIR =
		new File(System.getProperty("user.home"), "NonprofitDocuments");
	
	// Static initializer to ensure the storage directory exists.
	static
	{
		
		if (!DOCUMENT_BASE_DIR.exists())
		{
			DOCUMENT_BASE_DIR.mkdirs();
		}
		
	}
	
	/**
	 * Attaches a document (file) to a transaction by copying the file
	 * into the storage directory. The file is renamed to include the transaction ID
	 * and a timestamp. The new file name serves as the unique document ID.
	 *
	 * @param transactionId The ID of the transaction to attach the document to.
	 * @param file The source file to attach.
	 * @throws IOException if an error occurs during file copying.
	 */
	public void attachDocumentToTransaction(String transactionId, File file) throws IOException
	{
		
		if (file == null || !file.exists())
		{
			throw new IllegalArgumentException("File does not exist: " + file);
		}
		
		// Get the original file's extension (if any).
		String originalName = file.getName();
		String extension = "";
		int idx = originalName.lastIndexOf('.');
		
		if (idx > 0)
		{
			extension = originalName.substring(idx);
		}
		
		// Generate a new filename using the transactionId and the current timestamp.
		String newFileName = transactionId + "_" + System.currentTimeMillis() + extension;
		File targetFile = new File(DOCUMENT_BASE_DIR, newFileName);
		
		// Copy the source file to the target location, replacing any existing file.
		Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		// Optionally, log the operation.
		System.out.println("Document attached for transaction " + transactionId + ": " +
			targetFile.getAbsolutePath());
	}
	
	/**
	 * Retrieves the document associated with the given document ID.
	 * Here, the document ID corresponds to the file name saved in the storage directory.
	 *
	 * @param documentId The document's unique identifier (i.e., the file name).
	 * @return A File object referring to the stored document.
	 * @throws IOException if the document is not found or is inaccessible.
	 */
	public File retrieveDocument(String documentId) throws IOException
	{
		
		if (documentId == null || documentId.trim().isEmpty())
		{
			throw new IllegalArgumentException("Document ID must not be empty.");
		}
		
		File targetFile = new File(DOCUMENT_BASE_DIR, documentId);
		
		if (!targetFile.exists())
		{
			throw new IOException("Document not found: " + documentId);
		}
		
		return targetFile;
	}
	
}
