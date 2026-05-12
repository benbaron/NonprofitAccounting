package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;

import java.util.function.Supplier;

/**
 * Adapts JavaFX Node-based panels (for example classes in nonprofitbookkeeping.ui.panels)
 * to the {@link AppPanel} contract used by {@link PanelHost}.
 */
public final class FxAppPanelAdapter implements AppPanel
{
    private final String title;
    private final Supplier<? extends Node> nodeFactory;
    private final Runnable onSave;
    private final Runnable onNew;
    private final Runnable onCopy;
    private final Runnable onPaste;

    private Node node;

    public FxAppPanelAdapter(String title, Supplier<? extends Node> nodeFactory)
    {
        this(title, nodeFactory, () -> {}, () -> {}, () -> {}, () -> {});
    }

    public FxAppPanelAdapter(
        String title,
        Supplier<? extends Node> nodeFactory,
        Runnable onSave,
        Runnable onNew,
        Runnable onCopy,
        Runnable onPaste)
    {
        this.title = title;
        this.nodeFactory = nodeFactory;
        this.onSave = onSave;
        this.onNew = onNew;
        this.onCopy = onCopy;
        this.onPaste = onPaste;
    }

    @Override
    public String title()
    {
        return title;
    }

    @Override
    public Node root()
    {
        if (node == null)
        {
            node = nodeFactory.get();
        }
        return node;
    }

    @Override public void onSave() { onSave.run(); }
    @Override public void onNew() { onNew.run(); }
    @Override public void onCopy() { onCopy.run(); }
    @Override public void onPaste() { onPaste.run(); }
}
