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
                legacyPanel.onSave();
            }
        };
    }
}
