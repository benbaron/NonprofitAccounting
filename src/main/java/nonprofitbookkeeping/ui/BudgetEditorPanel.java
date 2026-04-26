package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
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
		this.root.setPadding(new Insets(8));
		Label title = new Label("Budget Editor");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Budget Line");
		Button delete = new Button("Delete Selected");
		Button refresh = new Button("Refresh");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, delete, refresh, save);

		this.root.setTop(new VBox(6, title, actions, new Separator()));
		this.table.setEditable(true);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		TableColumn<BudgetRow, String> account = new TableColumn<>("Account");
		account.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().account()));
		account.setCellFactory(TextFieldTableCell.forTableColumn());
		account.setOnEditCommit(event ->
			this.table.getItems().set(event.getTablePosition().getRow(),
				event.getRowValue().withAccount(event.getNewValue())));
		TableColumn<BudgetRow, String> fund = new TableColumn<>("Fund");
		fund.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().fund()));
		fund.setCellFactory(TextFieldTableCell.forTableColumn());
		fund.setOnEditCommit(event ->
			this.table.getItems().set(event.getTablePosition().getRow(),
				event.getRowValue().withFund(event.getNewValue())));
		TableColumn<BudgetRow, String> period = new TableColumn<>("Period");
		period.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().period()));
		period.setCellFactory(TextFieldTableCell.forTableColumn());
		period.setOnEditCommit(event ->
			this.table.getItems().set(event.getTablePosition().getRow(),
				event.getRowValue().withPeriod(event.getNewValue())));
		TableColumn<BudgetRow, String> amount = new TableColumn<>("Budget Amount");
		amount.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().amount()));
		amount.setCellFactory(TextFieldTableCell.forTableColumn());
		amount.setOnEditCommit(event ->
			this.table.getItems().set(event.getTablePosition().getRow(),
				event.getRowValue().withAmount(event.getNewValue())));
		this.table.getColumns().addAll(account, fund, period, amount);
		loadRows();

		this.root.setCenter(this.table);
		this.root.setBottom(new VBox(new Separator(), this.status));

		add.setOnAction(e -> this.table.getItems().add(new BudgetRow("", "", "", "0.00")));
		delete.setOnAction(e -> onDeleteSelected());
		refresh.setOnAction(e -> loadRows());
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
		return this.root;
	}

	@Override
	public void onSave()
	{
		try
		{
			int saved = this.store.saveEditorRows(this.table.getItems());
			this.status.setText("Saved " + saved + " budget row(s)");
		}
		catch (RuntimeException | SQLException ex)
		{
			this.status.setText("Save failed: " + ex.getMessage());
		}
	}

	private void loadRows()
	{
		if (!Database.isInitialized())
		{
			this.table.getItems().setAll(new BudgetRow("", "General", "", "0.00"));
			this.status.setText("Database not initialized. Open/Create H2 DB and open a company, then press Refresh.");
			return;
		}
		try
		{
			this.table.getItems().setAll(this.store.loadEditorRows());
			if (this.table.getItems().isEmpty())
			{
				this.table.getItems().add(new BudgetRow("", "General", "", "0.00"));
			}
		}
		catch (RuntimeException | SQLException ex)
		{
			this.table.getItems().setAll(new BudgetRow("", "General", "", "0.00"));
			this.status.setText("Load failed: " + ex.getMessage());
		}
	}

	private void onDeleteSelected()
	{
		BudgetRow selected = this.table.getSelectionModel().getSelectedItem();
		if (selected == null)
		{
			this.status.setText("Select a budget row to delete.");
			return;
		}
		this.table.getItems().remove(selected);
		this.status.setText("Deleted selected budget row (save to persist).");
	}

	public record BudgetRow(String account, String fund, String period, String amount)
	{
		BudgetRow withAccount(String value) { return new BudgetRow(value, this.fund, this.period, this.amount); }
		BudgetRow withFund(String value) { return new BudgetRow(this.account, value, this.period, this.amount); }
		BudgetRow withPeriod(String value) { return new BudgetRow(this.account, this.fund, value, this.amount); }
		BudgetRow withAmount(String value) { return new BudgetRow(this.account, this.fund, this.period, value); }
	}
}
