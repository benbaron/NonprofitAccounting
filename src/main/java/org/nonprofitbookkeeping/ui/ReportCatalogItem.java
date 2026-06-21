package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.reports.ReportKind;

import java.util.List;

/** A report entry available in the native alternate reports catalog. */
public record ReportCatalogItem(String id, String displayName, ReportKind kind, String templateId,
    boolean dateRangeRequired, boolean fundSupported, boolean accountSupported, boolean donorSupported,
    List<String> optionNames)
{
    public ReportCatalogItem
    {
        optionNames = optionNames == null ? List.of() : List.copyOf(optionNames);
    }
}
