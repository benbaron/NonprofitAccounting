
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Ledger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel that displays account transactions and provides features for filtering, reconciliation, and importing statements.
 */
public class AccountsActivityPanel extends JPanel
{
	private static final long serialVersionUID = 4215883159897410073L;
	
	private JComboBox<String> accountSelector;
	private JTable transactionTable;
	private DefaultTableModel transactionModel;
	private JTextField filterDateField, filterMemoField, filterAmountField;
	
	// Real data for accounts and transactions
	private List<String> accountNames; // This should be populated dynamically, e.g., from a
										// database
	private List<AccountingTransaction> transactions; // This should hold the real transactions
	/**
	 * Constructor for AccountsActivityPanel
	 * 
	 * @param ledger the ledger containing account data and transactions
	 */
	public AccountsActivityPanel(Ledger ledger)
	{
		this.accountNames = Ledger.getAccountNames();
		this.transactions = ledger.getTransactions();
		
		setLayout(new BorderLayout());
		
		// Account selector and filters
		JPanel topPanel = new JPanel(new BorderLayout());
		
		JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		selectorPanel.setBorder(new TitledBorder("Account Selection"));
		this.accountSelector = new JComboBox<>(this.accountNames.toArray(new String[0]));
		selectorPanel.add(new JLabel("Account:"));
		selectorPanel.add(this.accountSelector);
		
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.setBorder(new TitledBorder("Filters"));
		this.filterDateField = new JTextField(10);
		this.filterMemoField = new JTextField(10);
		this.filterAmountField = new JTextField(10);
		
		filterPanel.add(new JLabel("Date:"));
		filterPanel.add(this.filterDateField);
		filterPanel.add(new JLabel("Memo:"));
		filterPanel.add(this.filterMemoField);
		filterPanel.add(new JLabel("Amount:"));
		filterPanel.add(this.filterAmountField);
		JButton applyFilters = new JButton("Apply");
		
		// Action listener for applyFilters
		applyFilters.addActionListener(e -> applyFilters());
		filterPanel.add(applyFilters);
		
		topPanel.add(selectorPanel, BorderLayout.NORTH);
		topPanel.add(filterPanel, BorderLayout.SOUTH);
		
		// Transaction table
		String[] cols =
		{ "Date", "Description", "Amount", "Balance", "Memo" };
		this.transactionModel = new DefaultTableModel(cols, 0);
		this.transactionTable = new JTable(this.transactionModel);
		JScrollPane tableScrollPane = new JScrollPane(this.transactionTable);
		tableScrollPane.setBorder(new TitledBorder("Ledger"));
		
		// Populate table with the real data for the selected account
		updateTransactionTable();
		
		// Control buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton reconcileButton = new JButton("Reconcile");
		JButton importStatementButton = new JButton("Import Statement (CSV/QIF/OFX)");
		
		reconcileButton.addActionListener(e -> {
			JOptionPane.showMessageDialog(this,
				"Reconciliation process would start here.");
		});
		
		importStatementButton.addActionListener(e -> {
			JOptionPane.showMessageDialog(this,
				"Import dialog for CSV/QIF/OFX not implemented.");
		});
		
		buttonPanel.add(reconcileButton);
		buttonPanel.add(importStatementButton);
		
		// Assemble all sections
		add(topPanel, BorderLayout.NORTH);
		add(tableScrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Updates the transaction table based on the selected account and filters.
	 */
	private void updateTransactionTable()
	{
		// Clear existing rows
		this.transactionModel.setRowCount(0);
		
		// Populate table with filtered transactions
		for (AccountingTransaction t : this.transactions)
		{
			
			// Only show transactions for the selected account (and optionally, filter by
			// date, memo, or amount)
			if (t.getAccountName().equals(this.accountSelector.getSelectedItem()))
			{
				
				// Apply filters (date, memo, amount)
				if (matchesFilters(t))
				{
					this.transactionModel.addRow(new Object[]
					{
						t.getDate(),
						t.getDescription(),
						t.getTotalAmount(), // BigDecimal value
						t.getAccountBalance(),
						t.getMemo()
					});
				}
				
			}
			
		}
		
	}
	
	/**
	 * Applies filters to transactions when the "Apply" button is clicked.
	 */
	private void applyFilters()
	{
		// This method is called when the "Apply" button is clicked
		updateTransactionTable();
	}
	
	/**
	 * Checks if a transaction matches the current filter criteria.
	 * 
	 * @param transaction the accounting transaction to check
	 * @return true if the transaction matches all filters, false otherwise
	 */
	private boolean matchesFilters(AccountingTransaction transaction)
	{
		boolean matches = true;
		
		// Date filter
		String dateFilter = this.filterDateField.getText().trim();
		
		if (!dateFilter.isEmpty() && !transaction.getDate().contains(dateFilter))
		{
			matches = false;
		}
		
		// Memo filter
		String memoFilter = this.filterMemoField.getText().trim();
		
		if (!memoFilter.isEmpty() && !transaction.getMemo().contains(memoFilter))
		{
			matches = false;
		}
		
		// Amount filter
		String amountFilter = this.filterAmountField.getText().trim();
		
		if (!amountFilter.isEmpty())
		{
			
			try
			{
				BigDecimal filterAmount = new BigDecimal(amountFilter);
				
				if (transaction.getTotalAmount().compareTo(filterAmount) != 0)
				{ // Compare using BigDecimal's compareTo
					matches = false;
				}
				
			}
			catch (NumberFormatException e)
			{
				matches = false;
			}
			
		}
		
		return matches;
	}
	
}
