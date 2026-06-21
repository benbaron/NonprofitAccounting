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
        return warnings.size();
    }

    public int errorCount()
    {
        return errors.size();
    }

    public boolean hasBlockingErrors()
    {
        return !errors.isEmpty();
    }

    public String describeCounts()
    {
        return "Created: " + created + "\nUpdated: " + updated + "\nSkipped: " + skipped
            + "\nWarnings: " + warningCount() + "\nErrors: " + errorCount();
    }
}
