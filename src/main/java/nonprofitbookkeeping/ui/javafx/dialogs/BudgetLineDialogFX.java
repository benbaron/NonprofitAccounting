
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
import nonprofitbookkeeping.util.FormatUtils;
import java.util.stream.Collectors;
import java.math.RoundingMode;

// TODO: Auto-generated Javadoc
/**
 * The Class BudgetLineDialogFX.
 */
public class BudgetLineDialogFX extends Dialog<BudgetLine>
{
	
	/** The budget line. */
	private BudgetLine budgetLine;
	
	/** The chart of accounts. */
	private ChartOfAccounts chartOfAccounts;
	
	/** The available funds. */
	private List<Fund> availableFunds;
	
	/** The cmb account. */
	private ComboBox<Account> cmbAccount;
	
	/** The txt total amount. */
	private TextField txtTotalAmount;
	
	/** The cmb periodicity. */
	private ComboBox<Periodicity> cmbPeriodicity;
	
	/** The cmb fund. */
	private ComboBox<Fund> cmbFund; // Will add a "None" option representation
	
	/** The amount error label. */
	private Label amountErrorLabel;
	
	/** The periodic breakdown label. */
	private Label periodicBreakdownLabel;
	
	/** The ok button. */
	private Button okButton;
	
	/** The Constant NO_FUND_SENTINEL. */
	private static final Fund NO_FUND_SENTINEL = new Fund(); // Sentinel for
																// "None" option
	
	/**
	 * Instantiates a new budget line dialog FX.
	 *
	 * @param title the title
	 * @param lineToEdit the line to edit
	 * @param coa the coa
	 * @param funds the funds
	 */
	public BudgetLineDialogFX(String title, BudgetLine lineToEdit,
		ChartOfAccounts coa,
		List<Fund> funds)
	{
		this.chartOfAccounts =
			Objects.requireNonNull(coa, "ChartOfAccounts cannot be null.");
		this.availableFunds =
			funds != null ? new ArrayList<>(funds) : new ArrayList<>();
		
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
		getDialogPane().getButtonTypes().addAll(ButtonType.OK,
			ButtonType.CANCEL);
		
		initializeComponents();
		GridPane grid = createLayout();
		getDialogPane().setContent(grid);
		
		populateFields();
		setupResultConverter();
		attachListeners();
		
	}
	
