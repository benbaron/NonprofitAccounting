package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.sql.SQLException;

/**
 * Budget editor panel with basic line entry controls.
 */
public class BudgetEditorPanel implements AppPanel
{
	private final BorderPane root = new BorderPane();
	private final BudgetWorkspaceStore store;
	private final TableView<BudgetRow> table = new TableView<>();
	private final Label status = new Label("Ready");

	/**
	 * Creates the budget editor panel.
	 */
	public BudgetEditorPanel()
	{
		this(new BudgetWorkspaceStore());
	}

	BudgetEditorPanel(BudgetWorkspaceStore store)
	{
		this.store = store;
		root.setPadding(new Insets(8));
		Label title = new Label("Budget Editor");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Budget Line");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, save);

		root.setTop(new VBox(6, title, actions, new Separator()));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		TableColumn<BudgetRow, String> account = new TableColumn<>("Account");
		account.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().account()));
		TableColumn<BudgetRow, String> fund = new TableColumn<>("Fund");
		fund.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().fund()));
		TableColumn<BudgetRow, String> period = new TableColumn<>("Period");
		period.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().period()));
		TableColumn<BudgetRow, String> amount = new TableColumn<>("Budget Amount");
		amount.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().amount()));
		table.getColumns().addAll(account, fund, period, amount);
		loadRows();

		root.setCenter(table);
		root.setBottom(new VBox(new Separator(), status));

		add.setOnAction(e -> table.getItems().add(new BudgetRow("", "", "", "0.00")));
		save.setOnAction(e -> onSave());
	}

	@Override
	public String title()
	{
		return "Budget Editor";
	}

	@Override
	public Node root()
	{
		return root;
	}

	@Override
	public void onSave()
	{
		try
		{
			int saved = store.saveEditorRows(table.getItems());
			status.setText("Saved " + saved + " budget row(s)");
		}
		catch (RuntimeException | SQLException ex)
		{
			status.setText("Save failed: " + ex.getMessage());
		}
	}

	private void loadRows()
	{
		try
		{
			table.getItems().setAll(store.loadEditorRows());
			if (table.getItems().isEmpty())
			{
				table.getItems().add(new BudgetRow("", "General", "", "0.00"));
			}
		}
		catch (RuntimeException | SQLException ex)
		{
			table.getItems().setAll(new BudgetRow("", "General", "", "0.00"));
			status.setText("Load failed: " + ex.getMessage());
		}
	}

	public record BudgetRow(String account, String fund, String period, String amount)
	{
	}
}
