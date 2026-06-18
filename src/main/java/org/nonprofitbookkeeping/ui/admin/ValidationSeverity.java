package org.nonprofitbookkeeping.ui.admin;

/** Severity levels for validation, preview, and commit messages. */
public enum ValidationSeverity
{
    INFO(false),
    WARNING(false),
    ERROR(true),
    BLOCKING(true);

    private final boolean blocking;

    ValidationSeverity(boolean blocking)
    {
        this.blocking = blocking;
    }

    public boolean isBlocking()
    {
        return blocking;
    }
}
