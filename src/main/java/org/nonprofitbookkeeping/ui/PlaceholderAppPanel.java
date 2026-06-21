package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Explicit placeholder panel for first-class routes whose workflows are not implemented yet. */
class PlaceholderAppPanel implements AppPanel
{
    private final String title;
    private final VBox root;

    PlaceholderAppPanel(String title, String message)
    {
        this.title = title;
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alternate-panel-title");
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        this.root = new VBox(8, titleLabel, messageLabel);
        this.root.setPadding(new Insets(12));
        this.root.getStyleClass().add("alternate-content-card");
    }

    public String title()
    {
        return title;
    }

    public Node root()
    {
        return root;
    }
}
