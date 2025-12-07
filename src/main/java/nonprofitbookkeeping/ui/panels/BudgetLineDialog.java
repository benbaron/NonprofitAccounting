
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import nonprofitbookkeeping.util.FormatUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * A Swing {@link JDialog} for creating or editing a {@link BudgetLine}.
 * It allows users to select an account, specify a total budgeted amount,
 * choose a periodicity, and optionally associate the budget line with a specific fund.
 * The dialog uses {@link AccountItem} and {@link FundItem} inner classes to wrap
 * model objects for display in JComboBoxes.
 */
public class BudgetLineDialog extends JDialog
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 1L;
	/** The {@link BudgetLine} object being created or edited by this dialog. */
	private BudgetLine budgetLine;
	/** Flag indicating whether the user saved the changes (clicked OK) or cancelled the dialog. */
	private boolean saved = false;
	
	/** JComboBox for selecting the account for this budget line. */
	private JComboBox<AccountItem> cmbAccount;
	/** JTextField for entering the total budgeted amount. */
	private JTextField txtTotalAmount;
        /** JComboBox for selecting the {@link Periodicity} of the budget line (e.g., ANNUAL, MONTHLY). */
        private JComboBox<Periodicity> cmbPeriodicity;
        /** JComboBox for selecting an optional fund specific to this budget line. Includes a "None" option. */
        private JComboBox<FundItem> cmbFund;

        /** Button used to persist the dialog; toggled by validation logic. */
        private JButton btnOk;
        /** Label that surfaces validation feedback for the amount entry field. */
        private JLabel amountErrorLabel;
        /** Label that previews how the total breaks down per period. */
        private JLabel periodicBreakdownLabel;

        /** Blank placeholder text used when validation messages are hidden. */
        private static final String EMPTY_MESSAGE = "\u00a0";
	
	/** The {@link ChartOfAccounts} used to populate the account selector. */
	private ChartOfAccounts chartOfAccounts;
	/** A list of available {@link Fund}s to populate the fund selector. */
	private List<Fund> availableFunds;
	
	/**
	 * Wrapper class for displaying {@link Account} objects in a JComboBox.
	 * Overrides {@code toString} to show account name and number.
	 * Equality is based on account number.
	 */
	private static class AccountItem
	{
		/** The wrapped Account object. */
		Account account;
		
		/**
		 * Constructs an AccountItem.
		 * @param account The {@link Account} to wrap.
		 */
		public AccountItem(Account account)
		{
			this.account = account;
		}
		
		/**
		 * Gets the wrapped {@link Account}.
		 * @return The account.
		 */
		public Account getAccount()
		{
			return this.account;
		}
		
		/**
		 * Returns a string representation for display in the JComboBox.
		 * @return Formatted string: "Account Name (AccountNumber)".
		 */
		@Override public String toString()
		{
			return this.account.getName() + " (" + this.account.getAccountNumber() + ")";
		}
		
		/**
		 * Compares this AccountItem to another object for equality based on account number.
		 * @param o The object to compare with.
		 * @return True if equal, false otherwise.
		 */
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AccountItem that = (AccountItem) o;
			return Objects.equals(this.account.getAccountNumber(), that.account.getAccountNumber());
		}
		
		/**
		 * Generates a hash code based on the account number.
		 * @return The hash code.
		 */
		@Override public int hashCode()
		{
			return Objects.hash(this.account.getAccountNumber());
		}
		
	}
	
	/**
	 * Wrapper class for displaying {@link Fund} objects in a JComboBox.
	 * Includes a special constructor for a "None" option.
	 * Overrides {@code toString} for display. Equality is based on fund ID or display name for "None".
	 */
	private static class FundItem
	{
		/** The wrapped Fund object; null for the "None" item. */
		Fund fund;
		/** The display name, used for "None" or derived from fund name. */
		String displayName;
		
		/**
		 * Constructs a FundItem wrapping a {@link Fund}.
		 * @param fund The {@link Fund} to wrap.
		 */
		public FundItem(Fund fund)
		{
			this.fund = fund;
			this.displayName = fund.getName();
		}
		
		/**
		 * Constructs a FundItem for a special display name (e.g., "None").
		 * The underlying fund will be null.
		 * @param displayName The string to display (e.g., "None").
		 */
		public FundItem(String displayName)
		{
			this.displayName = displayName;
			this.fund = null;
		}
		
		/**
		 * Gets the wrapped {@link Fund}.
		 * @return The fund, or null if this item represents an option like "None".
		 */
		public Fund getFund()
		{
			return this.fund;
		}
		
		/**
		 * Gets the ID of the wrapped fund.
		 * @return The fund ID, or null if there is no underlying fund.
		 */
		public String getFundId()
		{
			return this.fund != null ? this.fund.getFundId() : null;
		}
		
		/**
		 * Returns the display name of this item.
		 * @return The display name.
		 */
		@Override public String toString()
		{
			return this.displayName;
		}
		
		/**
		 * Compares this FundItem to another object for equality.
		 * If both items wrap actual funds, comparison is by fund ID.
		 * Otherwise, comparison is by display name (to handle "None" item correctly).
		 * @param o The object to compare with.
		 * @return True if equal, false otherwise.
		 */
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			FundItem that = (FundItem) o;
			
			if (this.fund != null && that.fund != null)
			{
				return Objects.equals(this.fund.getFundId(), that.fund.getFundId());
			}
			
			// Compare by displayName if one or both funds are null (e.g., "None" item vs.
			// another "None" or a real fund)
			return Objects.equals(this.displayName, that.displayName);
		}
		
		/**
		 * Generates a hash code based on the fund ID if available, otherwise on the display name.
		 * @return The hash code.
		 */
		@Override public int hashCode()
		{
			if (this.fund != null)
				return Objects.hash(this.fund.getFundId());
			return Objects.hash(this.displayName);
		}
		
	}
	
	/**
	 * Constructs a {@code BudgetLineDialog}.
	 *
	 * @param owner The parent {@link Dialog} that owns this dialog.
	 * @param title The title of the dialog window.
	 * @param coa The {@link ChartOfAccounts} used to populate the account selection ComboBox.
	 * @param funds A list of available {@link Fund}s to populate the fund selection ComboBox. Can be null or empty.
	 * @param existingLine The {@link BudgetLine} to edit. If null, the dialog is configured for creating a new budget line.
	 */
	public BudgetLineDialog(Dialog owner, String title, ChartOfAccounts coa, List<Fund> funds,
		BudgetLine existingLine)
	{
		super(owner, title, true); // Modal dialog
		this.chartOfAccounts = Objects.requireNonNull(coa, "ChartOfAccounts cannot be null.");
		this.availableFunds = funds != null ? funds : List.of(); // Ensure non-null list
		this.budgetLine = (existingLine != null) ? existingLine : new BudgetLine(); // Use existing
																					// or create new
		
		// Ensure a new budget line has a default periodicity if not set
		if (this.budgetLine.getPeriodicity() == null)
		{
			this.budgetLine.setPeriodicity(Periodicity.ANNUAL);
		}
		
		initComponents();
		layoutComponents();
		populateFields();
		attachListeners();
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(450, 250); // Adjusted size
		setLocationRelativeTo(owner);
	}
	
	/**
	 * Initializes the UI components of the dialog, such as JComboBoxes and JTextFields.
	 * Populates account and fund selectors.
	 */
	private void initComponents()
	{
		// Populate Account ComboBox
		Vector<AccountItem> accountItems =
			this.chartOfAccounts.getAccounts().stream().filter(Objects::nonNull) // Filter out null
																					// accounts
				.sorted(Comparator.comparing(Account::getName, String.CASE_INSENSITIVE_ORDER)) // Sort
																								// by
																								// name
				.map(AccountItem::new).collect(Collectors.toCollection(Vector::new));
		this.cmbAccount = new JComboBox<>(accountItems);
		
		this.txtTotalAmount = new JTextField(15);
		this.cmbPeriodicity = new JComboBox<>(Periodicity.values()); // Populate with all enum
																		// values
		
		// Populate Fund ComboBox
		Vector<FundItem> fundItems = new Vector<>();
		fundItems.add(new FundItem("None")); // Add "None" option for no specific fund
		this.availableFunds.stream().filter(Objects::nonNull) // Filter out null funds
			.sorted(Comparator.comparing(Fund::getName, String.CASE_INSENSITIVE_ORDER)) // Sort by
																						// name
			.map(FundItem::new).forEach(fundItems::add);
                this.cmbFund = new JComboBox<>(fundItems);

                this.amountErrorLabel = new JLabel(EMPTY_MESSAGE);
                this.amountErrorLabel.setForeground(Color.RED);
                this.periodicBreakdownLabel = new JLabel(EMPTY_MESSAGE);
                Font baseFont = this.periodicBreakdownLabel.getFont();
                this.periodicBreakdownLabel
                        .setFont(baseFont.deriveFont(Math.max(10f, baseFont.getSize2D() - 1f)));
        }
	
	/**
	 * Arranges the UI components on the dialog panel using {@link GridBagLayout}.
	 * This includes labels, input fields (JComboBoxes, JTextField), and action buttons (OK, Cancel).
	 */
        private void layoutComponents()
        {
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5); // Padding around components
                gbc.fill = GridBagConstraints.HORIZONTAL; // Components expand horizontally

                int row = 0;

                // Account
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.EAST;
                add(new JLabel("Account:"), gbc);
                gbc.gridx = 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0;
                add(this.cmbAccount, gbc);
                gbc.weightx = 0; // Reset

                row++;

                // Total Amount
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.EAST;
                add(new JLabel("Total Budgeted Amount:"), gbc);
                gbc.gridx = 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                add(this.txtTotalAmount, gbc);

                row++;

                // Amount validation message
                gbc.gridx = 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                add(this.amountErrorLabel, gbc);

                row++;

                // Periodicity
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.EAST;
                add(new JLabel("Periodicity:"), gbc);
                gbc.gridx = 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                add(this.cmbPeriodicity, gbc);

                row++;

                // Per-period preview
                gbc.gridx = 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                add(this.periodicBreakdownLabel, gbc);

                row++;

                // Fund
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.EAST;
                add(new JLabel("Line-specific Fund:"), gbc);
                gbc.gridx = 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                add(this.cmbFund, gbc);

                row++;

                // Buttons
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                this.btnOk = new JButton("OK");
                JButton btnCancel = new JButton("Cancel");
                buttonPanel.add(this.btnOk);
                buttonPanel.add(btnCancel);

                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 2;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.fill = GridBagConstraints.NONE;
                add(buttonPanel, gbc);

                // Action Listeners for buttons
                this.btnOk.addActionListener(e -> saveAndClose());
                btnCancel.addActionListener(e -> dispose()); // Close dialog without saving
        }
	
	/**
	 * Populates the dialog's input fields with data from the current {@link #budgetLine} object.
	 * This is used when editing an existing budget line or to set initial default values.
	 */
	private void populateFields()
	{
		
		if (this.budgetLine.getAccountId() != null)
		{
			// Find the Account object corresponding to the accountId and select it
			Account acc = this.chartOfAccounts.getAccount(this.budgetLine.getAccountId());
			
			if (acc != null)
			{
				// AccountItem's equals/hashCode must be correctly implemented for this to work
				this.cmbAccount.setSelectedItem(new AccountItem(acc));
			}
			else
			{
				this.cmbAccount.setSelectedIndex(-1); // No account selected or account not found
			}
			
		}
		else
		{
			this.cmbAccount.setSelectedIndex(-1); // No account pre-selected
		}
		
               this.txtTotalAmount.setText(this.budgetLine.getTotalBudgetedAmount() != null ?
                        FormatUtils.formatCurrency(this.budgetLine.getTotalBudgetedAmount()) : ""); // Handle null amount
		this.cmbPeriodicity.setSelectedItem(this.budgetLine.getPeriodicity()); // Handles null by
																				// selecting first
																				// if not found
		
		// Select the fund in ComboBox
		if (this.budgetLine.getFundId() != null)
		{
			this.availableFunds.stream()
				.filter(f -> this.budgetLine.getFundId().equals(f.getFundId())).findFirst()
				.ifPresentOrElse(fund -> this.cmbFund.setSelectedItem(new FundItem(fund)),
					() -> this.cmbFund.setSelectedItem(new FundItem("None")) // Fallback if fund ID
																				// not in list
				);
		}
		else
		{
			this.cmbFund.setSelectedItem(new FundItem("None")); // Default to "None"
		}
		
	}
	
        /**
         * Attaches runtime listeners that keep the dialog responsive. The listeners validate
         * the total amount, update the per-period preview, and toggle the OK button so users
         * can only persist valid budget lines.
         */
        private void attachListeners()
        {
                this.txtTotalAmount.getDocument().addDocumentListener(new DocumentListener()
                {
                        @Override public void insertUpdate(DocumentEvent e)
                        {
                                updateAmountFeedback();
                        }

                        @Override public void removeUpdate(DocumentEvent e)
                        {
                                updateAmountFeedback();
                        }

                        @Override public void changedUpdate(DocumentEvent e)
                        {
                                updateAmountFeedback();
                        }
                });

                this.txtTotalAmount.addFocusListener(new FocusAdapter()
                {
                        @Override public void focusLost(FocusEvent e)
                        {
                                BigDecimal parsed = parseAmount(BudgetLineDialog.this.txtTotalAmount.getText());

                                if (parsed != null)
                                {
                                        BudgetLineDialog.this.txtTotalAmount
                                                .setText(FormatUtils.formatCurrency(parsed));
                                }
                        }
                });

                this.cmbPeriodicity.addItemListener(event ->
                {
                        if (event.getStateChange() == ItemEvent.SELECTED)
                        {
                                updateAmountFeedback();
                        }
                });

                this.cmbAccount.addActionListener(e -> updateAmountFeedback());

                updateAmountFeedback();
        }

        private void updateAmountFeedback()
        {
                BigDecimal amount = parseAmount(this.txtTotalAmount.getText());
                boolean amountValid = amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
                boolean accountSelected = this.cmbAccount.getSelectedItem() != null;

                if (amount == null)
                {
                        if (this.txtTotalAmount.getText() == null || this.txtTotalAmount.getText().trim().isEmpty())
                        {
                                setAmountError("Enter a total budgeted amount.");
                        }
                        else
                        {
                                setAmountError("Enter a valid monetary amount (e.g., 100.00).");
                        }
                        this.periodicBreakdownLabel.setText(EMPTY_MESSAGE);
                }
                else if (amount.compareTo(BigDecimal.ZERO) < 0)
                {
                        setAmountError("Amount cannot be negative.");
                        this.periodicBreakdownLabel.setText(EMPTY_MESSAGE);
                        amountValid = false;
                }
                else
                {
                        setAmountError(null);
                        updatePeriodicBreakdown(amount);
                }

                if (this.btnOk != null)
                {
                        this.btnOk.setEnabled(amountValid && accountSelected);
                }
        }

        private void setAmountError(String message)
        {
                String text = (message == null || message.isBlank()) ? EMPTY_MESSAGE : message;
                this.amountErrorLabel.setText(text);
                this.amountErrorLabel.setToolTipText(text.equals(EMPTY_MESSAGE) ? null : message);
        }

        private void updatePeriodicBreakdown(BigDecimal total)
        {
                if (total == null)
                {
                        this.periodicBreakdownLabel.setText(EMPTY_MESSAGE);
                        return;
                }

                Periodicity periodicity = (Periodicity) this.cmbPeriodicity.getSelectedItem();
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

        private BigDecimal computePerPeriodAmount(BigDecimal total, Periodicity periodicity)
        {
                if (total == null)
                {
                        return BigDecimal.ZERO;
                }

                int periods = switch (periodicity != null ? periodicity : Periodicity.ANNUAL)
                {
                        case MONTHLY -> 12;
                        case QUARTERLY -> 4;
                        case ANNUAL -> 1;
                };

                if (periods <= 1)
                {
                        return total;
                }

                return total.divide(BigDecimal.valueOf(periods), 2, RoundingMode.HALF_UP);
        }

        private String describePeriodicity(Periodicity periodicity)
        {
                if (periodicity == null)
                {
                        return "Annual total";
                }

                return switch (periodicity)
                {
                        case ANNUAL -> "Annual total";
                        case QUARTERLY -> "Quarterly amount";
                        case MONTHLY -> "Monthly amount";
                };
        }

        private BigDecimal parseAmount(String rawText)
        {
                return FormatUtils.parseCurrency(rawText);
        }
	
	/**
	 * Validates the input fields, saves the data from the dialog fields into the
	 * {@link #budgetLine} object, sets the {@code saved} flag to true, and closes the dialog.
	 * Displays error messages using {@link JOptionPane} if validation fails for account selection
	 * or total amount (empty, non-numeric, or negative).
	 */
	private void saveAndClose()
	{
		AccountItem selectedAccountItem = (AccountItem) this.cmbAccount.getSelectedItem();
		
		if (selectedAccountItem == null || selectedAccountItem.getAccount() == null)
		{
			JOptionPane.showMessageDialog(this, "Please select an account.", "Input Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		Account selectedAccount = selectedAccountItem.getAccount();
		
                String rawAmount = this.txtTotalAmount.getText();
                BigDecimal totalAmount = parseAmount(rawAmount);

                if (totalAmount == null)
                {
                        String message = (rawAmount == null || rawAmount.trim().isEmpty())
                                ? "Total Budgeted Amount cannot be empty."
                                : "Please enter a valid Total Budgeted Amount.";
                        JOptionPane.showMessageDialog(this, message,
                                "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }

                if (totalAmount.compareTo(BigDecimal.ZERO) < 0)
                {
                        JOptionPane.showMessageDialog(this, "Total Budgeted Amount cannot be negative.",
                                "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }

                this.budgetLine.setAccountId(selectedAccount.getAccountNumber()); // Assuming
																			// getAccountNumber()
																			// is the ID
		this.budgetLine.setAccountName(selectedAccount.getName()); // Store name for convenience
		this.budgetLine.setTotalBudgetedAmount(totalAmount);
		this.budgetLine.setPeriodicity((Periodicity) this.cmbPeriodicity.getSelectedItem());
		
		FundItem selectedFundItem = (FundItem) this.cmbFund.getSelectedItem();
		
		if (selectedFundItem != null && selectedFundItem.getFund() != null)
		{
			this.budgetLine.setFundId(selectedFundItem.getFundId());
		}
		else
		{
			this.budgetLine.setFundId(null); // "None" selected or no fund
		}
		
		// Note: periodicAmounts are not handled in this V1 dialog.
		// They would need additional input fields based on periodicity.
		
		this.saved = true;
		dispose(); // Close the dialog
	}
	
	/**
	 * Checks if the dialog was saved (i.e., the "OK" button was clicked and data was successfully saved).
	 *
	 * @return {@code true} if the dialog data was saved, {@code false} otherwise (e.g., if cancelled).
	 */
	public boolean isSaved()
	{
		return this.saved;
	}
	
	/**
	 * Gets the {@link BudgetLine} object that was created or edited by this dialog.
	 * This object contains the data entered by the user if the dialog was saved.
	 *
	 * @return The {@link BudgetLine} instance.
	 */
	public BudgetLine getBudgetLine()
	{
		return this.budgetLine;
	}
	
}
