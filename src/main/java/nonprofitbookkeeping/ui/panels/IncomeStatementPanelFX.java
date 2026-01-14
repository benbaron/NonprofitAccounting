package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.ui.helpers.TableExportUtils;
import nonprofitbookkeeping.util.FormatUtils;

/**
 * Displays the income statement as a JavaFX table with print and export actions.
 */
public class IncomeStatementPanelFX extends BorderPane
{
	private final TableView<StatementRow> table =
		new TableView<>();
	private final ObservableList<StatementRow> rows =
		FXCollections.observableArrayList();
	private final Button refreshButton = new Button("Refresh");
	private final Button printButton = new Button("Print");
	private final MenuButton exportMenu = new MenuButton("Export");
	private CompanyChangeListener companyListener;

	/** Constructs the panel and loads the current company data. */
	public IncomeStatementPanelFX()
	{
		setPadding(new Insets(10));

		this.table.setItems(this.rows);
		this.table
			.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.table.setPlaceholder(new Label("No income statement data."));

		setupColumns();
		setCenter(this.table);

		HBox actions = new HBox(10, this.refreshButton, this.printButton,
			this.exportMenu);
		actions.setPadding(new Insets(0, 0, 10, 0));
		setTop(actions);

		this.refreshButton.setOnAction(e -> refresh());
		this.printButton.setTooltip(new Tooltip("Print this table."));
		this.printButton.setOnAction(e -> TableExportUtils.printTable(this.table));

		MenuItem exportPdf = new MenuItem("Export to PDF");
		exportPdf.setOnAction(e -> TableExportUtils.exportTableToPdf(this.table,
			"Income Statement",
			getScene() != null ? getScene().getWindow() : null));
		MenuItem exportXlsx = new MenuItem("Export to Excel");
		exportXlsx.setOnAction(e -> TableExportUtils.exportTableToXlsx(this.table,
			"Income Statement",
			getScene() != null ? getScene().getWindow() : null));
		this.exportMenu.getItems().addAll(exportPdf, exportXlsx);
		this.exportMenu
			.setTooltip(new Tooltip("Export this table to PDF or Excel."));

		registerCompanyListener();
		refresh();
	}

	private void setupColumns()
	{
		TableColumn<StatementRow, String> categoryCol =
			new TableColumn<>("Category");
		categoryCol.setCellValueFactory(data -> data.getValue().category);

		TableColumn<StatementRow, String> numberCol =
			new TableColumn<>("Account #");
		numberCol.setCellValueFactory(data -> data.getValue().accountNumber);

		TableColumn<StatementRow, String> nameCol =
			new TableColumn<>("Account Name");
		nameCol.setCellValueFactory(data -> data.getValue().accountName);

		TableColumn<StatementRow, String> balanceCol =
			new TableColumn<>("Amount");
		balanceCol.setCellValueFactory(data -> data.getValue().balance);

		this.table.getColumns().addAll(categoryCol, numberCol, nameCol,
			balanceCol);
	}

	private void refresh()
	{
		this.rows.clear();

		Company company = CurrentCompany.getCompany();
		if (company == null || company.getChartOfAccounts() == null)
		{
			this.table.setPlaceholder(new Label("No company loaded."));
			return;
		}

		ChartOfAccounts chart = company.getChartOfAccounts();
		Ledger ledger = company.getLedger();

		BigDecimal totalIncome = BigDecimal.ZERO;
		BigDecimal totalExpense = BigDecimal.ZERO;

		List<Account> accounts = new ArrayList<>(chart.getAccounts());
		for (Account account : accounts)
		{
			AccountType type = account.getAccountType();
			if (type == null)
			{
				continue;
			}

			if (type == AccountType.INCOME)
			{
				BigDecimal balance = account.totalAccountBalance(ledger);
				totalIncome = totalIncome.add(balance);
				this.rows.add(new StatementRow("Income",
					account.getAccountNumber(), account.getName(),
					FormatUtils.formatCurrency(balance)));
			}
			else if (type == AccountType.EXPENSE)
			{
				BigDecimal balance = account.totalAccountBalance(ledger);
				totalExpense = totalExpense.add(balance);
				this.rows.add(new StatementRow("Expense",
					account.getAccountNumber(), account.getName(),
					FormatUtils.formatCurrency(balance)));
			}
		}

		this.rows.add(new StatementRow("Total Income", "", "",
			FormatUtils.formatCurrency(totalIncome)));
		this.rows.add(new StatementRow("Total Expenses", "", "",
			FormatUtils.formatCurrency(totalExpense)));
		this.rows.add(new StatementRow("Net Income", "", "",
			FormatUtils.formatCurrency(totalIncome.subtract(totalExpense))));
	}

	private void registerCompanyListener()
	{
		this.companyListener = companyNowOpen -> refresh();
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
	}

	/** Holds a row for the income statement table. */
	public static class StatementRow
	{
		private final StringProperty category;
		private final StringProperty accountNumber;
		private final StringProperty accountName;
		private final StringProperty balance;

		StatementRow(String category, String accountNumber, String accountName,
			String balance)
		{
			this.category = new SimpleStringProperty(category);
			this.accountNumber = new SimpleStringProperty(accountNumber);
			this.accountName = new SimpleStringProperty(accountName);
			this.balance = new SimpleStringProperty(balance);
		}
	}
}
