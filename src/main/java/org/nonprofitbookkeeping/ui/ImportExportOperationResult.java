package org.nonprofitbookkeeping.ui;

import java.util.List;

/** Standard result summary for alternate UI import/export operations. */
public record ImportExportOperationResult(
    int created,
    int updated,
    int skipped,
    List<String> warnings,
    List<String> errors)
{
    public ImportExportOperationResult
    {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public int warningCount()
    {
        return this.warnings.size();
    }

    public int errorCount()
    {
        return this.errors.size();
    }

    public boolean hasBlockingErrors()
    {
        return !this.errors.isEmpty();
    }

    public String describeCounts()
    {
        return "Created: " + this.created + "\nUpdated: " + this.updated + "\nSkipped: " + this.skipped
            + "\nWarnings: " + warningCount() + "\nErrors: " + errorCount();
    }
}
