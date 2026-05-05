package org.nonprofitbookkeeping.ui.alternate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class AlternateInventoryTransferOrdersView {
    private AlternateInventoryTransferOrdersView() {}
    public static Node build() {
        VBox root = new VBox(12,
            new Label("Transfer Orders"),
            new HBox(8, new TextField("Search or filter results.."), new Button("New Transfer Order")),
            new Label("Date | Status | Source / Destination | Transfer Order"));
        root.setPadding(new Insets(12));
        return root;
    }
}
