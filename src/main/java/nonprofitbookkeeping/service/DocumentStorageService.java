
package nonprofitbookkeeping.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
<<<<<<< HEAD
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import java.sql.SQLException;
=======
=======
>>>>>>> b1f07f2 Extend SQL support
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
=======
>>>>>>> 6159d55 Revert service changes
=======
import java.sql.SQLException;

import nonprofitbookkeeping.model.attachment.DocumentAttachment;

>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git

import nonprofitbookkeeping.model.DocumentAttachment;
import nonprofitbookkeeping.service.DatabaseManager;
>>>>>>> 627421c Add attachment persistence


import nonprofitbookkeeping.model.attachment.DocumentAttachment;

import nonprofitbookkeeping.model.attachment.DocumentAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nonprofitbookkeeping.ui.helpers.AlertBox;
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
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        private static final Logger LOGGER = LoggerFactory.getLogger(DocumentStorageService.class);
	
	/** Base directory located in the user's home directory under "NonprofitDocuments" for storing all attached documents. */
    private static final File DOCUMENT_BASE_DIR =
            new File(System.getProperty("user.home"), "NonprofitDocuments");

    /** Manager handling persistence of {@link DocumentAttachment} metadata. */
    private final DatabaseManager databaseManager;
<<<<<<< HEAD
=======

        /** Base directory located in the user's home directory under "NonprofitDocuments" for storing all attached documents. */
        private static final File DOCUMENT_BASE_DIR =
                new File(System.getProperty("user.home"), "NonprofitDocuments");

        /** Database manager used for persisting attachment metadata. */
        private final DatabaseManager dbManager = new DatabaseManager();
>>>>>>> 627421c Add attachment persistence
=======
>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
	
	/**
	 * Static initializer block to ensure that the base directory for document storage
	 * ({@link #DOCUMENT_BASE_DIR}) exists when the class is loaded.
	 * If the directory does not exist, it will be created.
	 */
        static
        {

                if (!DOCUMENT_BASE_DIR.exists())
                {
                        DOCUMENT_BASE_DIR.mkdirs();
                }

        }

    /**
     * Creates a DocumentStorageService using a database file located inside the
     * document base directory.
     */
    public DocumentStorageService() {
        try {
            File dbFile = new File(DOCUMENT_BASE_DIR, "attachments.db");
            this.databaseManager = new DatabaseManager(dbFile);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DatabaseManager", e);
        }
    }

    /**
     * Creates a DocumentStorageService with the given {@link DatabaseManager}.
     *
     * @param manager database manager to use
     */
    public DocumentStorageService(DatabaseManager manager) {
        this.databaseManager = manager;
    }
	
	/**
	 * Attaches a specified document file to a transaction.
	 * The method copies the source {@code file} into a predefined storage directory
	 * ({@link #DOCUMENT_BASE_DIR}). The copied file is renamed using the {@code transactionId},
	 * a timestamp to ensure uniqueness, and the original file's extension.
	 * This new filename effectively becomes the document's unique identifier within the storage.
	 * If a file with the generated name already exists, it will be replaced.
	 *
	 * @param transactionId The ID of the transaction to which the document should be attached.
	 *                      This is used in generating the stored filename. Must not be null or empty.
	 * @param file The source {@link File} to be attached. Must not be null and must exist.
<<<<<<< HEAD
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
         * @return the generated database ID for the attachment record
         * @throws IOException if an error occurs during file copying (e.g., permission issues, disk full).
         * @throws IllegalArgumentException if {@code file} is null or does not exist, or if {@code transactionId} is null or empty.
         */
        public long attachDocumentToTransaction(String transactionId, File file) throws IOException
=======
	 * @throws IOException if an error occurs during file copying (e.g., permission issues, disk full).
	 * @throws IllegalArgumentException if {@code file} is null or does not exist, or if {@code transactionId} is null or empty.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        public void attachDocumentToTransaction(String transactionId, File file) throws IOException
>>>>>>> b1f07f2 Extend SQL support
        {
=======
	public void attachDocumentToTransaction(String transactionId, File file) throws IOException
	{
>>>>>>> 6159d55 Revert service changes
=======
         * @return the generated database ID for the attachment record
         * @throws IOException if an error occurs during file copying (e.g., permission issues, disk full).
         * @throws IllegalArgumentException if {@code file} is null or does not exist, or if {@code transactionId} is null or empty.
         */
        public long attachDocumentToTransaction(String transactionId, File file) throws IOException
        {
>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
		if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID must not be null or empty.");
        }
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
		
<<<<<<< HEAD
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
                try
                {
                        // Copy the source file to the target location, replacing any existing file.
                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("Document attached for transaction {}: {}", transactionId,
                                        targetFile.getAbsolutePath());
                }
                catch (IOException e)
                {
                        LOGGER.error("Failed to attach document for transaction {}", transactionId, e);
                        AlertBox.showError(null,
                                        "Failed to attach document: " + e.getMessage());
                        throw e;
                }
=======
                // Copy the source file to the target location, replacing any existing file.
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Persist attachment record
                try (Connection c = DatabaseManager.getConnection();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO document_attachment (transaction_id, stored_file, original_file) VALUES (?, ?, ?)");) {
                        ps.setString(1, transactionId);
                        ps.setString(2, newFileName);
                        ps.setString(3, originalName);
                        ps.executeUpdate();
                } catch (SQLException ex) {
                        throw new IOException("Failed to record attachment", ex);
                }

                // Optionally, log the operation.
                System.out.println("Document attached for transaction " + transactionId + ": " +
                        targetFile.getAbsolutePath());
>>>>>>> 627421c Add attachment persistence
	}
=======
                // Copy the source file to the target location, replacing any existing file.
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String docId = newFileName;
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "MERGE INTO document_attachment(document_id,transaction_id,file_path,original_name,upload_time) " +
                                     "KEY(document_id) VALUES(?,?,?,?,?)")) {
                    ps.setString(1, docId);
                    ps.setString(2, transactionId);
                    ps.setString(3, targetFile.getAbsolutePath());
                    ps.setString(4, originalName);
                    ps.setLong(5, System.currentTimeMillis());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException("Error recording document metadata", e);
                }

                System.out.println("Document attached for transaction " + transactionId + ": " +
                        targetFile.getAbsolutePath());
        }
