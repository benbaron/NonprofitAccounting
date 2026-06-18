package org.nonprofitbookkeeping.ui.admin;

/** High-level lifecycle status for an administrative operation result. */
public enum AdminOperationStatus
{
    NOT_STARTED,
    RUNNING,
    PREVIEW_READY,
    VALIDATION_READY,
    COMMIT_REQUIRED,
    COMMITTED,
    ROLLED_BACK,
    FAILED,
    CANCELLED
}
