
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
			return account;
		}
		
		@Override public String toString()
		{
			return account.getName() + " (" + account.getAccountNumber() + ")";
		}
		
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AccountItem that = (AccountItem) o;
			return Objects.equals(account.getAccountNumber(), that.account.getAccountNumber());
		}
		
		@Override public int hashCode()
		{
			return Objects.hash(account.getAccountNumber());
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
			return fund;
		}
		
		public String getFundId()
		{
			return fund != null ? fund.getFundId() : null;
		}
		
		@Override public String toString()
		{
			return displayName;
		}
		
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			FundItem that = (FundItem) o;
			
			if (fund != null && that.fund != null)
			{
				return Objects.equals(fund.getFundId(), that.fund.getFundId());
			}
			
			return Objects.equals(displayName, that.displayName); // Compare by name if one or both
																	// funds are null (e.g. "None")
		}
		
		@Override public int hashCode()
		{
			if (fund != null)
				return Objects.hash(fund.getFundId());
			return Objects.hash(displayName);
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
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(450, 250);
		setLocationRelativeTo(owner);
	}
	
	private void initComponents()
	{
		Vector<AccountItem> accountItems = chartOfAccounts.getAccounts().stream()
			.sorted(Comparator.comparing(Account::getName))
			.map(AccountItem::new)
			.collect(Collectors.toCollection(Vector::new));
		cmbAccount = new JComboBox<>(accountItems);
		
		txtTotalAmount = new JTextField(15);
		cmbPeriodicity = new JComboBox<>(Periodicity.values());
		
		Vector<FundItem> fundItems = new Vector<>();
		fundItems.add(new FundItem("None")); // Option for no specific fund
		availableFunds.stream()
			.sorted(Comparator.comparing(Fund::getName))
			.map(FundItem::new)
			.forEach(fundItems::add);
		cmbFund = new JComboBox<>(fundItems);
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
		add(cmbAccount, gbc);
		gbc.weightx = 0; // Reset
		
		// Total Amount
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Total Budgeted Amount:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		add(txtTotalAmount, gbc);
		
		// Periodicity
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Periodicity:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		add(cmbPeriodicity, gbc);
		
		// Fund
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Line-specific Fund:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		add(cmbFund, gbc);
		
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
		
		if (budgetLine.getAccountId() != null)
		{
			Account acc = chartOfAccounts.getAccount(budgetLine.getAccountId());
			
			if (acc != null)
			{
				cmbAccount.setSelectedItem(new AccountItem(acc));
			}
			
		}
		
		txtTotalAmount.setText(budgetLine.getTotalBudgetedAmount() != null ?
			budgetLine.getTotalBudgetedAmount().toPlainString() : "");
		cmbPeriodicity.setSelectedItem(budgetLine.getPeriodicity());
		
		if (budgetLine.getFundId() != null)
		{
			availableFunds.stream()
				.filter(f -> budgetLine.getFundId().equals(f.getFundId()))
				.findFirst()
				.ifPresent(fund -> cmbFund.setSelectedItem(new FundItem(fund)));
		}
		else
		{
			cmbFund.setSelectedItem(new FundItem("None"));
		}
		
	}
	
	private void attachListeners()
	{
		// Listeners can be added if dynamic interactions are needed before OK is
		// pressed
	}
	
	private void saveAndClose()
	{
		AccountItem selectedAccountItem = (AccountItem) cmbAccount.getSelectedItem();
		
		if (selectedAccountItem == null || selectedAccountItem.getAccount() == null)
		{
			JOptionPane.showMessageDialog(this, "Please select an account.", "Input Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		Account selectedAccount = selectedAccountItem.getAccount();
		
		String amountStr = txtTotalAmount.getText().trim();
		
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
		
		budgetLine.setAccountId(selectedAccount.getAccountNumber()); // Assuming getAccountNumber()
																		// is the ID
		budgetLine.setAccountName(selectedAccount.getName()); // Store name for convenience
		budgetLine.setTotalBudgetedAmount(totalAmount);
		budgetLine.setPeriodicity((Periodicity) cmbPeriodicity.getSelectedItem());
		
		FundItem selectedFundItem = (FundItem) cmbFund.getSelectedItem();
		
		if (selectedFundItem != null && selectedFundItem.getFund() != null)
		{
			budgetLine.setFundId(selectedFundItem.getFundId());
		}
		else
		{
			budgetLine.setFundId(null); // "None" selected or no fund
		}
		
		// Note: periodicAmounts are not handled in this V1 dialog.
		// They would need additional input fields based on periodicity.
		
		saved = true;
		dispose();
	}
	
	public boolean isSaved()
	{
		return saved;
	}
	
	public BudgetLine getBudgetLine()
	{
		return budgetLine;
	}
	
}
