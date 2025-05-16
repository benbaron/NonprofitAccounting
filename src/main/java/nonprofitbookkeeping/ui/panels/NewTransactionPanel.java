
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class NewTransactionPanel extends JPanel
{
	private static final long serialVersionUID = -146045487750388254L;
	
	private JTable transactionTable;
	private DefaultTableModel tableModel;
	private List<Account> accounts; // List of all defined accounts for combo box
	private JButton addRowButton, submitButton;
	private SimpleDateFormat dateFormatter;
	
	public NewTransactionPanel(List<Account> accounts2)
	{
		this.accounts = accounts2;
		this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd"); // Define date format
		setLayout(new BorderLayout());
		
		// Column names for the journal entries
		String[] columnNames =
		{ "Date", "Account", "Description", "Debit", "Credit" };
		
		// Initialize the table model
		this.tableModel = new DefaultTableModel(columnNames, 0);
		
		// Create the transaction table
		this.transactionTable = new JTable(this.tableModel);
//		this.transactionTable.getColumnModel().getColumn(0).setCellEditor(new DateCellEditor()); // Date
																									// column																								// editor
		this.transactionTable.getColumnModel().getColumn(1)
			.setCellEditor(new DefaultCellEditor(createAccountComboBox())); // Account column editor
		// The rest of the columns are strings for debit/credit/description
		
		JScrollPane scrollPane = new JScrollPane(this.transactionTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Journal Entries"));
		
		// Add button to add new rows
		this.addRowButton = new JButton("Add Entry");
		this.addRowButton.addActionListener(e -> addRow());
		
		// Submit button to process the journal entries
		this.submitButton = new JButton("Submit Transactions");
		this.submitButton.addActionListener(e -> submitTransaction());
		
		// Panel for buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(this.addRowButton);
		buttonPanel.add(this.submitButton);
		
		// Add components to the panel
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a combo box for selecting accounts.
	 *
	 * @return JComboBox for selecting accounts
	 */
	private JComboBox<String> createAccountComboBox()
	{
		String[] accountNames = this.accounts.stream()
			.map(account -> account.getAccountDetails().getAccountName())
			.toArray(String[]::new);
		
		return new JComboBox<>(accountNames);
	}
	
	/**
	 * Adds a new row to the journal table.
	 */
	private void addRow()
	{
		// Add a new row with empty values
		this.tableModel.addRow(new Object[]
		{ "", "", "", "", "" });
	}
	
	/**
	 * Submits the journal entries by creating AccountingTransactions and validating the entries.
	 */
	private void submitTransaction()
	{
		// Collect the entries from the table and create AccountingTransactions
		// Validate that the transaction is balanced
		BigDecimal totalDebit = BigDecimal.ZERO;
		BigDecimal totalCredit = BigDecimal.ZERO;
		
		// Iterate over each row to create accounting entries
		for (int i = 0; i < this.tableModel.getRowCount(); i++)
		{
			String date = (String) this.tableModel.getValueAt(i, 0);
			String accountName = (String) this.tableModel.getValueAt(i, 1);
			this.tableModel.getValueAt(i, 2);
			BigDecimal amount = new BigDecimal((String) this.tableModel.getValueAt(i, 3));
			String debitCredit = (String) this.tableModel.getValueAt(i, 4);
			
			Account account = findAccountByName(accountName); // Lookup account by name
			
			if (account == null)
			{
				JOptionPane.showMessageDialog(this, "Account not found: " + accountName, "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// Validate Date - Only the first row can have a date
			if (i == 0)
			{
				
				try
				{
					this.dateFormatter.parse(date);
				}
				catch (@SuppressWarnings("unused") ParseException ex)
				{
					JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.",
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
			}
			else
			{
				
				if (!date.isEmpty())
				{
					JOptionPane.showMessageDialog(this, "Only the first row can have a date.",
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
			}
			
			// Create the accounting entry
			AccountSide accountSide =
				debitCredit.equalsIgnoreCase("Debit") ? AccountSide.DEBIT : AccountSide.CREDIT;
			AccountingEntry entry = new AccountingEntry(amount,
				account.getAccountDetails().getAccountNumber(), accountSide);
			
			@SuppressWarnings("unused") AccountingTransaction accountingTransaction = new AccountingTransaction(account, Set.of(entry), null, System.currentTimeMillis());
			
			// Update total debits and credits for balancing
			if (accountSide == AccountSide.DEBIT)
			{
				totalDebit = totalDebit.add(amount);
			}
			else
			{
				totalCredit = totalCredit.add(amount);
			}
			
		}
		
		// Check if debits and credits match
		if (totalDebit.compareTo(totalCredit) != 0)
		{
			JOptionPane.showMessageDialog(this,
				"Transaction is unbalanced. Debits do not equal credits.", "Error",
				JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Transaction submitted successfully.", "Success",
				JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	/**
	 * Finds the account by its name.
	 *
	 * @param accountName the name of the account to find
	 * @return the Account object, or null if not found
	 */
	private Account findAccountByName(String accountName)
	{
		
		for (Account account : this.accounts)
		{
			
			if (account.getAccountDetails().getAccountName().equals(accountName))
			{
				return account;
			}
			
		}
		
		return null; // Account not found
	}
	
	

	
	// Main method for testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			// Create some dummy accounts for testing
			List<Account> accounts = List.of(new Account("Cash", "1000", AccountSide.DEBIT),
				new Account("Accounts Payable", "2000", AccountSide.CREDIT));
			JFrame frame = new JFrame("New Transaction Panel");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new NewTransactionPanel(accounts));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