>>>>>>> b1f07f2 Extend SQL support
=======
=======

>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
		// Copy the source file to the target location, replacing any existing file.
<<<<<<< HEAD
		Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		// Optionally, log the operation.
		System.out.println("Document attached for transaction " + transactionId + ": " +
			targetFile.getAbsolutePath());
	}
>>>>>>> 6159d55 Revert service changes
=======
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                long id = -1L;
                try {
                        id = this.databaseManager.insertAttachment(transactionId, originalName, newFileName);
                } catch (SQLException e) {
                        throw new IOException("Failed to record attachment", e);

                }

                // Optionally, log the operation.
                System.out.println("Document attached for transaction " + transactionId + ": " +
                        targetFile.getAbsolutePath());

                return id;
        }

>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
	
	/**
	 * Retrieves a document from the storage directory based on its document ID.
	 * The {@code documentId} is expected to be the filename under which the document
	 * was stored (typically generated by {@link #attachDocumentToTransaction(String, File)}).
	 *
	 * @param documentId The unique identifier (filename) of the document to retrieve. Must not be null or empty.
	 * @return A {@link File} object pointing to the stored document.
	 * @throws IOException if the document identified by {@code documentId} is not found in the storage directory,
	 *                     or if there's an issue accessing it.
	 * @throws IllegalArgumentException if {@code documentId} is null or empty.
	 */
<<<<<<< HEAD
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        public File retrieveDocument(String documentId) throws IOException
        {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
		
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

        /**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
         * Looks up a {@link DocumentAttachment} by ID and returns the attached file.
         *
         * @param attachmentId database ID of the attachment
         * @return the file associated with the attachment
         * @throws IOException if the file cannot be located
         * @throws SQLException if the database lookup fails
         */
        public File retrieveDocument(long attachmentId) throws IOException, SQLException
        {
                DocumentAttachment attachment = this.databaseManager.getAttachment(attachmentId);
                if (attachment == null)
                {
                        throw new IOException("Attachment not found: " + attachmentId);
                }

                return retrieveDocument(attachment.getStoredName());
=======
         * Retrieve an attachment using its database identifier.
         *
         * @param attachmentId the primary key of the attachment record
         * @return the stored document file
         * @throws IOException if the record does not exist or the file is missing
         */
        public File retrieveAttachment(int attachmentId) throws IOException
        {
                try (Connection c = DatabaseManager.getConnection();
                        PreparedStatement ps = c.prepareStatement(
                                "SELECT stored_file FROM document_attachment WHERE id=?");) {
                        ps.setInt(1, attachmentId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        String stored = rs.getString(1);
                                        return retrieveDocument(stored);
                                }
                        }
                } catch (SQLException ex) {
                        throw new IOException("Failed to read attachment", ex);
                }

                throw new IOException("Attachment not found: " + attachmentId);
>>>>>>> 627421c Add attachment persistence
=======

                if (documentId == null || documentId.trim().isEmpty())
                {
                        throw new IllegalArgumentException("Document ID must not be empty.");
                }

                String path = null;
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "SELECT file_path FROM document_attachment WHERE document_id=?")) {
                    ps.setString(1, documentId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                } catch (SQLException e) {
                        throw new RuntimeException("Error retrieving document metadata", e);
                }

                if (path == null) {
                        throw new IOException("Document metadata not found for ID: " + documentId);
                }

                File targetFile = new File(path);
                if (!targetFile.exists()) {
                        throw new IOException("Document file missing: " + path);
                }

                return targetFile;
>>>>>>> b1f07f2 Extend SQL support
        }
=======
	public File retrieveDocument(String documentId) throws IOException
	{
=======
        public File retrieveDocument(String documentId) throws IOException
        {
>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
		
		if (documentId == null || documentId.trim().isEmpty())
		{
			throw new IllegalArgumentException("Document ID must not be empty.");
		}
		
		File targetFile = new File(DOCUMENT_BASE_DIR, documentId);
		
		if (!targetFile.exists())
		{
			throw new IOException("Document not found: " + documentId);
		}
		
<<<<<<< HEAD
		return targetFile;
	}
>>>>>>> 6159d55 Revert service changes
=======
                return targetFile;
        }

        /**
         * Looks up a {@link DocumentAttachment} by ID and returns the attached file.
         *
         * @param attachmentId database ID of the attachment
         * @return the file associated with the attachment
         * @throws IOException if the file cannot be located
         * @throws SQLException if the database lookup fails
         */
        public File retrieveDocument(long attachmentId) throws IOException, SQLException
        {
                DocumentAttachment attachment = this.databaseManager.getAttachment(attachmentId);
                if (attachment == null)
                {
                        throw new IOException("Attachment not found: " + attachmentId);
                }

                return retrieveDocument(attachment.getStoredName());

        }
>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
	
}
