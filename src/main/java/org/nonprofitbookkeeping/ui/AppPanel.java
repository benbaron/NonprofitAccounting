package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;

/**
 * Defines the AppPanel contract in the nonprofit bookkeeping application.
 */
public interface AppPanel
{
    String title();
    Node root();

    default void onSave() {}

    /**
     * Optional truthful save lifecycle. Existing panels may continue overriding {@link #onSave()}.
     */
    interface SaveAware
    {
        SaveResult save();
    }

    default void onNew() {}
    default void onCopy() {}
    default void onPaste() {}
    default void onDelete() {}
    default void onCancel() {}
}
