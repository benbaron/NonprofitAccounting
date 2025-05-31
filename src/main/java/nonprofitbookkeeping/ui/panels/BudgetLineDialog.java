
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class BudgetLineDialog extends JDialog
{
	private BudgetLine budgetLine;
	private boolean saved = false;
	
	private JComboBox<AccountItem> cmbAccount;
	private JTextField txtTotalAmount;
	private JComboBox<Periodicity> cmbPeriodicity;
	private JComboBox<FundItem> cmbFund; // Optional line-specific fund
	
	private ChartOfAccounts chartOfAccounts;
	private List<Fund> availableFunds;
	
	// Wrapper class for JComboBox display
	private static class AccountItem
	{
		Account account;
		
		public AccountItem(Account account)
		{
			this.account = account;
		}
		
		public Account getAccount()
		{
			return this.account;
		}
		
		@Override public String toString()
		{
			return this.account.getName() + " (" + this.account.getAccountNumber() + ")";
		}
		
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AccountItem that = (AccountItem) o;
			return Objects.equals(this.account.getAccountNumber(), that.account.getAccountNumber());
		}
		
		@Override public int hashCode()
		{
			return Objects.hash(this.account.getAccountNumber());
		}
		
	}
	
	private static class FundItem
	{
		Fund fund;
		String displayName; // For "None" option
		
		public FundItem(Fund fund)
		{
			this.fund = fund;
			this.displayName = fund.getName();
		}
		
		public FundItem(String displayName)
		{
			this.displayName = displayName;
			this.fund = null;
		} // For "None"
		
		public Fund getFund()
		{
			return this.fund;
		}
		
		public String getFundId()
		{
			return this.fund != null ? this.fund.getFundId() : null;
		}
		
		@Override public String toString()
		{
			return this.displayName;
		}
		
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
			
			return Objects.equals(this.displayName, that.displayName); // Compare by name if one or both
																	// funds are null (e.g. "None")
		}
		
		@Override public int hashCode()
		{
			if (this.fund != null)
				return Objects.hash(this.fund.getFundId());
			return Objects.hash(this.displayName);
		}
		
	}
	
	
	public BudgetLineDialog(Dialog owner, String title, ChartOfAccounts coa, List<Fund> funds,
		BudgetLine existingLine)
	{
		super(owner, title, true);
		this.chartOfAccounts = coa;
		this.availableFunds = funds != null ? funds : List.of();
		this.budgetLine = (existingLine != null) ? existingLine : new BudgetLine();
		
		if (this.budgetLine.getPeriodicity() == null)
		{ // Ensure default if new line
			this.budgetLine.setPeriodicity(Periodicity.ANNUAL);
		}
		
		initComponents();
		layoutComponents();
		populateFields();
		attachListeners();
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(450, 250);
		setLocationRelativeTo(owner);
	}
	
	private void initComponents()
	{
		Vector<AccountItem> accountItems = this.chartOfAccounts.getAccounts().stream()
			.sorted(Comparator.comparing(Account::getName))
			.map(AccountItem::new)
			.collect(Collectors.toCollection(Vector::new));
		this.cmbAccount = new JComboBox<>(accountItems);
		
		this.txtTotalAmount = new JTextField(15);
		this.cmbPeriodicity = new JComboBox<>(Periodicity.values());
		
		Vector<FundItem> fundItems = new Vector<>();
		fundItems.add(new FundItem("None")); // Option for no specific fund
		this.availableFunds.stream()
			.sorted(Comparator.comparing(Fund::getName))
			.map(FundItem::new)
			.forEach(fundItems::add);
		this.cmbFund = new JComboBox<>(fundItems);
	}
	
	private void layoutComponents()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		// Account
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Account:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		add(this.cmbAccount, gbc);
		gbc.weightx = 0; // Reset
		
		// Total Amount
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Total Budgeted Amount:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		add(this.txtTotalAmount, gbc);
		
		// Periodicity
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Periodicity:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		add(this.cmbPeriodicity, gbc);
		
		// Fund
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Line-specific Fund:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		add(this.cmbFund, gbc);
		
		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnOK = new JButton("OK");
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnOK);
		buttonPanel.add(btnCancel);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		add(buttonPanel, gbc);
		
		// Action Listeners for buttons
		btnOK.addActionListener(e -> saveAndClose());
		btnCancel.addActionListener(e -> dispose());
	}
	
	private void populateFields()
	{
		
		if (this.budgetLine.getAccountId() != null)
		{
			Account acc = this.chartOfAccounts.getAccount(this.budgetLine.getAccountId());
			
			if (acc != null)
			{
				this.cmbAccount.setSelectedItem(new AccountItem(acc));
			}
			
		}
		
		this.txtTotalAmount.setText(this.budgetLine.getTotalBudgetedAmount() != null ?
			this.budgetLine.getTotalBudgetedAmount().toPlainString() : "");
		this.cmbPeriodicity.setSelectedItem(this.budgetLine.getPeriodicity());
		
		if (this.budgetLine.getFundId() != null)
		{
			this.availableFunds.stream()
				.filter(f -> this.budgetLine.getFundId().equals(f.getFundId()))
				.findFirst()
				.ifPresent(fund -> this.cmbFund.setSelectedItem(new FundItem(fund)));
		}
		else
		{
			this.cmbFund.setSelectedItem(new FundItem("None"));
		}
		
	}
	
	private void attachListeners()
	{
		// Listeners can be added if dynamic interactions are needed before OK is
		// pressed
	}
	
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
		
		String amountStr = this.txtTotalAmount.getText().trim();
		
		if (amountStr.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Total Budgeted Amount cannot be empty.",
				"Input Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		BigDecimal totalAmount;
		
		try
		{
			totalAmount = new BigDecimal(amountStr);
			
			if (totalAmount.compareTo(BigDecimal.ZERO) < 0)
			{
				JOptionPane.showMessageDialog(this, "Total Budgeted Amount cannot be negative.",
					"Input Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		}
		catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Invalid number format for Total Budgeted Amount.",
				"Input Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		this.budgetLine.setAccountId(selectedAccount.getAccountNumber()); // Assuming getAccountNumber()
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
		dispose();
	}
	
	public boolean isSaved()
	{
		return this.saved;
	}
	
	public BudgetLine getBudgetLine()
	{
		return this.budgetLine;
	}
	
}
