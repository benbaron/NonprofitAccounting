package org.nonprofitbookkeeping.ui.admin;

/** Common count summary for preview and commit result screens. */
public record OperationCounts(int created, int updated, int skipped, int warnings, int errors)
{
    public OperationCounts
    {
        if (created < 0 || updated < 0 || skipped < 0 || warnings < 0 || errors < 0)
        {
            throw new IllegalArgumentException("operation counts cannot be negative");
        }
    }

    public static OperationCounts zero()
    {
        return new OperationCounts(0, 0, 0, 0, 0);
    }

    public int changed()
    {
        return created + updated;
    }
}
