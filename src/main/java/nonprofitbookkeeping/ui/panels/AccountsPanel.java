
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.service.AccountService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AccountsPanel extends JPanel
{
	private static final long serialVersionUID = -4961663219408912807L;
	private JTable accountTable;
	private DefaultTableModel tableModel;
	public AccountsPanel(AccountService accountService)
	{
		setLayout(new BorderLayout());
		
		// Table columns
		String[] columns =
		{
			"Account Code", "Account Name", "Type", "Parent Account", "Currency", "Opening Balance"
		};
		
		// Fetch real account data from the service
		List<Account> accounts = AccountService.getAllAccounts();
		
		// Convert account data to a format suitable for the table (Object[][])
		Object[][] data = new Object[accounts.size()][columns.length];
		
		for (int i = 0; i < accounts.size(); i++)
		{
			Account account = accounts.get(i);
			data[i][0] = account.getAccountDetails().getAccountCode();
			data[i][1] = account.getAccountDetails().getAccountName();
			data[i][2] = account.getAccountDetails().getAccountType();
			data[i][3] = account.getAccountDetails().getParentAccount();
			data[i][4] = account.getAccountDetails().getCurrency();
			data[i][5] = account.getAccountDetails().getOpeningBalance();
		}
		
		// Create the table model with real data
		this.tableModel = new DefaultTableModel(data, columns);
		this.accountTable = new JTable(this.tableModel);
		JScrollPane scrollPane = new JScrollPane(this.accountTable);
		scrollPane.setBorder(new TitledBorder("Chart of Accounts"));
		
		// Control panel
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JButton addBtn = new JButton("Add Account");
		JButton editBtn = new JButton("Edit Account");
		JButton deleteBtn = new JButton("Delete Account");
		
		controlPanel.add(addBtn);
		controlPanel.add(editBtn);
		controlPanel.add(deleteBtn);
		
		// Button actions
		addBtn.addActionListener(e -> {
			this.tableModel.addRow(new Object[]
			{ "", "", "Asset", "", "USD", "0.00" });
		});
		
		editBtn.addActionListener(e -> {
			int row = this.accountTable.getSelectedRow();
			
			if (row == -1)
			{
				JOptionPane.showMessageDialog(this, "Please select an account to edit.");
			}
			else
			{
				this.accountTable.editCellAt(row, 0);
			}
			
		});
		
		deleteBtn.addActionListener(e -> {
			int row = this.accountTable.getSelectedRow();
			
			if (row != -1)
			{
				this.tableModel.removeRow(row);
			}
			
		});
		
		add(scrollPane, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
	}
	
	// Main method for independent testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			// Create AccountService and pass it to the panel
			AccountService accountService = new AccountService(); // Assuming AccountService is
																	// implemented
			JFrame frame = new JFrame("AccountsPanel Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new AccountsPanel(accountService));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
