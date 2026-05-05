package org.nonprofitbookkeeping.ui.alternate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class AlternateChartOfAccountsView {
    private AlternateChartOfAccountsView() {}
    public static Node build() {
        VBox root = new VBox(12,
            new Label("Chart of Accounts"),
            new HBox(8, new TextField("Search or filter results.."), new Button("New Chart of Account")),
            new Label("Assets"), new Separator(), new TableView<>());
        root.setPadding(new Insets(12));
        return root;
    }
}