	/**
	 * Initialize components.
	 */
	private void initializeComponents()
	{
		this.cmbAccount = new ComboBox<>();
		
		if (this.chartOfAccounts.getAccounts() != null)
		{
			List<Account> sortedAccounts = this.chartOfAccounts.getAccounts()
				.stream()
				.filter(Objects::nonNull)
				.sorted(
					(a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
				.collect(Collectors.toList());
			this.cmbAccount
				.setItems(FXCollections.observableArrayList(sortedAccounts));
		}
		
		this.cmbAccount.setConverter(new StringConverter<Account>()
		{
			@Override
			public String toString(Account account)
			{
				return (account == null || account.getName() == null) ? "" :
					account.getName() + " (" + account.getAccountNumber() + ")";
				
			}
			
			@Override
			public Account fromString(String string)
			{
				return null;
				
				/* Not needed for ComboBox-only use */ }
				
		});
		// Custom cell factory for better display (optional but good practice)
		this.cmbAccount.setCellFactory(listView -> new ListCell<Account>()
		{
			@Override
			protected void updateItem(Account account, boolean empty)
			{
				super.updateItem(account, empty);
				setText(empty || account == null ? "" :
					account.getName() + " (" + account.getAccountNumber() +
						")");
				
			}
			
		});
		
		
		this.txtTotalAmount = new TextField();
		this.txtTotalAmount.setPromptText("0.00");
		
		this.cmbPeriodicity = new ComboBox<>(
			FXCollections.observableArrayList(Periodicity.values()));
		
		this.cmbFund = new ComboBox<>();
		List<Fund> fundItemsWithNone = new ArrayList<>();
		fundItemsWithNone.add(NO_FUND_SENTINEL); // "None" option
		
		if (this.availableFunds != null)
		{
			List<Fund> sortedFunds = this.availableFunds.stream()
				.filter(Objects::nonNull)
				.sorted(
					(f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
				.collect(Collectors.toList());
			fundItemsWithNone.addAll(sortedFunds);
		}
		
		this.cmbFund
			.setItems(FXCollections.observableArrayList(fundItemsWithNone));
		this.cmbFund.setConverter(new StringConverter<Fund>()
		{
			@Override
			public String toString(Fund fund)
			{
				return (fund == null || fund.getName() == null) ? "" :
					fund.getName();
				
			}
			
			@Override
			public Fund fromString(String string)
			{
				return null;
				
			}
			
		});
		this.cmbFund.setCellFactory(listView -> new ListCell<Fund>()
		{
			@Override
			protected void updateItem(Fund fund, boolean empty)
			{
				super.updateItem(fund, empty);
				setText(empty || fund == null ? "" : fund.getName());
				
			}
			
		});
		
		this.amountErrorLabel = new Label();
		this.periodicBreakdownLabel = new Label();
		
	}
	
	/**
	 * Creates the layout.
	 *
	 * @return the grid pane
	 */
	private GridPane createLayout()
	{
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10)); // top, right, bottom,
														// left
		
		grid.add(new Label("Account:"), 0, 0);
		grid.add(this.cmbAccount, 1, 0);
		GridPane.setHgrow(this.cmbAccount, Priority.ALWAYS);
		
		grid.add(new Label("Total Budgeted Amount:"), 0, 1);
		grid.add(this.txtTotalAmount, 1, 1);
		GridPane.setHgrow(this.txtTotalAmount, Priority.ALWAYS);
		
		grid.add(this.amountErrorLabel, 1, 2);
		
		grid.add(new Label("Periodicity:"), 0, 3);
		grid.add(this.cmbPeriodicity, 1, 3);
		GridPane.setHgrow(this.cmbPeriodicity, Priority.ALWAYS);
		
		grid.add(this.periodicBreakdownLabel, 1, 4);
		
		grid.add(new Label("Line-specific Fund:"), 0, 5);
		grid.add(this.cmbFund, 1, 5);
		GridPane.setHgrow(this.cmbFund, Priority.ALWAYS);
		
		return grid;
		
	}
	
	/**
	 * Populate fields.
	 */
	private void populateFields()
	{
		
		if (this.budgetLine.getAccountId() != null &&
			this.chartOfAccounts.getAccounts() != null)
		{
			Account existing =
				this.chartOfAccounts.getAccount(this.budgetLine.getAccountId());
			
			if (existing != null)
			{
				this.cmbAccount.getSelectionModel().select(existing);
			}
			
		}
		else
		{
			this.cmbAccount.getSelectionModel().clearSelection();
		}
		
		this.txtTotalAmount
			.setText(this.budgetLine.getTotalBudgetedAmount() != null ?
				FormatUtils.formatCurrency(
					this.budgetLine.getTotalBudgetedAmount()) :
				"");
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
	
	/**
	 * Setup result converter.
	 */
	private void setupResultConverter()
	{
		setResultConverter(dialogButton -> {
			
			if (dialogButton == ButtonType.OK)
			{
				
				if (this.cmbAccount.getValue() == null)
				{
					showValidationError("Account must be selected.");
					return null;
				}
				
				BigDecimal totalAmount =
					parseAmount(this.txtTotalAmount.getText());
				
				if (totalAmount == null)
				{
					showValidationError(
						"Total Budgeted Amount cannot be empty or invalid.");
					return null;
				}
				
				if (totalAmount.compareTo(BigDecimal.ZERO) < 0)
				{
					showValidationError(
						"Total Budgeted Amount cannot be negative.");
					return null;
				}
				
				this.budgetLine.setTotalBudgetedAmount(totalAmount);
				this.budgetLine.setAccountId(
					this.cmbAccount.getValue().getAccountNumber());
				this.budgetLine
					.setAccountName(this.cmbAccount.getValue().getName());
				this.budgetLine.setPeriodicity(this.cmbPeriodicity.getValue());
				
				Fund selectedFund = this.cmbFund.getValue();
				
				if (selectedFund != null && selectedFund != NO_FUND_SENTINEL)
				{
					this.budgetLine.setFundId(selectedFund.getFundId());
				}
				else
				{
					this.budgetLine.setFundId(null);
				}
				
				return this.budgetLine;
			}
			
			return null;
		});
		
	}
	
	/**
	 * Attach listeners.
	 */
	private void attachListeners()
	{
		this.okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
		
		this.txtTotalAmount.textProperty()
			.addListener((obs, oldText, newText) -> updateAmountFeedback());
		this.txtTotalAmount.focusedProperty()
			.addListener((obs, wasFocused, focused) ->
			{
				
				if (!focused)
				{
					BigDecimal parsed =
						parseAmount(this.txtTotalAmount.getText());
					
					if (parsed != null)
					{
						this.txtTotalAmount
							.setText(FormatUtils.formatCurrency(parsed));
					}
					
				}
				
			});
			
		this.cmbPeriodicity.valueProperty()
			.addListener((obs, oldValue, newValue) -> updateAmountFeedback());
		this.cmbAccount.valueProperty()
			.addListener((obs, oldValue, newValue) -> updateAmountFeedback());
		
		updateAmountFeedback();
		
	}
	
	/**
	 * Update amount feedback.
	 */
	private void updateAmountFeedback()
	{
		BigDecimal amount = parseAmount(this.txtTotalAmount.getText());
		boolean accountSelected = this.cmbAccount.getValue() != null;
		boolean amountValid =
			amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
		
		if (amount == null)
		{
			
			if (this.txtTotalAmount.getText() == null ||
				this.txtTotalAmount.getText().trim().isEmpty())
			{
				setAmountError("Enter a total budgeted amount.");
			}
			else
			{
				setAmountError("Enter a valid monetary amount (e.g., 100.00).");
			}
			
			this.periodicBreakdownLabel.setText("");
		}
		else if (amount.compareTo(BigDecimal.ZERO) < 0)
		{
			setAmountError("Amount cannot be negative.");
			this.periodicBreakdownLabel.setText("");
			amountValid = false;
		}
		else
		{
			setAmountError(null);
			updatePeriodicBreakdown(amount);
		}
		
		if (this.okButton != null)
		{
			this.okButton.setDisable(!(amountValid && accountSelected));
		}
		
	}
	
	/**
	 * Sets the amount error.
	 *
	 * @param message the new amount error
	 */
	private void setAmountError(String message)
	{
		
		if (message == null || message.isBlank())
		{
			this.amountErrorLabel.setText("");
			this.amountErrorLabel.setTooltip(null);
		}
		else
		{
			this.amountErrorLabel.setText(message);
			this.amountErrorLabel.setTooltip(new Tooltip(message));
		}
		
	}
	
	/**
	 * Update periodic breakdown.
	 *
	 * @param total the total
	 */
	private void updatePeriodicBreakdown(BigDecimal total)
	{
		
		if (total == null)
		{
			this.periodicBreakdownLabel.setText("");
			return;
		}
		
		Periodicity periodicity = this.cmbPeriodicity.getValue();
		BigDecimal perPeriod = computePerPeriodAmount(total, periodicity);
		StringBuilder builder = new StringBuilder();
		builder.append(describePeriodicity(periodicity)).append(':').append(' ')
			.append(FormatUtils.formatCurrency(perPeriod));
		
		if (periodicity != Periodicity.ANNUAL)
		{
			builder.append(" (Total ")
				.append(FormatUtils.formatCurrency(total))
				.append(')');
		}
		
		this.periodicBreakdownLabel.setText(builder.toString());
		
	}
	
	/**
	 * Compute per period amount.
	 *
	 * @param total the total
	 * @param periodicity the periodicity
	 * @return the big decimal
	 */
	private BigDecimal computePerPeriodAmount(BigDecimal total,
		Periodicity periodicity)
	{
		
		if (total == null)
		{
			return BigDecimal.ZERO;
		}
		
		Periodicity effective =
			periodicity != null ? periodicity : Periodicity.ANNUAL;
		int periods = switch(effective)
		{
			case MONTHLY -> 12;
			case QUARTERLY -> 4;
			case ANNUAL -> 1;
		};
		
		if (periods <= 1)
		{
			return total;
		}
		
		return total.divide(BigDecimal.valueOf(periods), 2,
			RoundingMode.HALF_UP);
		
	}
	
	/**
	 * Describe periodicity.
	 *
	 * @param periodicity the periodicity
	 * @return the string
	 */
	private String describePeriodicity(Periodicity periodicity)
	{
		
		if (periodicity == null)
		{
			return "Annual total";
		}
		
		return switch(periodicity)
		{
			case ANNUAL -> "Annual total";
			case QUARTERLY -> "Quarterly amount";
			case MONTHLY -> "Monthly amount";
		};
		
	}
	
	/**
	 * Parses the amount.
	 *
	 * @param rawText the raw text
	 * @return the big decimal
	 */
	private BigDecimal parseAmount(String rawText)
	{
		return FormatUtils.parseCurrency(rawText);
		
	}
	
	/**
	 * Show validation error.
	 *
	 * @param message the message
	 */
	private void showValidationError(String message)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Validation Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		
	}
	
}
