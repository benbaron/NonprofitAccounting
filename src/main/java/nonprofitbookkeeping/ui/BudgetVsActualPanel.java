package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Budget versus actual report panel.
 */
public class BudgetVsActualPanel implements AppPanel
{
	private final BorderPane root = new BorderPane();
	private final BudgetWorkspaceStore store;
	private final TreeTableView<Row> table = new TreeTableView<>();
	private final Label status = new Label("Run report to refresh values");

	/**
	 * Creates the budget versus actual panel.
	 */
	public BudgetVsActualPanel()
	{
		this(new BudgetWorkspaceStore());
	}

	BudgetVsActualPanel(BudgetWorkspaceStore store)
	{
		this.store = store;
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
		this.root.setCenter(this.table);
		this.root.setBottom(new VBox(new Separator(), this.status));

		run.setOnAction(e -> runReport());
		expandAll.setOnAction(e -> setExpandedOnChildren(true));
		collapseAll.setOnAction(e -> setExpandedOnChildren(false));
		runReport();
	}

	private TreeTableColumn<Row, String> col(String name,
		java.util.function.Function<Row, String> getter)
	{
		TreeTableColumn<Row, String> column = new TreeTableColumn<>(name);
		column.setCellValueFactory(v ->
			new ReadOnlyStringWrapper(getter.apply(v.getValue().getValue())));
		return column;
	}

	private void runReport()
	{
		try
		{
			TreeItem<Row> rootItem = new TreeItem<>(new Row("All", "", "", ""));
			for (GroupRow group : this.store.loadBudgetVsActual())
			{
				BigDecimal budgetTotal = BigDecimal.ZERO;
				BigDecimal actualTotal = BigDecimal.ZERO;
				TreeItem<Row> groupItem = new TreeItem<>(new Row(group.groupName(), "", "", ""));
				for (AccountRow account : group.accounts())
				{
					budgetTotal = budgetTotal.add(account.budget());
					actualTotal = actualTotal.add(account.actual());
					groupItem.getChildren().add(new TreeItem<>(new Row(
						account.account(),
						money(account.budget()),
						money(account.actual()),
						money(account.budget().subtract(account.actual())))));
				}
				groupItem.setValue(new Row(
					group.groupName(),
					money(budgetTotal),
					money(actualTotal),
					money(budgetTotal.subtract(actualTotal))));
				rootItem.getChildren().add(groupItem);
			}
			this.table.setRoot(rootItem);
			setExpandedOnChildren(true);
			this.status.setText("Grouped report generated for " + DateRangeContext.get());
		}
		catch (RuntimeException | SQLException ex)
		{
			this.table.setRoot(new TreeItem<>(new Row("All", "", "", "")));
			this.status.setText("Run failed: " + ex.getMessage());
		}
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

	private static String money(BigDecimal amount)
	{
		return amount == null ? "0.00" : amount.toPlainString();
	}

	public record GroupRow(String groupName, java.util.List<AccountRow> accounts)
	{
	}

	public record AccountRow(String account, BigDecimal budget, BigDecimal actual)
	{
	}

	public record Row(String label, String budget, String actual, String variance)
	{
	}
}
