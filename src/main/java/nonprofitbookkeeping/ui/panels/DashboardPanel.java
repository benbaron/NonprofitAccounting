
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import nonprofitbookkeeping.api.AccountDetails;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CompanyDataFile;

/**
 * Displays detailed activity for a selected account.
 * It shows a filter section with dynamic account selection, 
 * date, memo, and amount filters,
 * and a table listing transactions with a running balance.
 */
public class DashboardPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -56788246492096081L;
	private JComboBox<String> accountSelector;
	private JTable transactionTable;
	private DefaultTableModel transactionModel;
	private JTextField filterDateField, filterMemoField, filterAmountField;
	
	private CompanyDataFile cdf = null;
	/**  
	 * Constructor DashboardPanel
	 * @param companyDataFile
	 */
	public DashboardPanel(CompanyDataFile companyDataFile)
	{
		this.cdf = companyDataFile;
		initComponents();
		loadData();
	}
	
	/**
	 * Initializes UI components.
	 */
	private void initComponents()
	{
		setLayout(new BorderLayout());
		
		// Top panel for account selection and filters.
		JPanel topPanel = new JPanel(new BorderLayout());
		
		// Selector panel: dynamically populate the account list from the ledger's
		// chart.
		JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		selectorPanel.setBorder(new TitledBorder("Account Selection"));
		
		// build an array of strings of account names.
		ChartOfAccounts coa = this.cdf.getLedger().getCoa();
		Collection<AccountDetails> accounts = coa.getAccountNumberToAccountDetails().values();
		Stream<String> accountNamesStream = accounts.stream()
		    .map((a) -> a.getAccountName());
		String[] accountNames = accountNamesStream.toArray(String[]::new);

		
		this.accountSelector = new JComboBox<>(accountNames);
		selectorPanel.add(new JLabel("Account:"));
		selectorPanel.add(this.accountSelector);
		
		// Filter panel: for date, memo, and amount filters.
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.setBorder(new TitledBorder("Filters"));
		this.filterDateField = new JTextField(10);
		this.filterMemoField = new JTextField(10);
		this.filterAmountField = new JTextField(10);
		
		JButton applyFilters = new JButton("Apply");
		applyFilters.addActionListener(e -> loadData());
		filterPanel.add(new JLabel("Date (yyyy-mm-dd):"));
		filterPanel.add(this.filterDateField);
		filterPanel.add(new JLabel("Memo:"));
		filterPanel.add(this.filterMemoField);
		filterPanel.add(new JLabel("Amount:"));
		filterPanel.add(this.filterAmountField);
		filterPanel.add(applyFilters);
		
		topPanel.add(selectorPanel, BorderLayout.NORTH);
		topPanel.add(filterPanel, BorderLayout.SOUTH);
		
		add(topPanel, BorderLayout.NORTH);
		
		// Transaction table panel.
		String[] columns =
		{ "Date", "Description", "Amount", "Running Balance", "Memo" };
		
		this.transactionModel = new DefaultTableModel(columns, 0);
		this.transactionTable = new JTable(this.transactionModel);
		
		JScrollPane tableScrollPane = new JScrollPane(this.transactionTable);
		tableScrollPane.setBorder(new TitledBorder("Journal Transactions"));
		add(tableScrollPane, BorderLayout.CENTER);
		
		// Bottom panel for action buttons.
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JButton reconcileButton = new JButton("Reconcile");
		reconcileButton.addActionListener(
			e -> JOptionPane.showMessageDialog(this, "Reconciliation process initiated."));
		
		JButton importButton = new JButton("Import Statement (CSV/QIF/OFX)");
		importButton.addActionListener(
			e -> JOptionPane.showMessageDialog(this,
				"Import functionality not yet implemented."));
		buttonPanel.add(reconcileButton);
		buttonPanel.add(importButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Loads transaction data for the selected account, applies filters,
	 * and populates the table with the resulting transactions and a 
	 * running balance.
	 */
	private void loadData()
	{
		// Get selected account name.
		String account = (String) this.accountSelector.getSelectedItem();
		
		if (account == null)
		{
			return;
		}
		
		// Retrieve transactions for the selected account from the service.
		List<AccountingTransaction> transactions = this.cdf.getLedger().getTransactions();
			
		
		// Clear current data.
		this.transactionModel.setRowCount(0);
		BigDecimal runningBalance = new BigDecimal(0);
		
		// Optional: Retrieve filter values.
		String filterDate = this.filterDateField.getText().trim();
		String filterMemo = this.filterMemoField.getText().trim();
		String filterAmount = this.filterAmountField.getText().trim();
		
		// Process transactions (assumed sorted chronologically).
		for (AccountingTransaction txn : transactions)
		{
			
			// Filtering: only include transactions 
			// that match the filter criteria.
			if (!filterDate.isEmpty() && !txn.getDate().contains(filterDate))
			{
				continue;
			}
			
			if (!filterMemo.isEmpty() &&
				!txn.getMemo().toLowerCase().contains(filterMemo.toLowerCase()))
			{
				continue;
			}
			
			// Assume each transaction provides a method
			// getNetAmountForAccount(String accountName)
			BigDecimal amount = txn.getNetAmountForAccount(account);
			
			// If a numeric filter is provided, attempt to parse and compare.
			if (!filterAmount.isEmpty())
			{
				
				try
				{

					if (amount.compareTo(new BigDecimal(filterAmount)) != 0)
					{
						continue;
					}
					
				}
				catch (NumberFormatException nfe)
				{
					// If unable to parse, ignore the filter.
				}
				
			}
			
			runningBalance = runningBalance.add(amount);
			this.transactionModel.addRow(new Object[]
			{
				txn.getDate(),
				txn.getDescription(),
				String.format("%.2f", amount),
				String.format("%.2f", runningBalance),
				txn.getMemo()
			});
		}
		
	}
	
}
