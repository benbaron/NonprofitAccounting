
package nonprofitbookkeeping.ui.panels;


import nonprofitbookkeeping.model.Donor;
import nonprofitbookkeeping.service.DonorService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * DonorsPanel displays a list of donors along with donation information.
 * <p>
 * It retrieves donor data from a DonorService implementation (FileBasedDonorService in this example)
 * and displays it in a table. The panel also allows refreshing the data.
 * </p>
 */
public class DonorsPanel extends JPanel
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -1263915002038344598L;
	private JTable donorTable;
	private DefaultTableModel donorTableModel;
	private JButton refreshButton;
	
	// Use the DonorService to retrieve donors from a JSON file.
	private DonorService donorService;
	
	/**
	 * Constructs the DonorsPanel.
	 */
	public DonorsPanel()
	{
		// Initialize the donor service 
		this.donorService = new DonorService();
		initComponents();
		loadDonorData();
	}
	
	/**
	 * Initializes the UI components.
	 */
	private void initComponents()
	{
		setLayout(new BorderLayout());
		
		// Initialize the table model with column headers.
		this.donorTableModel = new DefaultTableModel(
			new Object[]
			{ "Donor ID", "Name", "Total Donations", "Last Donation Date" }, 0);
		this.donorTable = new JTable(this.donorTableModel);
		JScrollPane scrollPane = new JScrollPane(this.donorTable);
		scrollPane.setBorder(new TitledBorder("Donor List"));
		
		// Create a refresh button to reload donor data.
		this.refreshButton = new JButton("Refresh");
		this.refreshButton.addActionListener(e -> loadDonorData());
		
		// Assemble the panel.
		add(scrollPane, BorderLayout.CENTER);
		add(this.refreshButton, BorderLayout.SOUTH);
	}
	
	/**
	 * Loads donor data from the donor service and populates the table.
	 */
	private void loadDonorData()
	{
		// Clear any existing data.
		this.donorTableModel.setRowCount(0);
		// Retrieve the list of donors from the service.
		List<Donor> donors = this.donorService.getAllDonors();
		
		for (Donor donor : donors)
		{
			this.donorTableModel.addRow(new Object[]
			{
				donor.getDonorId(),
				donor.getName(),
				String.format("$%.2f", donor.getTotalDonations()),
				donor.getLastDonationDate()
			});
		}
		
	}
	
}
