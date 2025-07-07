
package nonprofitbookkeeping.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BudgetLineDialogFX extends Dialog<BudgetLine>
{
	
	private BudgetLine budgetLine;
	private ChartOfAccounts chartOfAccounts;
	private List<Fund> availableFunds;
	
	private ComboBox<Account> cmbAccount;
	private TextField txtTotalAmount;
	private ComboBox<Periodicity> cmbPeriodicity;
	private ComboBox<Fund> cmbFund; // Will add a "None" option representation
	
	private static final Fund NO_FUND_SENTINEL = new Fund(); // Sentinel for "None" option
	
	public BudgetLineDialogFX(String title, BudgetLine lineToEdit, ChartOfAccounts coa,
		List<Fund> funds)
	{
		this.chartOfAccounts = Objects.requireNonNull(coa, "ChartOfAccounts cannot be null.");
		this.availableFunds = funds != null ? new ArrayList<>(funds) : new ArrayList<>();
		
		if (lineToEdit != null)
		{
			this.budgetLine = lineToEdit;
		}
		else
		{
			this.budgetLine = new BudgetLine();
			
			// Default periodicity for new lines
			if (this.budgetLine.getPeriodicity() == null)
			{
				this.budgetLine.setPeriodicity(Periodicity.ANNUAL);
			}
			
		}
		
		NO_FUND_SENTINEL.setName("None"); // Initialize sentinel display name
		
		setTitle(title);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		initializeComponents();
		GridPane grid = createLayout();
		getDialogPane().setContent(grid);
		
		populateFields();
		setupResultConverter();
	}
	
	private void initializeComponents()
	{
		this.cmbAccount = new ComboBox<>();
		
		if (this.chartOfAccounts.getAccounts() != null)
		{
			List<Account> sortedAccounts = this.chartOfAccounts.getAccounts().stream()
				.filter(Objects::nonNull)
				.sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
				.collect(Collectors.toList());
			this.cmbAccount.setItems(FXCollections.observableArrayList(sortedAccounts));
		}
		
		this.cmbAccount.setConverter(new StringConverter<Account>()
		{
			@Override public String toString(Account account)
			{
				return (account == null || account.getName() == null) ? "" :
					account.getName() + " (" + account.getAccountNumber() + ")";
			}
			
			@Override public Account fromString(String string)
			{
				return null;
				/* Not needed for ComboBox-only use */ }
				
		});
		// Custom cell factory for better display (optional but good practice)
		this.cmbAccount.setCellFactory(listView -> new ListCell<Account>()
		{
			@Override protected void updateItem(Account account, boolean empty)
			{
				super.updateItem(account, empty);
				setText(empty || account == null ? "" :
					account.getName() + " (" + account.getAccountNumber() + ")");
			}
			
		});
		
		
		this.txtTotalAmount = new TextField();
		this.txtTotalAmount.setPromptText("0.00");
		
		this.cmbPeriodicity = new ComboBox<>(FXCollections.observableArrayList(Periodicity.values()));
		
		this.cmbFund = new ComboBox<>();
		List<Fund> fundItemsWithNone = new ArrayList<>();
		fundItemsWithNone.add(NO_FUND_SENTINEL); // "None" option
		
		if (this.availableFunds != null)
		{
			List<Fund> sortedFunds = this.availableFunds.stream()
				.filter(Objects::nonNull)
				.sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
				.collect(Collectors.toList());
			fundItemsWithNone.addAll(sortedFunds);
		}
		
		this.cmbFund.setItems(FXCollections.observableArrayList(fundItemsWithNone));
		this.cmbFund.setConverter(new StringConverter<Fund>()
		{
			@Override public String toString(Fund fund)
			{
				return (fund == null || fund.getName() == null) ? "" : fund.getName();
			}
			
			@Override public Fund fromString(String string)
			{
				return null;
			}
			
		});
		this.cmbFund.setCellFactory(listView -> new ListCell<Fund>()
		{
			@Override protected void updateItem(Fund fund, boolean empty)
			{
				super.updateItem(fund, empty);
				setText(empty || fund == null ? "" : fund.getName());
			}
			
		});
	}
	
	private GridPane createLayout()
	{
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10)); // top, right, bottom, left
		
		grid.add(new Label("Account:"), 0, 0);
		grid.add(this.cmbAccount, 1, 0);
		GridPane.setHgrow(this.cmbAccount, Priority.ALWAYS);
		
		grid.add(new Label("Total Budgeted Amount:"), 0, 1);
		grid.add(this.txtTotalAmount, 1, 1);
		GridPane.setHgrow(this.txtTotalAmount, Priority.ALWAYS);
		
		grid.add(new Label("Periodicity:"), 0, 2);
		grid.add(this.cmbPeriodicity, 1, 2);
		GridPane.setHgrow(this.cmbPeriodicity, Priority.ALWAYS);
		
		grid.add(new Label("Line-specific Fund:"), 0, 3);
		grid.add(this.cmbFund, 1, 3);
		GridPane.setHgrow(this.cmbFund, Priority.ALWAYS);
		
		return grid;
	}
	
	private void populateFields()
	{
		
		if (this.budgetLine.getAccountId() != null && this.chartOfAccounts.getAccounts() != null)
		{
			// FIXME
	//		chartOfAccounts.getAccount(budgetLine.getAccountId())
	//			.ifPresent(cmbAccount::setValue);
		}
		else
		{
			this.cmbAccount.getSelectionModel().clearSelection();
		}
		
		this.txtTotalAmount.setText(this.budgetLine.getTotalBudgetedAmount() != null ?
			this.budgetLine.getTotalBudgetedAmount().toPlainString() : "");
		this.cmbPeriodicity.setValue(this.budgetLine.getPeriodicity());
		
		if (this.budgetLine.getFundId() != null && this.availableFunds != null)
		{
			Optional<Fund> selectedFund = this.availableFunds.stream()
				.filter(f -> this.budgetLine.getFundId().equals(f.getFundId()))
				.findFirst();
			this.cmbFund.setValue(selectedFund.orElse(NO_FUND_SENTINEL));
		}
		else
		{
			this.cmbFund.setValue(NO_FUND_SENTINEL); // Default to "None"
		}
		
	}
	
	private void setupResultConverter()
	{
		setResultConverter(dialogButton -> {
			
			if (dialogButton == ButtonType.OK)
			{
				
				// Basic validation (can be expanded)
				if (this.cmbAccount.getValue() == null)
				{
					showValidationError("Account must be selected.");
					return null; // Prevents dialog from closing
				}
				
				String amountStr = this.txtTotalAmount.getText().trim();
				
				if (amountStr.isEmpty())
				{
					showValidationError("Total Budgeted Amount cannot be empty.");
					return null;
				}
				
				try
				{
					BigDecimal totalAmount = new BigDecimal(amountStr);
					
					if (totalAmount.compareTo(BigDecimal.ZERO) < 0)
					{
						showValidationError("Total Budgeted Amount cannot be negative.");
						return null;
					}
					
					this.budgetLine.setTotalBudgetedAmount(totalAmount);
				}
				catch (NumberFormatException e)
				{
					showValidationError("Invalid number format for Total Budgeted Amount.");
					return null;
				}
				
				this.budgetLine.setAccountId(this.cmbAccount.getValue().getAccountNumber());
				this.budgetLine.setAccountName(this.cmbAccount.getValue().getName()); // Store name for
																			// convenience
				this.budgetLine.setPeriodicity(this.cmbPeriodicity.getValue());
				
				Fund selectedFund = this.cmbFund.getValue();
				
				if (selectedFund != null && selectedFund != NO_FUND_SENTINEL)
				{
					this.budgetLine.setFundId(selectedFund.getFundId());
				}
				else
				{
					this.budgetLine.setFundId(null); // "None" selected
				}
				
				return this.budgetLine;
			}
			
			return null; // Cancel or close
		});
	}
	
	private void showValidationError(String message)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Validation Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
}
