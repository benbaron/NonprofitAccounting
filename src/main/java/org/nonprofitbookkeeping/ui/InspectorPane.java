package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Represents the InspectorPane component in the nonprofit bookkeeping application.
 */
public class InspectorPane extends VBox
{
    private final Label title = new Label("Inspector");
    private final TextArea body = new TextArea();

    public InspectorPane()
    {
        getStyleClass().add("inspector");
        setPadding(new Insets(8));
        setSpacing(8);

        this.title.getStyleClass().add("inspector-title");
        this.body.setEditable(false);
        this.body.setWrapText(true);

        getChildren().addAll(this.title, new Separator(), this.body);
        clear();
    }

    public void show(String t, String b)
    {
        this.title.setText(t);
        this.body.setText(b == null ? "" : b);
    }

    public void clear()
    {
        show("Inspector", "Right-click an item to see details here.\n\n(Placeholder)");
    }
}
