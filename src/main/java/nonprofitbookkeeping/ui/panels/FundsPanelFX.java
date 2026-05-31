
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.ui.UiSpacing;
import nonprofitbookkeeping.ui.FundNameLookup;


/**
 * JavaFX translation of the Swing {@code FundsPanel}. Supports:
 * <ul>
 *     <li>Transferring money between funds</li>
 *     <li>Adding a new fund with opening balance</li>
 *     <li>Deleting a fund</li>
 *     <li>Live table of balances</li>
 * </ul>
 */
public class FundsPanelFX extends BorderPane
{
	
	/** The service layer for fund accounting operations. */
	private final FundAccountingService service;
	/** Directory where data should be persisted, may be null. */
	private final File companyDirectory;
	/** TableView to display fund names and their balances. */
	private final TableView<FundRow> table = new TableView<>();
	/** Fund selector for transfer source. */
	private ComboBox<String> fromFundBox;
	/** Fund selector for transfer destination. */
	private ComboBox<String> toFundBox;
	/** Account selector for recording fund transfers in the journal. */
	private ComboBox<Account> transferAccountBox;
	
	/**
	 * Constructs a new {@code FundsPanelFX}.
	 * Initializes the panel with the necessary {@link FundAccountingService} and builds the UI components,
	 * including sections for fund transfers, a table displaying fund balances, and fund management actions.
	 *
	 * @param service The {@link FundAccountingService} to be used for all fund-related operations. Must not be null.
	 * @param companyDirectory the company directory
	 */
	public FundsPanelFX(FundAccountingService service, File companyDirectory)
	{
		this.service = service;
		this.companyDirectory = companyDirectory;
		
		try
		{
			this.service.loadFunds(this.companyDirectory);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		setPadding(PanelChrome.PANEL_PADDING);
		setTop(PanelChrome.topSection("Funds and Fund Accounting"));
		buildTransferPane();
		
		buildTable();
		
		buildManagementPane();
		refresh();
		
	}
	
	/**
	 * Convenience constructor when no directory is available.
	 *
	 * @param service the service
	 */
	public FundsPanelFX(FundAccountingService service)
	{
		this(service, null);
		
	}
	
	/* ───────────────────────── UI sections ───────────────────────── */
	
	/**
	 * Builds the UI section for transferring funds.
	 * This section includes combo boxes for "From" fund and "To" fund, a TextField for "Amount",
	 * and a "Transfer" button. It's placed at the top of the panel.
	 * Error handling for invalid input (non-positive amount, non-existent funds) is included.
	 */
	private void buildTransferPane()
	{
		this.fromFundBox = new ComboBox<>();
		this.toFundBox = new ComboBox<>();
		this.transferAccountBox = new ComboBox<>();
		this.fromFundBox.setPromptText("Select fund");
		this.toFundBox.setPromptText("Select fund");
		this.transferAccountBox.setPromptText("Select account");
		this.transferAccountBox.setConverter(accountConverter());
		this.transferAccountBox.setOnShowing(e -> refreshAccountComboItems());
		refreshFundComboItems(this.fromFundBox, this.toFundBox);
		refreshAccountComboItems();
		TextField amtField = new TextField();
		Button transfer = new Button("Transfer");
		transfer.setOnAction(e -> {
			
			try
			{
				BigDecimal amt = new BigDecimal(amtField.getText().trim());
				if (amt.compareTo(BigDecimal.ZERO) <= 0)
					throw new NumberFormatException();
				String fromFund = selectedFundName(this.fromFundBox);
				String toFund = selectedFundName(this.toFundBox);
				if (fromFund.isBlank() || toFund.isBlank())
				{
					throw new IllegalArgumentException("Select source and destination funds.");
				}
				Account transferAccount = selectedTransferAccount();
				if (transferAccount == null)
				{
					throw new IllegalArgumentException("Select an account for the transfer journal entry.");
				}
				recordFundTransfer(fromFund, toFund, amt, transferAccount);
				alert("Transfer journal entry recorded.");
				this.fromFundBox.getSelectionModel().clearSelection();
				this.toFundBox.getSelectionModel().clearSelection();
				amtField.clear();
				refresh();
			}
			catch (@SuppressWarnings("unused")
			NumberFormatException ex)
			{
				alert("Please enter a positive numeric amount.");
			}
			catch (IllegalArgumentException ex)
			{
				alert(ex.getMessage());
			}
			
		});
		FlowPane fp = new FlowPane(UiSpacing.SECTION_SPACING, UiSpacing.SECTION_SPACING,
			new Label("From:"), this.fromFundBox,
			new Label("To:"), this.toFundBox,
			new Label("Account:"), this.transferAccountBox,
			new Label("Amount:"), amtField,
			transfer);
		fp.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		TitledPane tp = new TitledPane("Transfer Funds", fp);
		tp.setCollapsible(false);
		setTop(tp);
		
	}
	
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying fund information.
	 * It defines columns for Fund Name and Balance, using {@link PropertyValueFactory}
	 * to bind them to the properties of the {@link FundRow} class.
	 * The table is centered in this {@link BorderPane}.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * uses reflection and can lead to type safety warnings if property names don't strictly match
	 * Java bean conventions or if raw types are inferred. "deprecation" might relate to older patterns
	 * of using PropertyValueFactory.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	private void buildTable()
	{
		TableColumn<FundRow, String> nameCol = new TableColumn<>("Fund Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<FundRow, String> accountsCol =
			new TableColumn<>("Associated Accounts");
		accountsCol.setCellValueFactory(
			new PropertyValueFactory<>("associatedAccounts"));
		TableColumn<FundRow, String> balCol = new TableColumn<>("Balance");
		balCol.setCellValueFactory(new PropertyValueFactory<>("displayBalance"));
		this.table.getColumns().addAll(nameCol, accountsCol, balCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setCenter(this.table);
		
	}
	
	/**
	 * Builds the UI section for fund management.
	 * This section includes "Add Fund" and "Delete Fund" buttons and is placed at the bottom of the panel.
	 */
	private void buildManagementPane()
	{
		Button add = new Button("Add Fund");
		Button edit = new Button("Edit Fund");
		Button del = new Button("Delete Fund");
		add.setOnAction(e -> addFundDialog());
		edit.setOnAction(e -> editFundDialog());
		del.setOnAction(e -> deleteFundDialog());
		HBox box = new HBox(UiSpacing.SECTION_SPACING, add, edit, del);
		box.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		TitledPane tp = new TitledPane("Fund Management", box);
		tp.setCollapsible(false);
		setBottom(tp);
		
	}
	
	/* ───────────────────────── Dialog helpers ───────────────────────── */
	
	/**
	 * Displays a dialog sequence for adding a new fund.
	 * First, it prompts for the new fund's name using a {@link TextInputDialog}.
	 * If a name is provided, it then prompts for the initial balance for that fund, defaulting to "0.00".
	 * If both are provided and the balance is valid, a new {@link Fund} is created,
	 * added via the {@link #service}, and the table is refreshed.
	 * Alerts are shown for success or invalid balance input.
	 */
	private void addFundDialog()
	{
		TextInputDialog nameDlg = new TextInputDialog();
		nameDlg.setTitle("Add Fund");
		nameDlg.setHeaderText("Enter new fund name");
		nameDlg.showAndWait().ifPresent(name -> {
			TextInputDialog balDlg = new TextInputDialog("0.00");
			balDlg.setTitle("Initial Balance");
			balDlg.setHeaderText("Enter opening balance for " + name);
			balDlg.showAndWait().ifPresent(balStr -> {
				
				try
				{
					BigDecimal bal = new BigDecimal(balStr.trim());
					Fund f = new Fund(name);
					f.setBalance(bal);
					this.service.addFund(f);
					save();
					alert("Fund added.");
					refresh();
				}
				catch (@SuppressWarnings("unused")
				NumberFormatException ex)
				{
					alert("Invalid balance amount.");
				}
				
			});
		});
		
	}
	

	/**
	 * Displays a dialog for editing the selected fund from the table.
	 */
	private void editFundDialog()
	{
		FundRow selected = this.table.getSelectionModel().getSelectedItem();
		if (selected == null)
		{
			alert("Select a fund to edit.");
			return;
		}

		Fund fund = this.service.findFund(selected.getName());
		if (fund == null)
		{
			alert("Fund not found.");
			return;
		}

		Dialog<FundEditResult> dialog = new Dialog<>();
		dialog.setTitle("Edit Fund");
		dialog.setHeaderText("Edit " + fund.getName());
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,
			ButtonType.CANCEL);

		TextField nameField = new TextField(fund.getName());
		TextField balanceField = new TextField(fund.getBalance() == null ? "0.00" :
			fund.getBalance().toPlainString());
		javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.setPadding(PanelChrome.PANEL_PADDING);
		grid.addRow(0, new Label("Fund name:"), nameField);
		grid.addRow(1, new Label("Display balance:"), balanceField);
		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(button -> {
			if (button != ButtonType.OK)
			{
				return null;
			}
			String newName = nameField.getText() == null ? "" :
				nameField.getText().trim();
			if (newName.isBlank())
			{
				alert("Fund name is required.");
				return null;
			}
			try
			{
				return new FundEditResult(newName,
					new BigDecimal(balanceField.getText().trim()));
			}
			catch (NumberFormatException ex)
			{
				alert("Invalid balance amount.");
				return null;
			}
		});

		dialog.showAndWait().ifPresent(result -> {
			try
			{
				this.service.editFund(fund.getName(), result.name(), result.balance());
				refresh();
				save();
				alert("Fund updated.");
			}
			catch (IllegalArgumentException ex)
			{
				alert(ex.getMessage());
			}
		});
	}

	/**
	 * Displays a dialog for deleting an existing fund.
	 * It prompts the user to choose the fund they wish to delete using a combo box dialog.
	 * If a name is provided, it attempts to remove the fund via the {@link #service}.
	 * Alerts are shown indicating whether the fund was successfully deleted or not found,
	 * and the table is refreshed on success.
	 */
	private void deleteFundDialog()
	{
		List<String> fundNames = fundNames();
		if (fundNames.isEmpty())
		{
			alert("No funds are available to delete.");
			return;
		}
		ChoiceDialog<String> dlg = new ChoiceDialog<>(fundNames.get(0), fundNames);
		dlg.setTitle("Delete Fund");
		dlg.setHeaderText("Select fund to delete");
		dlg.showAndWait().ifPresent(name -> {
			
			if (this.service.removeFund(name))
			{
				alert("Fund deleted.");
				refresh();
				save();
			}
			else
				alert("Fund not found.");
			
		});
		
	}
	
	/* ───────────────────────── Utility ───────────────────────── */
	
	/**
	 * Refreshes the data displayed in the funds {@link #table}.
	 * It fetches the current list of all funds from the {@link #service} and
	 * repopulates the table with {@link FundRow} objects created from these funds.
	 */
	private void refresh()
	{
		List<Fund> funds = this.service.listFunds();
		this.table.getItems().setAll(funds.stream().map(FundRow::new).toList());
		refreshFundComboItems(this.fromFundBox, this.toFundBox);
		refreshAccountComboItems();
		
	}


	private void recordFundTransfer(String fromFund, String toFund,
		BigDecimal amount, Account transferAccount)
	{
		if (fromFund.equals(toFund))
		{
			throw new IllegalArgumentException("Source and destination funds must differ.");
		}
		if (CurrentCompany.getCompany() == null ||
			CurrentCompany.getCompany().getLedger() == null ||
			CurrentCompany.getCompany().getLedger().getJournal() == null)
		{
			throw new IllegalArgumentException("Open a company before recording a fund transfer.");
		}

		AccountingTransaction tx = new AccountingTransaction();
		tx.setDate(java.time.LocalDate.now().toString());
		tx.setBookingDateTimestamp(System.currentTimeMillis());
		tx.setMemo("Fund transfer from " + fromFund + " to " + toFund);
		tx.setToFrom("Fund Transfer");
		tx.setAssociatedFundName(toFund);
		tx.setInfo(Map.of(
			"module", "FUND_TRANSFER",
			"from_fund", fromFund,
			"to_fund", toFund));

		String accountNumber = transferAccount.getAccountNumber();
		String accountName = transferAccount.getName();
		LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
		AccountingEntry debit = new AccountingEntry(amount, accountNumber,
			AccountSide.DEBIT, accountName);
		debit.setFundNumber(toFund);
		entries.add(debit);
		AccountingEntry credit = new AccountingEntry(amount, accountNumber,
			AccountSide.CREDIT, accountName);
		credit.setFundNumber(fromFund);
		entries.add(credit);
		tx.setEntries(entries);

		CurrentCompany.getCompany().getLedger().getJournal().addTransaction(tx);
		try
		{
			CurrentCompany.persist();
		}
		catch (IOException ex)
		{
			throw new IllegalArgumentException("Unable to save fund transfer journal entry.", ex);
		}
	}

	private void refreshAccountComboItems()
	{
		if (this.transferAccountBox == null)
		{
			return;
		}
		Account selected = this.transferAccountBox.getValue();
		List<Account> accounts = transferAccounts();
		this.transferAccountBox.getItems().setAll(accounts);
		if (selected != null)
		{
			selectTransferAccount(selected.getAccountNumber());
		}
		else if (CurrentCompany.getCompany() != null &&
			CurrentCompany.getCompany().getCompanyProfileModel() != null)
		{
			selectTransferAccount(CurrentCompany.getCompany().getCompanyProfileModel()
				.getDefaultBankAccount());
		}
	}

	private List<Account> transferAccounts()
	{
		if (CurrentCompany.getCompany() == null ||
			CurrentCompany.getCompany().getChartOfAccounts() == null)
		{
			return List.of();
		}
		return CurrentCompany.getCompany().getChartOfAccounts().getAccounts().stream()
			.filter(account -> account != null && account.getAccountNumber() != null &&
				!account.getAccountNumber().isBlank())
			.sorted(Comparator.comparing(Account::getAccountNumber))
			.toList();
	}

	private void selectTransferAccount(String accountNumber)
	{
		if (accountNumber == null || accountNumber.isBlank())
		{
			return;
		}
		for (Account account : this.transferAccountBox.getItems())
		{
			if (accountNumber.equals(account.getAccountNumber()))
			{
				this.transferAccountBox.getSelectionModel().select(account);
				return;
			}
		}
	}

	private Account selectedTransferAccount()
	{
		return this.transferAccountBox == null ? null : this.transferAccountBox.getValue();
	}

	private static StringConverter<Account> accountConverter()
	{
		return new StringConverter<>()
		{
			@Override
			public String toString(Account account)
			{
				if (account == null)
				{
					return "";
				}
				String number = account.getAccountNumber() == null ? "" :
					account.getAccountNumber();
				String name = account.getName() == null ? "" : account.getName();
				return name.isBlank() ? number : number + " — " + name;
			}

			@Override
			public Account fromString(String value)
			{
				return null;
			}
		};
	}

	@SafeVarargs
	private final void refreshFundComboItems(ComboBox<String>... comboBoxes)
	{
		List<String> fundNames = fundNames();
		for (ComboBox<String> comboBox : comboBoxes)
		{
			if (comboBox == null)
			{
				continue;
			}
			String selected = comboBox.getValue();
			comboBox.getItems().setAll(fundNames);
			if (selected != null && fundNames.contains(selected))
			{
				comboBox.getSelectionModel().select(selected);
			}
		}
	}

	private List<String> fundNames()
	{
		try
		{
			List<String> names = FundNameLookup.listActiveFundNames();
			if (!names.isEmpty())
			{
				return names;
			}
		}
		catch (Exception ignored)
		{
			// Fall back to the fund service below.
		}
		return this.service.listFunds().stream()
			.map(Fund::getName)
			.filter(name -> name != null && !name.isBlank())
			.sorted()
			.toList();
	}

	private static String selectedFundName(ComboBox<String> comboBox)
	{
		String value = comboBox == null ? null : comboBox.getValue();
		return value == null ? "" : value.trim();
	}
	
	/** Saves current funds to disk if a company directory is set. */
	private void save()
	{
		
		try
		{
			this.service.saveFunds(this.companyDirectory);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Displays a simple informational alert dialog with an OK button.
	 * 
	 * @param msg The message to be displayed in the alert dialog.
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK)
			.showAndWait();
		
	}
	
	/**
	 * A simple data class (POJO) used to represent a row in the funds {@link TableView}.
	 * It wraps a {@link Fund} object's name and balance for easy display with {@link PropertyValueFactory}.
	 */
	private record FundEditResult(String name, BigDecimal balance) {}

	public static class FundRow
	{
		/** The name of the fund. */
		private final String name;
		/** The current balance of the fund. */
		private final BigDecimal balance;
		/** The associated account summary. */
		private final String associatedAccounts;
		
		/**
		 * Constructs a {@code FundRow} from a {@link Fund} object.
		 *
		 * @param f The {@link Fund} object from which to extract data. Must not be null.
		 */
		public FundRow(Fund f)
		{
			this.name = f.getName();
			this.balance = f.getBalance();
			this.associatedAccounts = f.getAccountIds() == null ||
				f.getAccountIds().isEmpty() ? "—" :
				String.join(", ", f.getAccountIds());
			
		}
		
		/**
		 * Gets the name of the fund.
		 * @return The fund's name.
		 */
		public String getName()
		{
			return this.name;
			
		}
		
		/**
		 * Gets the balance of the fund.
		 * @return The fund's balance as a {@link BigDecimal}.
		 */
		public BigDecimal getBalance()
		{
			return this.balance;
			
		}

		public String getDisplayBalance()
		{
			return this.balance == null ? "" : this.balance.toPlainString();
		}

		public String getAssociatedAccounts()
		{
			return this.associatedAccounts;
		}
		
	}
	
}
