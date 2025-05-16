
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DonationsPanel extends JPanel
{
	private static final long serialVersionUID = 3605729443562855172L;
	
	private JTextField donorNameField;
	private JTextField donationAmountField;
	private JComboBox<String> donationTypeComboBox;
	private JSpinner donationDateSpinner;
	private JTable donationsTable;
	private DefaultTableModel tableModel;
	
	public DonationsPanel()
	{
		setLayout(new BorderLayout());
		
		// Panel for donation form
		JPanel donationFormPanel = new JPanel(new GridLayout(5, 2));
		donationFormPanel.setBorder(BorderFactory.createTitledBorder("Add New Donation"));
		
		// Donor Name
		donationFormPanel.add(new JLabel("Donor Name:"));
		this.donorNameField = new JTextField();
		donationFormPanel.add(this.donorNameField);
		
		// Donation Amount
		donationFormPanel.add(new JLabel("Donation Amount:"));
		this.donationAmountField = new JTextField();
		donationFormPanel.add(this.donationAmountField);
		
		// Donation Type (e.g., one-time, recurring)
		donationFormPanel.add(new JLabel("Donation Type:"));
		this.donationTypeComboBox = new JComboBox<>(new String[]
		{ "One-time", "Recurring" });
		donationFormPanel.add(this.donationTypeComboBox);
		
		// Donation Date
		donationFormPanel.add(new JLabel("Donation Date:"));
		this.donationDateSpinner = new JSpinner(new SpinnerDateModel());
		donationFormPanel.add(this.donationDateSpinner);
		
		// Add donation button
		JButton addDonationButton = new JButton("Add Donation");
		addDonationButton.addActionListener(this::handleAddDonation);
		donationFormPanel.add(addDonationButton);
		
		// Table to display donations
		String[] columns =
		{ "Donor Name", "Amount", "Type", "Date" };
		this.tableModel = new DefaultTableModel(columns, 0);
		this.donationsTable = new JTable(this.tableModel);
		JScrollPane tableScrollPane = new JScrollPane(this.donationsTable);
		tableScrollPane.setBorder(BorderFactory.createTitledBorder("Donations List"));
		
		// Add form and table to the panel
		add(donationFormPanel, BorderLayout.NORTH);
		add(tableScrollPane, BorderLayout.CENTER);
	}
	
	// Action handler for adding a new donation
	private void handleAddDonation(ActionEvent e)
	{
		String donorName = this.donorNameField.getText();
		String donationAmount = this.donationAmountField.getText();
		String donationType = (String) this.donationTypeComboBox.getSelectedItem();
		String donationDate = this.donationDateSpinner.getValue().toString();
		
		if (donorName.isEmpty() || donationAmount.isEmpty())
		{
			JOptionPane.showMessageDialog(this,
				"Please provide valid donor name and donation amount.");
			return;
		}
		
		// Add the new donation to the table
		this.tableModel.addRow(new Object[]
		{ donorName, donationAmount, donationType, donationDate });
		
		// Optionally, clear fields after submission
		this.donorNameField.setText("");
		this.donationAmountField.setText("");
		this.donationTypeComboBox.setSelectedIndex(0);
		this.donationDateSpinner.setValue(new java.util.Date());
	}
	
	// Main method for independent testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("DonationsPanel Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new DonationsPanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
