package org.nonprofitbookkeeping.ui;

import java.util.Objects;

/** Metadata for command-center and navigation commands in the alternate shell. */
public record CommandDescriptor(String label,
    String category,
    Runnable action,
    CommandAvailability availability,
    String disabledReason,
    AppPanelId panelId)
{
    public CommandDescriptor
    {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(availability, "availability");
        disabledReason = disabledReason == null ? "" : disabledReason;
    }

    public boolean executable()
    {
        return availability == CommandAvailability.AVAILABLE;
    }
}
