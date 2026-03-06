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
    private final BorderPane root = new BorderPane();
    private final TreeTableView<Row> table = new TreeTableView<>();
    private final Label status = new Label("Run report to refresh values");

    public BudgetVsActualPanel()
    {
        root.setPadding(new Insets(8));
        Label title = new Label("Budget vs Actual");
        title.getStyleClass().add("panel-title");

        Button run = new Button("Run");
        Button expandAll = new Button("Expand All");
        Button collapseAll = new Button("Collapse All");
        HBox actions = new HBox(8, run, expandAll, collapseAll);

        root.setTop(new VBox(6, title, actions, new Separator()));

        table.getColumns().add(col("Group / Account", Row::label));
        table.getColumns().add(col("Budget", Row::budget));
        table.getColumns().add(col("Actual", Row::actual));
        table.getColumns().add(col("Variance", Row::variance));
        table.setShowRoot(false);
        root.setCenter(table);
        root.setBottom(new VBox(new Separator(), status));

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

        TreeItem<Row> admin = new TreeItem<>(new Row("Administration", "5700.00", "5850.00", "-150.00"));
        admin.getChildren().add(new TreeItem<>(new Row("Office Rent", "4800.00", "4800.00", "0.00")));
        admin.getChildren().add(new TreeItem<>(new Row("Utilities", "900.00", "1050.00", "-150.00")));

        TreeItem<Row> programs = new TreeItem<>(new Row("Programs", "6200.00", "5900.00", "300.00"));
        programs.getChildren().add(new TreeItem<>(new Row("Program Supplies", "3500.00", "3200.00", "300.00")));
        programs.getChildren().add(new TreeItem<>(new Row("Volunteer Meals", "2700.00", "2700.00", "0.00")));

        rootItem.getChildren().setAll(admin, programs);
        table.setRoot(rootItem);
        setExpandedOnChildren(true);
        status.setText("Grouped report generated for " + DateRangeContext.get());
    }

    private void setExpandedOnChildren(boolean expanded)
    {
        if (table.getRoot() == null)
        {
            return;
        }
        for (TreeItem<Row> item : table.getRoot().getChildren())
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
        return root;
    }

    public record Row(String label, String budget, String actual, String variance)
    {
    }
}
