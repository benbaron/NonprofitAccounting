package org.nonprofitbookkeeping.ui.alternate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class AlternateReportsOverviewView {
    private AlternateReportsOverviewView() {}
    public static Node build() {
        VBox root = new VBox(12,
            new Label("Reports"),
            new HBox(8, new Button("General Ledger"), new Button("Trial Balance"), new Button("Balance Sheet")),
            new Label("Basis = Accrual | Filter bar placeholder"),
            new Label("Accounts Receivable ..."));
        root.setPadding(new Insets(12));
        return root;
    }
}
