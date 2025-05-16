
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.service.FundAccountingService;

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
	
	private final FundAccountingService service;
	private final TableView<FundRow> table = new TableView<>();
	
	public FundsPanelFX(FundAccountingService service)
	{
		this.service = service;
		setPadding(new Insets(10));
		buildTransferPane();
		buildTable();
		buildManagementPane();
		refresh();
	}
	
	/* ───────────────────────── UI sections ───────────────────────── */
	
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
				this.service.transferFunds(fromField.getText().trim(), toField.getText().trim(), amt);
				alert("Transfer complete.");
				fromField.clear();
				toField.clear();
				amtField.clear();
				refresh();
			}
			catch (@SuppressWarnings("unused") NumberFormatException ex)
			{
				alert("Please enter a positive numeric amount.");
			}
			catch (IllegalArgumentException ex)
			{
				alert(ex.getMessage());
			}
			
		});
		FlowPane fp = new FlowPane(10, 10,
			new Label("From:"), fromField,
			new Label("To:"), toField,
			new Label("Amount:"), amtField,
			transfer);
		fp.setPadding(new Insets(8));
		TitledPane tp = new TitledPane("Transfer Funds", fp);
		tp.setCollapsible(false);
		setTop(tp);
	}
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<FundRow, String> nameCol = new TableColumn<>("Fund");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<FundRow, BigDecimal> balCol = new TableColumn<>("Balance");
		balCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
		this.table.getColumns().addAll(nameCol, balCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setCenter(this.table);
	}
	
	private void buildManagementPane()
	{
		Button add = new Button("Add Fund");
		Button del = new Button("Delete Fund");
		add.setOnAction(e -> addFundDialog());
		del.setOnAction(e -> deleteFundDialog());
		HBox box = new HBox(10, add, del);
		box.setPadding(new Insets(8));
		TitledPane tp = new TitledPane("Fund Management", box);
		tp.setCollapsible(false);
		setBottom(tp);
	}
	
	/* ───────────────────────── Dialog helpers ───────────────────────── */
	
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
				}
				catch (@SuppressWarnings("unused") NumberFormatException ex)
				{
					alert("Invalid balance amount.");
				}
				
			});
		});
	}
	
	/**
	 * 
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
			}
			else
				alert("Fund not found.");
				
		});
	}
	
	/* ───────────────────────── Utility ───────────────────────── */
	
	private void refresh()
	{
		List<Fund> funds = this.service.listFunds();
		this.table.getItems().setAll(funds.stream().map(FundRow::new).toList());
	}
	
	/**
	 * 
	 * @param msg
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
	/**
	 * 
	 */
	/* Row wrapper for TableView */
	public static class FundRow
	{
		private final String name;
		private final BigDecimal balance;
		
		public FundRow(Fund f)
		{
			this.name = f.getName();
			this.balance = f.getBalance();
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public BigDecimal getBalance()
		{
			return this.balance;
		}
		
	}
	
}
