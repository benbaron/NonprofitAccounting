package nonprofitbookkeeping.ui.panels.skeletons;

import nonprofitbookkeeping.ui.panels.ClassicDashboardNavigation;
import nonprofitbookkeeping.ui.panels.DashboardNavigation;
import nonprofitbookkeeping.ui.panels.SharedDashboardPanelFX;

/**
 * Compatibility name for the classic shell's dashboard.
 *
 * <p>Both UI systems now use {@link SharedDashboardPanelFX}; this subclass
 * preserves the existing classic-shell type and constructor call sites.</p>
 */
public class SkeletonDashboardPanel extends SharedDashboardPanelFX
{
    public SkeletonDashboardPanel()
    {
        super(new ClassicDashboardNavigation());
    }

    public SkeletonDashboardPanel(DashboardNavigation navigation)
    {
        super(navigation);
    }
}
