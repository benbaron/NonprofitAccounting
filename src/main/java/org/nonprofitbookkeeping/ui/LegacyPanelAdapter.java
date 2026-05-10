package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;

/**
 * Adapter layer for hosting legacy {@code org.nonprofitbookkeeping.ui} panels inside
 * the alternate {@code org.nonprofitbookkeeping.ui} shell.
 */
public final class LegacyPanelAdapter
{
    private LegacyPanelAdapter() {}

    public interface AdaptedPanel
    {
        String title();

        Node content();

        default void onEnter()
        {
            // no-op by default
        }

        default void onLeave()
        {
            // no-op by default
        }

        default void saveContext()
        {
            // no-op by default
        }
    }

    public static AdaptedPanel from(AppPanel legacyPanel)
    {
        return new AdaptedPanel()
        {
            @Override
            public String title()
            {
                return legacyPanel.title();
            }

            @Override
            public Node content()
            {
                return legacyPanel.root();
            }

            @Override
            public void saveContext()
            {
                // Intentionally no-op: alternate-shell context save is in-memory only.
                // Persisting partial edits via onSave() while switching panels can commit
                // incomplete/garbage data to the backing store.
            }
        };
    }
}
