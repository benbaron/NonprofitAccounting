package org.nonprofitbookkeeping.ui.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminOperationResultTest
{
    @Test
    void previewCapturesDryRunValidationBackupAndConfirmationContract()
    {
        AdminOperationResult result = AdminOperationResult.preview(
            "SCLX import",
            List.of(
                ValidationMessage.info("SCLX_VERSION", "SCLX version 1.3 detected."),
                ValidationMessage.warning("DUPLICATE_ACCOUNT", "Duplicate account will be skipped.")),
            new OperationCounts(3, 0, 1, 1, 0),
            BackupResult.recommended("Back up the active company before import."),
            ConfirmationRequirement.required("Import commit changes the active company.", "IMPORT"));

        assertEquals(AdminOperationMode.PREVIEW, result.mode());
        assertEquals(AdminOperationStatus.PREVIEW_READY, result.status());
        assertTrue(result.dryRun());
        assertTrue(result.requiresConfirmation());
        assertTrue(result.backupResult().recommended());
        assertFalse(result.backupResult().completed());
        assertFalse(result.hasBlockingMessages());
        assertEquals(3, result.counts().changed());
        assertEquals(100, result.progress().percentComplete());
    }

    @Test
    void committedCapturesCountsOutputPathsAndCompletedBackup()
    {
        Path exportPath = Path.of("company.sclx.json");
        Path backupPath = Path.of("company.mv.db.bak");

        AdminOperationResult result = AdminOperationResult.committed(
            "Database export",
            new OperationCounts(0, 2, 0, 0, 0),
            List.of(exportPath),
            BackupResult.completed(List.of(backupPath), "Backup created."),
            List.of(ValidationMessage.info("EXPORT_DONE", "Export complete.")));

        assertEquals(AdminOperationStatus.COMMITTED, result.status());
        assertFalse(result.dryRun());
        assertTrue(result.successfulCommit());
        assertEquals(List.of(exportPath), result.outputPaths());
        assertEquals(List.of(backupPath), result.backupResult().backupPaths());
        assertEquals(2, result.counts().changed());
    }

    @Test
    void blockingMessagesPreventSuccessfulCommitAndModelFailureRollbackSummary()
    {
        AdminOperationResult result = AdminOperationResult.failed(
            "H2 repair",
            AdminOperationMode.COMMIT,
            List.of(ValidationMessage.blocking("RECOVER_FAILED", "Recovery script could not be created.")),
            "Original database files were left in place; no replacement was committed.");

        assertEquals(AdminOperationStatus.FAILED, result.status());
        assertFalse(result.dryRun());
        assertTrue(result.hasBlockingMessages());
        assertFalse(result.successfulCommit());
        assertEquals("Original database files were left in place; no replacement was committed.", result.rollbackSummary());
    }

    @Test
    void listsAreDefensivelyCopied()
    {
        List<ValidationMessage> messages = new ArrayList<>();
        messages.add(ValidationMessage.info("A", "First message."));

        AdminOperationResult result = AdminOperationResult.preview(
            "Company delete",
            messages,
            OperationCounts.zero(),
            BackupResult.none(),
            ConfirmationRequirement.none());

        messages.add(ValidationMessage.error("B", "Second message."));

        assertEquals(1, result.messages().size());
        assertThrows(UnsupportedOperationException.class,
            () -> result.messages().add(ValidationMessage.info("C", "Third message.")));
    }

    @Test
    void invalidContractValuesAreRejected()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new OperationCounts(0, -1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
            () -> OperationProgress.running("Read", "Reading file.", 101));
        assertThrows(IllegalArgumentException.class,
            () -> ConfirmationRequirement.required(" ", "DELETE"));
        assertThrows(IllegalArgumentException.class,
            () -> ValidationMessage.info("EMPTY", " "));
    }
}
