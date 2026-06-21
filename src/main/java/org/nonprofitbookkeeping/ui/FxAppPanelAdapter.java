package org.nonprofitbookkeeping.ui;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.Node;

/**
 * Adapts JavaFX {@link Node} panel implementations into the {@link AppPanel} lifecycle.
 * Node creation is lazy and occurs on first access to {@link #root()}.
 *
 * @param <T> concrete JavaFX node type
 */
public final class FxAppPanelAdapter<T extends Node> implements AppPanel, AppPanel.SaveAware
{
    private final String title;
    private final Supplier<T> nodeFactory;
    private final Consumer<T> onSave;
    private final Consumer<T> onNew;
    private T node;

    public FxAppPanelAdapter(String title, Supplier<T> nodeFactory)
    {
        this(title, nodeFactory, null, null);
    }

    public FxAppPanelAdapter(String title, Supplier<T> nodeFactory, Consumer<T> onSave, Consumer<T> onNew)
    {
        this.title = Objects.requireNonNull(title, "title");
        this.nodeFactory = Objects.requireNonNull(nodeFactory, "nodeFactory");
        this.onSave = onSave;
        this.onNew = onNew;
    }

    @Override
    public String title()
    {
        return this.title;
    }

    @Override
    public Node root()
    {
        if (this.node == null)
        {
            this.node = Objects.requireNonNull(this.nodeFactory.get(), "nodeFactory returned null");
        }
        return this.node;
    }

    @Override
    public void onSave()
    {
        save();
    }

    @Override
    public SaveResult save()
    {
        if (this.onSave == null)
        {
            return SaveResult.unsupported("Save is not supported for " + this.title + ".");
        }
        this.onSave.accept(node());
        return SaveResult.saved("Saved " + this.title + ".");
    }

    @Override
    public void onNew()
    {
        if (this.onNew != null)
        {
            this.onNew.accept(node());
        }
    }

    private T node()
    {
        root();
        return this.node;
    }
}
