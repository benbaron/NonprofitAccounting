package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;

/**
 * Shared spacing scale for legacy/imported UI package.
 */
public final class UiSpacing
{
    public static final double PAGE_PADDING = 12;
    public static final double SECTION_SPACING = 8;

    private UiSpacing()
    {
    }

    public static Insets pageInsets()
    {
        return new Insets(PAGE_PADDING);
    }
}
