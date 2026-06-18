package org.nonprofitbookkeeping.ui;

import java.util.Objects;

/**
 * Truthful outcome for an AppPanel save request.
 */
public final class SaveResult
{
    public enum Status
    {
        SAVED,
        NO_CHANGES,
        UNSUPPORTED,
        FAILED
    }

    private static final SaveResult SAVED = new SaveResult(Status.SAVED, "Saved.", null);
    private static final SaveResult NO_CHANGES = new SaveResult(Status.NO_CHANGES, "No changes to save.", null);
    private static final SaveResult UNSUPPORTED = new SaveResult(Status.UNSUPPORTED, "Save is not supported for this panel.", null);

    private final Status status;
    private final String message;
    private final Throwable cause;

    private SaveResult(Status status, String message, Throwable cause)
    {
        this.status = Objects.requireNonNull(status, "status");
        this.message = message == null || message.isBlank() ? defaultMessage(status) : message;
        this.cause = cause;
    }

    public static SaveResult saved()
    {
        return SAVED;
    }

    public static SaveResult saved(String message)
    {
        return new SaveResult(Status.SAVED, message, null);
    }

    public static SaveResult noChanges()
    {
        return NO_CHANGES;
    }

    public static SaveResult noChanges(String message)
    {
        return new SaveResult(Status.NO_CHANGES, message, null);
    }

    public static SaveResult unsupported()
    {
        return UNSUPPORTED;
    }

    public static SaveResult unsupported(String message)
    {
        return new SaveResult(Status.UNSUPPORTED, message, null);
    }

    public static SaveResult failed(String message, Throwable cause)
    {
        return new SaveResult(Status.FAILED, message, cause);
    }

    public Status status()
    {
        return status;
    }

    public String message()
    {
        return message;
    }

    public Throwable cause()
    {
        return cause;
    }

    public boolean savedChanges()
    {
        return status == Status.SAVED;
    }

    public boolean failed()
    {
        return status == Status.FAILED;
    }

    private static String defaultMessage(Status status)
    {
        return switch (status)
        {
            case SAVED -> "Saved.";
            case NO_CHANGES -> "No changes to save.";
            case UNSUPPORTED -> "Save is not supported for this panel.";
            case FAILED -> "Save failed.";
        };
    }
}
