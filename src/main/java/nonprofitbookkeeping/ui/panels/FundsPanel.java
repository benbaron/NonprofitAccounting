
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.service.FundAccountingService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class FundsPanel extends JPanel
{
	
	private static final long serialVersionUID = -4156921380101699525L;
	private final FundAccountingService service;
	private JTable fundTable;
	private DefaultTableModel fundModel;
	
	public FundsPanel(FundAccountingService service)
	{
		this.service = service;
		buildUI();
		refresh();
	}
	
	private void buildUI()
	{
		setLayout(new BorderLayout());
		
		// Panel for fund transfer
		JPanel transferPanel = new JPanel(new FlowLayout());
		transferPanel.setBorder(new TitledBorder("Transfer Funds"));
		
		JTextField fromField = new JTextField(10);
		JTextField toField = new JTextField(10);
		JTextField amountField = new JTextField(6);
		JButton transferBtn = new JButton("Transfer");
		
		// Add action listener to the transfer button
		transferBtn.addActionListener(e -> {
			
			try
			{
				String from = fromField.getText().trim();
				String to = toField.getText().trim();
				BigDecimal amount = new BigDecimal(amountField.getText().trim());
				
				if (amount.compareTo(BigDecimal.ZERO) <= 0)
				{
					JOptionPane.showMessageDialog(this, "Amount must be greater than zero.",
						"Invalid Amount", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// Transfer the funds using the service
				this.service.transferFunds(from, to, amount);
				
				JOptionPane.showMessageDialog(this, "Transfer Complete.");
				refresh(); // Refresh the table after the transfer
				
				// Clear fields
				fromField.setText("");
				toField.setText("");
				amountField.setText("");
				
			}
			catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(this, "Please enter a valid number for the amount.",
					"Invalid Amount", JOptionPane.ERROR_MESSAGE);
			}
			catch (IllegalArgumentException ex)
			{
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Transfer Error",
					JOptionPane.ERROR_MESSAGE);
			}
			
		});
		
		// Add components to the transfer panel
		transferPanel.add(new JLabel("From Fund:"));
		transferPanel.add(fromField);
		transferPanel.add(new JLabel("To Fund:"));
		transferPanel.add(toField);
		transferPanel.add(new JLabel("Amount:"));
		transferPanel.add(amountField);
		transferPanel.add(transferBtn);
		
		// Add transfer panel to the top of the main panel
		add(transferPanel, BorderLayout.NORTH);
		
		// Panel for adding/removing funds
		JPanel fundControlPanel = new JPanel(new FlowLayout());
		fundControlPanel.setBorder(new TitledBorder("Fund Management"));
		
		JButton addFundBtn = new JButton("Add Fund");
		JButton deleteFundBtn = new JButton("Delete Fund");
		
		// Add action listeners for adding/removing funds
		addFundBtn.addActionListener(e -> {
			String fundName = JOptionPane.showInputDialog(this, "Enter Fund Name:");
			
			if (fundName != null && !fundName.trim().isEmpty())
			{
				String balanceInput = JOptionPane.showInputDialog(this, "Enter Fund Balance:");
				
				try
				{
					BigDecimal balance = new BigDecimal(balanceInput);
					Fund fund = new Fund(fundName);
					fund.setBalance(balance); // Set the initial balance
					this.service.addFund(fund);
					JOptionPane.showMessageDialog(this, "Fund added successfully.");
					refresh(); // Refresh the table after adding the fund
				}
				catch (NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(this, "Please enter a valid balance.",
						"Invalid Balance", JOptionPane.ERROR_MESSAGE);
				}
				
			}
			
		});
		
		deleteFundBtn.addActionListener(e -> {
			String fundName = JOptionPane.showInputDialog(this, "Enter Fund Name to delete:");
			
			if (fundName != null && !fundName.trim().isEmpty())
			{
				boolean removed = this.service.removeFund(fundName);
				
				if (removed)
				{
					JOptionPane.showMessageDialog(this, "Fund deleted successfully.");
					refresh(); // Refresh the table after deleting the fund
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Fund not found.", "Error",
						JOptionPane.ERROR_MESSAGE);
				}
				
			}
			
		});
		
		// Add buttons to the fund control panel
		fundControlPanel.add(addFundBtn);
		fundControlPanel.add(deleteFundBtn);
		add(fundControlPanel, BorderLayout.SOUTH);
		
		// Table to display fund balances
		String[] cols =
		{ "Fund", "Balance" };
		this.fundModel = new DefaultTableModel(cols, 0);
		this.fundTable = new JTable(this.fundModel);
		
		// Add the table with scroll pane
		add(new JScrollPane(this.fundTable), BorderLayout.CENTER);
	}
	
	private void refresh()
	{
		// Clear the existing rows in the table
		this.fundModel.setRowCount(0);
		
		// Get the updated fund balances from the service
		List<Fund> funds = this.service.listFunds();
		
		// Populate the table with current balances
		for (Fund fund : funds)
		{
			this.fundModel.addRow(new Object[]
			{ fund.getName(), fund.getBalance() });
		}
		
	}
	
	// Main method for independent testing
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			// Create a sample FundAccountingService
			FundAccountingService fundService = new FundAccountingService();
			JFrame frame = new JFrame("FundsPanel Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new FundsPanel(fundService));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
