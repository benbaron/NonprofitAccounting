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

        default SaveResult saveContext()
        {
            return SaveResult.noChanges();
        }

        default void onEnter()
        {
            // no-op by default
        }

        default void onLeave()
        {
            // no-op by default
        }
    }

    public static AdaptedPanel from(AppPanel legacyPanel)
    {
        return from(legacyPanel, () -> {}, () -> {});
    }

    public static AdaptedPanel from(AppPanel legacyPanel, Runnable onEnter, Runnable onLeave)
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
            public SaveResult saveContext()
            {
                if (legacyPanel instanceof AppPanel.SaveAware saveAware)
                {
                    return saveAware.save();
                }
                legacyPanel.onSave();
                return SaveResult.saved("Saved " + legacyPanel.title() + ".");
            }

            @Override
            public void onEnter()
            {
                onEnter.run();
            }

            @Override
            public void onLeave()
            {
                onLeave.run();
            }
        };
    }
}
