package org.nonprofitbookkeeping.ui.alternate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class AlternateManualJournalView {
    public static final String NO_SERVICE_DATA_MESSAGE =
        "No service-backed manual journal data source is wired for this view yet.";

    private AlternateManualJournalView() {}

    public static Node build() {
        VBox root = new VBox(12,
            new Label("Manual Journals"),
            new HBox(8, new TextField("Search or filter results.."), new Button("New Manual Journal")),
            new Label("Date | Description | Reference | Amount"),
            new Separator(),
            new Label(NO_SERVICE_DATA_MESSAGE));
        root.setPadding(new Insets(12));
        return root;
    }
}
