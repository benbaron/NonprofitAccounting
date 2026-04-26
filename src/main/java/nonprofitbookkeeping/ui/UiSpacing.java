package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;

/**
 * Shared spacing scale for JavaFX layouts.
 */
public final class UiSpacing
{
    public static final double PAGE_PADDING = 12;
    public static final double SECTION_SPACING = 8;
    public static final double GRID_H_GAP = 12;
    public static final double GRID_V_GAP = 8;
    public static final double ACTION_BAR_TOP_MARGIN = 8;

    private UiSpacing()
    {
    }

    public static Insets pageInsets()
    {
        return new Insets(PAGE_PADDING);
    }

    public static Insets actionBarTopMargin()
    {
        return new Insets(ACTION_BAR_TOP_MARGIN, 0, 0, 0);
    }
}
