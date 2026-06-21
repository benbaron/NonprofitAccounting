package org.nonprofitbookkeeping.ui.admin;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Shared safety/result contract for alternate UI administrative operations.
 *
 * <p>This DTO is intentionally service-oriented rather than JavaFX-specific so
 * database open, H2 repair, import/export, company administration, and SCLX
 * workflows can all expose preview/validation, confirmation, backup, commit,
 * rollback/failure, output-path, count, and async progress details in one shape.
 */
public record AdminOperationResult(
    String operationName,
    AdminOperationMode mode,
    AdminOperationStatus status,
    boolean dryRun,
    List<ValidationMessage> messages,
    ConfirmationRequirement confirmationRequirement,
    BackupResult backupResult,
    OperationCounts counts,
    List<Path> outputPaths,
    String rollbackSummary,
    OperationProgress progress)
{
    public AdminOperationResult
    {
        operationName = ValidationMessage.requireText(operationName, "operationName");
        mode = Objects.requireNonNull(mode, "mode");
        status = Objects.requireNonNull(status, "status");
        messages = messages == null ? List.of() : List.copyOf(messages);
        confirmationRequirement = confirmationRequirement == null ? ConfirmationRequirement.none() : confirmationRequirement;
        backupResult = backupResult == null ? BackupResult.none() : backupResult;
        counts = counts == null ? OperationCounts.zero() : counts;
        outputPaths = outputPaths == null ? List.of() : List.copyOf(outputPaths);
        rollbackSummary = ValidationMessage.normalize(rollbackSummary);
        progress = progress == null ? OperationProgress.notStarted() : progress;
    }

    public static AdminOperationResult preview(String operationName, List<ValidationMessage> messages,
                                               OperationCounts counts, BackupResult backupResult,
                                               ConfirmationRequirement confirmationRequirement)
    {
        return new AdminOperationResult(operationName, AdminOperationMode.PREVIEW, AdminOperationStatus.PREVIEW_READY,
            true, messages, confirmationRequirement, backupResult, counts, List.of(), null,
            OperationProgress.finished(AdminOperationStatus.PREVIEW_READY, "Preview complete."));
    }

    public static AdminOperationResult committed(String operationName, OperationCounts counts, List<Path> outputPaths,
                                                 BackupResult backupResult, List<ValidationMessage> messages)
    {
        return new AdminOperationResult(operationName, AdminOperationMode.COMMIT, AdminOperationStatus.COMMITTED,
            false, messages, ConfirmationRequirement.none(), backupResult, counts, outputPaths, null,
            OperationProgress.finished(AdminOperationStatus.COMMITTED, "Commit complete."));
    }

    public static AdminOperationResult failed(String operationName, AdminOperationMode mode,
                                              List<ValidationMessage> messages, String rollbackSummary)
    {
        return new AdminOperationResult(operationName, mode, AdminOperationStatus.FAILED,
            mode != AdminOperationMode.COMMIT, messages, ConfirmationRequirement.none(), BackupResult.none(),
            OperationCounts.zero(), List.of(), rollbackSummary,
            OperationProgress.finished(AdminOperationStatus.FAILED, "Operation failed."));
    }

    public boolean hasBlockingMessages()
    {
        return this.messages.stream().anyMatch(ValidationMessage::isBlocking);
    }

    public boolean requiresConfirmation()
    {
        return this.confirmationRequirement.required();
    }

    public boolean successfulCommit()
    {
        return this.status == AdminOperationStatus.COMMITTED && !hasBlockingMessages();
    }
}
