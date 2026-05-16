
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;
import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.ui.UiSpacing;


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
	 * This section includes TextFields for "From" fund, "To" fund, and "Amount",
	 * and a "Transfer" button. It's placed at the top of the panel.
	 * Error handling for invalid input (non-positive amount, non-existent funds) is included.
	 */
	private void buildTransferPane()
	{
		TextField fromField = new TextField();
		TextField toField = new TextField();
		TextField amtField = new TextField();
		Button transfer = new Button("Transfer");
		transfer.setOnAction(e -> {
			
			try
			{
				BigDecimal amt = new BigDecimal(amtField.getText().trim());
				if (amt.compareTo(BigDecimal.ZERO) <= 0)
					throw new NumberFormatException();
				this.service.transferFunds(fromField.getText().trim(),
					toField.getText().trim(), amt);
				alert("Transfer complete.");
				fromField.clear();
				toField.clear();
				amtField.clear();
				refresh();
				save();
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
			new Label("From:"), fromField,
			new Label("To:"), toField,
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
		TableColumn<FundRow, String> nameCol = new TableColumn<>("Fund");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<FundRow, BigDecimal> balCol = new TableColumn<>("Balance");
		balCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
		this.table.getColumns().addAll(nameCol, balCol);
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
		Button del = new Button("Delete Fund");
		add.setOnAction(e -> addFundDialog());
		del.setOnAction(e -> deleteFundDialog());
		HBox box = new HBox(UiSpacing.SECTION_SPACING, add, del);
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
					alert("Fund added.");
					refresh();
					save();
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
	 * Displays a dialog for deleting an existing fund.
	 * It prompts the user to enter the name of the fund they wish to delete using a {@link TextInputDialog}.
	 * If a name is provided, it attempts to remove the fund via the {@link #service}.
	 * Alerts are shown indicating whether the fund was successfully deleted or not found,
	 * and the table is refreshed on success.
	 */
	private void deleteFundDialog()
	{
		TextInputDialog dlg = new TextInputDialog();
		dlg.setTitle("Delete Fund");
		dlg.setHeaderText("Enter fund name to delete");
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
	public static class FundRow
	{
		/** The name of the fund. */
		private final String name;
		/** The current balance of the fund. */
		private final BigDecimal balance;
		
		/**
		 * Constructs a {@code FundRow} from a {@link Fund} object.
		 *
		 * @param f The {@link Fund} object from which to extract data. Must not be null.
		 */
		public FundRow(Fund f)
		{
			this.name = f.getName();
			this.balance = f.getBalance();
			
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
		
	}
	
}
