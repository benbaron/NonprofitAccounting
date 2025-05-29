
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.service.AccountService;

/**
 * JavaFX port of {@code AccountsPanel}. Shows the chart of accounts and basic
 * add/edit/delete row operations.
 */
public class AccountsPanelFX extends BorderPane
{	
	private final TableView<AccountRow> table = new TableView<>();
	private final ObservableList<AccountRow> rows = FXCollections.observableArrayList();
	public AccountsPanelFX(@SuppressWarnings("unused") AccountService service)
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(new TitledPane("Chart of Accounts", this.table)
		{
			{
				setCollapsible(false);
			}
			
		});
		setBottom(buildControls());
		refresh();
	}
	
	/* --------------------------------------------------------------------- */
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<AccountRow, String> codeCol = col("Account Code", "code");
		TableColumn<AccountRow, String> nameCol = col("Account Name", "name");
		TableColumn<AccountRow, String> typeCol = col("Type", "type");
		TableColumn<AccountRow, String> parentCol = col("Parent Account", "parent");
		TableColumn<AccountRow, String> curCol = col("Currency", "currency");
		TableColumn<AccountRow, BigDecimal> balCol = new TableColumn<>("Opening Balance");
		balCol.setCellValueFactory(new PropertyValueFactory<>("opening"));
		
		this.table.getColumns().addAll(codeCol, nameCol, 
			typeCol, parentCol, 
			curCol, balCol);
		this.table.setItems(this.rows);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	private static <T> TableColumn<AccountRow, T> col(String title, String prop)
	{
		TableColumn<AccountRow, T> c = new TableColumn<>(title);
		c.setCellValueFactory(new PropertyValueFactory<>(prop));
		return c;
	}
	
	private HBox buildControls()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(8));
		Button add = new Button("Add Account");
		Button edit = new Button("Edit Account");
		Button delete = new Button("Delete Account");
		
		add.setOnAction(e -> this.rows.add(new AccountRow()));
		edit.setOnAction(e -> {
			
			if (this.table.getSelectionModel().isEmpty())
			{
				alert("Please select an account to edit.");
			}
			else
			{
				this.table.edit(this.table.getSelectionModel().getSelectedIndex(), this.table.getColumns().get(0));
			}
			
		});
		delete.setOnAction(e -> {
			int idx = this.table.getSelectionModel().getSelectedIndex();
			if (idx >= 0)
				this.rows.remove(idx);
		});
		box.getChildren().addAll(add, edit, delete);
		return box;
	}
	
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
	private void refresh()
	{
		this.rows.clear();
		List<Account> accounts = AccountService.getAllAccounts();
		accounts.forEach(a -> this.rows.add(new AccountRow(a)));
	}
	
	/* --------------------------------------------------------------------- */
	public static class AccountRow
	{
		private String code = "", name = "", currency = "USD";
		Account parent;
		private BigDecimal opening = BigDecimal.ZERO;
		
		private AccountType type;
		
		public AccountRow()
		{
		}
		
		public AccountRow(Account a)
		{
			this.code = a.getAccountCode();
			this.name = a.getName();
			this.type = a.getAccountType();
			this.parent = a.getParentAccount();
			this.currency = a.getCurrency();
			this.opening = a.getOpeningBalance();
		}
		
		/* getters / setters for PropertyValueFactory */
		public String getCode()
		{
			return this.code;
		}
		
		public void setCode(String s)
		{
			this.code = s;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public void setName(String s)
		{
			this.name = s;
		}
		
		public AccountType getType()
		{
			return this.type;
		}
		
		public void setType(AccountType s)
		{
			this.type = s;
		}
		
		public Account getParent()
		{
			return this.parent;
		}
		
		public void setParent(Account s)
		{
			this.parent = s;
		}
		
		public String getCurrency()
		{
			return this.currency;
		}
		
		public void setCurrency(String s)
		{
			this.currency = s;
		}
		
		public BigDecimal getOpening()
		{
			return this.opening;
		}
		
		public void setOpening(BigDecimal b)
		{
			this.opening = b;
		}
		
	}
	
}
