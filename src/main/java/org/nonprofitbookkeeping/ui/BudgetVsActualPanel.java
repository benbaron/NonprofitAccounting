package org.nonprofitbookkeeping.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Represents the BudgetVsActualPanel component in the nonprofit bookkeeping application.
 */
public class BudgetVsActualPanel implements AppPanel
{
    static final String NO_SERVICE_DATA_MESSAGE = "No service-backed data source is wired for this panel yet.";

    private final BorderPane root = new BorderPane();
    private final TreeTableView<Row> table = new TreeTableView<>();
    private final Label status = new Label(NO_SERVICE_DATA_MESSAGE);

    public BudgetVsActualPanel()
    {
        this.root.setPadding(new Insets(8));
        Label title = new Label("Budget vs Actual");
        title.getStyleClass().add("panel-title");

        Button run = new Button("Run");
        Button expandAll = new Button("Expand All");
        Button collapseAll = new Button("Collapse All");
        HBox actions = new HBox(8, run, expandAll, collapseAll);

        this.root.setTop(new VBox(6, title, actions, new Separator()));

        this.table.getColumns().add(col("Group / Account", Row::label));
        this.table.getColumns().add(col("Budget", Row::budget));
        this.table.getColumns().add(col("Actual", Row::actual));
        this.table.getColumns().add(col("Variance", Row::variance));
        this.table.setShowRoot(false);
        this.table.setPlaceholder(new Label(NO_SERVICE_DATA_MESSAGE));
        this.root.setCenter(this.table);
        this.root.setBottom(new VBox(new Separator(), this.status));

        run.setOnAction(e -> runReport());
        expandAll.setOnAction(e -> setExpandedOnChildren(true));
        collapseAll.setOnAction(e -> setExpandedOnChildren(false));

        runReport();
    }

    private TreeTableColumn<Row, String> col(String name, java.util.function.Function<Row, String> getter)
    {
        TreeTableColumn<Row, String> c = new TreeTableColumn<>(name);
        c.setCellValueFactory(v -> new ReadOnlyStringWrapper(getter.apply(v.getValue().getValue())));
        return c;
    }

    private void runReport()
    {
        TreeItem<Row> rootItem = new TreeItem<>(new Row("All", "", "", ""));
        this.table.setRoot(rootItem);
        this.status.setText(NO_SERVICE_DATA_MESSAGE + " Date range: " + DateRangeContext.get() + ".");
    }

    private void setExpandedOnChildren(boolean expanded)
    {
        if (this.table.getRoot() == null)
        {
            return;
        }
        for (TreeItem<Row> item : this.table.getRoot().getChildren())
        {
            item.setExpanded(expanded);
        }
    }

    @Override
    public String title()
    {
        return "Budget vs Actual";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    public record Row(String label, String budget, String actual, String variance)
    {
    }
}
