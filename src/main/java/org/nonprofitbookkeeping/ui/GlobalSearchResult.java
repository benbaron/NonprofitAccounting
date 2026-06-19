package org.nonprofitbookkeeping.ui;

/** A mapped global-search hit that the alternate shell can render and route. */
public record GlobalSearchResult(SearchResultType type, String title, String subtitle, AppPanelId targetPanelId,
                                 String targetDescription)
{
    public GlobalSearchResult
    {
        title = title == null || title.isBlank() ? "Untitled result" : title;
        subtitle = subtitle == null ? "" : subtitle;
        targetDescription = targetDescription == null ? "" : targetDescription;
    }

    public boolean hasRoute()
    {
        return targetPanelId != null;
    }

    @Override
    public String toString()
    {
        return type + ": " + title + (subtitle.isBlank() ? "" : " — " + subtitle);
    }
}
