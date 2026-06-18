package org.nonprofitbookkeeping.ui.admin;

/** Snapshot used by async administrative tasks to report progress safely to UI code. */
public record OperationProgress(AdminOperationStatus status, String phase, String message, int percentComplete)
{
    public OperationProgress
    {
        if (percentComplete < 0 || percentComplete > 100)
        {
            throw new IllegalArgumentException("percentComplete must be between 0 and 100");
        }
        phase = ValidationMessage.normalize(phase);
        message = ValidationMessage.normalize(message);
    }

    public static OperationProgress notStarted()
    {
        return new OperationProgress(AdminOperationStatus.NOT_STARTED, null, null, 0);
    }

    public static OperationProgress running(String phase, String message, int percentComplete)
    {
        return new OperationProgress(AdminOperationStatus.RUNNING, phase, message, percentComplete);
    }

    public static OperationProgress finished(AdminOperationStatus status, String message)
    {
        return new OperationProgress(status, null, message, 100);
    }
}
