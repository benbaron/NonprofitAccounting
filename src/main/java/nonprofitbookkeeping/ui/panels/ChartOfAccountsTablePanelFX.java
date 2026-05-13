package nonprofitbookkeeping.ui.panels;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.ui.UiSpacing;
import nonprofitbookkeeping.ui.helpers.TableExportUtils;

/**
 * Displays the chart of accounts as a JavaFX table with print/export actions.
 */
public class ChartOfAccountsTablePanelFX extends BorderPane
{
	private final TableView<AccountRow> table = new TableView<>();
	private final ObservableList<AccountRow> rows =
		FXCollections.observableArrayList();
	private final Button refreshButton = new Button("Refresh");
	private final Button printButton = new Button("Print");
	private final MenuButton exportMenu = new MenuButton("Export");
	private CompanyChangeListener companyListener;

	/** Constructs the panel and loads the current company chart of accounts. */
	public ChartOfAccountsTablePanelFX()
	{
		setPadding(UiSpacing.pageInsets());

		this.table.setItems(this.rows);
		this.table
			.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.table.setPlaceholder(new Label("No chart of accounts data."));

		setupColumns();
		setCenter(this.table);

		HBox actions = new HBox(UiSpacing.SECTION_SPACING, this.refreshButton, this.printButton,
			this.exportMenu);
		actions.setPadding(UiSpacing.actionBarTopMargin());
		setTop(actions);

		this.refreshButton.setOnAction(e -> refreshData());
		this.printButton.setTooltip(new Tooltip("Print this table."));
		this.printButton.setOnAction(e -> TableExportUtils.printTable(this.table));

		MenuItem exportPdf = new MenuItem("Export to PDF");
		exportPdf.setOnAction(e -> TableExportUtils.exportTableToPdf(this.table,
			"Chart of Accounts",
			getScene() != null ? getScene().getWindow() : null));
		MenuItem exportXlsx = new MenuItem("Export to Excel");
		exportXlsx.setOnAction(e -> TableExportUtils.exportTableToXlsx(this.table,
			"Chart of Accounts",
			getScene() != null ? getScene().getWindow() : null));
		this.exportMenu.getItems().addAll(exportPdf, exportXlsx);
		this.exportMenu
			.setTooltip(new Tooltip("Export this table to PDF or Excel."));

		registerCompanyListener();
		refreshData();
	}

	private void setupColumns()
	{
		TableColumn<AccountRow, String> numberCol =
			new TableColumn<>("Account #");
		numberCol.setCellValueFactory(data -> data.getValue().accountNumber);

		TableColumn<AccountRow, String> nameCol =
			new TableColumn<>("Account Name");
		nameCol.setCellValueFactory(data -> data.getValue().accountName);

		TableColumn<AccountRow, String> typeCol =
			new TableColumn<>("Account Type");
		typeCol.setCellValueFactory(data -> data.getValue().accountType);

		TableColumn<AccountRow, String> sideCol =
			new TableColumn<>("Increase Side");
		sideCol.setCellValueFactory(data -> data.getValue().increaseSide);

		TableColumn<AccountRow, String> parentCol =
			new TableColumn<>("Parent Account");
		parentCol.setCellValueFactory(data -> data.getValue().parentAccount);

		this.table.getColumns().addAll(numberCol, nameCol, typeCol, sideCol,
			parentCol);
	}

	
	/**
	 * Backward-compatible refresh entrypoint for existing classic callers.
	 */
	@Deprecated
	public void refresh()
	{
		refreshData();
	}

	/** Refreshes the table rows from the currently open company's chart of accounts. */
	public void refreshData()
	{
		this.rows.clear();

		Company company = CurrentCompany.getCompany();
		if (company == null || company.getChartOfAccounts() == null)
		{
			this.table.setPlaceholder(new Label("No company loaded."));
			return;
		}

		ChartOfAccounts chart = company.getChartOfAccounts();
		List<Account> accounts = new ArrayList<>(chart.getAccounts());
		for (Account account : accounts)
		{
			this.rows.add(new AccountRow(account));
		}
	}

	private void registerCompanyListener()
	{
		this.companyListener = companyNowOpen -> refreshData();
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
	}

	/** Row wrapper for the table. */
	public static class AccountRow
	{
		private final StringProperty accountNumber;
		private final StringProperty accountName;
		private final StringProperty accountType;
		private final StringProperty increaseSide;
		private final StringProperty parentAccount;

		AccountRow(Account account)
		{
			this.accountNumber = new SimpleStringProperty(
				account != null ? account.getAccountNumber() : "");
			this.accountName = new SimpleStringProperty(
				account != null ? account.getName() : "");
			this.accountType = new SimpleStringProperty(
				account != null && account.getAccountType() != null ?
					account.getAccountType().name() : "");
			this.increaseSide = new SimpleStringProperty(
				account != null && account.getIncreaseSide() != null ?
					account.getIncreaseSide().name() : "");
			this.parentAccount = new SimpleStringProperty(
				account != null ? account.getParentAccountId() : "");
		}
	}
}
