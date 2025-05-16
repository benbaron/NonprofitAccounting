
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import nonprofitbookkeeping.core.FileBasedGrantsService;

import java.awt.*;
import java.util.List;

/**
 * GrantsPanel displays a list of grants with detailed information.
 * <p>
 * It retrieves grant data from a GrantsService (implemented here as FileBasedGrantsService)
 * and displays it in a table with columns for Grant ID, Grantor, Amount, Date Awarded,
 * Purpose, and Status.
 * </p>
 */
public class GrantsPanel extends JPanel
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -2540148378601203839L;
	private JTable grantTable;
	private DefaultTableModel tableModel;
	private JButton refreshButton;
	
	// Use a GrantsService to provide real grant data.
	private GrantsService grantsService;
	
	/**
	 * Constructs the GrantsPanel.
	 * In a production environment, the GrantsService should be injected.
	 */
	public GrantsPanel()
	{
		// Instantiate the service; here using a file-based implementation.
		this.grantsService = new FileBasedGrantsService();
		initComponents();
		loadGrantData();
	}
	
	/**
	 * Initializes the UI components.
	 */
	private void initComponents()
	{
		setLayout(new BorderLayout());
		
		// Set up the table model with column headers.
		String[] columns =
		{ "Grant ID", "Grantor", "Amount", "Date Awarded", "Purpose", "Status" };
		this.tableModel = new DefaultTableModel(columns, 0);
		this.grantTable = new JTable(this.tableModel);
		JScrollPane scrollPane = new JScrollPane(this.grantTable);
		scrollPane.setBorder(new TitledBorder("Grant List"));
		
		// Create a refresh button to reload grant data.
		this.refreshButton = new JButton("Refresh");
		this.refreshButton.addActionListener(e -> loadGrantData());
		
		// Assemble the panel.
		add(scrollPane, BorderLayout.CENTER);
		add(this.refreshButton, BorderLayout.SOUTH);
	}
	
	/**
	 * Loads grant data from the GrantsService and populates the table.
	 */
	private void loadGrantData()
	{
		// Clear current data.
		this.tableModel.setRowCount(0);
		// Retrieve a list of grants from the service.
		List<Grant> grants = this.grantsService.getAllGrants();
		
		for (Grant grant : grants)
		{
			this.tableModel.addRow(new Object[]
			{
				grant.getGrantId(),
				grant.getGrantor(),
				String.format("$%.2f", grant.getAmount()),
				grant.getDateAwarded(),
				grant.getPurpose(),
				grant.getStatus()
			});
		}
		
	}
	
	/**
	 * GrantsService defines the API for retrieving grant data.
	 * In a full implementation, this would be defined elsewhere (e.g., in a service package).
	 */
	public interface GrantsService
	{
		List<Grant> getAllGrants();
		
	}
	
	/**
	 * Donor-like model class representing a Grant.
	 * In a full system, this would likely be a more robust domain model.
	 */
	public static class Grant
	{
		private String grantId;
		private String grantor;
		private double amount;
		private String dateAwarded;
		private String purpose;
		private String status;
		
		// Default constructor required for JSON deserialization.
		public Grant()
		{
		}
		
		public Grant(String grantId, String grantor, double amount, String dateAwarded,
			String purpose, String status)
		{
			this.grantId = grantId;
			this.grantor = grantor;
			this.amount = amount;
			this.dateAwarded = dateAwarded;
			this.purpose = purpose;
			this.status = status;
		}
		
		public String getGrantId()
		{
			return this.grantId;
		}
		
		public void setGrantId(String grantId)
		{
			this.grantId = grantId;
		}
		
		public String getGrantor()
		{
			return this.grantor;
		}
		
		public void setGrantor(String grantor)
		{
			this.grantor = grantor;
		}
		
		public double getAmount()
		{
			return this.amount;
		}
		
		public void setAmount(double amount)
		{
			this.amount = amount;
		}
		
		public String getDateAwarded()
		{
			return this.dateAwarded;
		}
		
		public void setDateAwarded(String dateAwarded)
		{
			this.dateAwarded = dateAwarded;
		}
		
		public String getPurpose()
		{
			return this.purpose;
		}
		
		public void setPurpose(String purpose)
		{
			this.purpose = purpose;
		}
		
		public String getStatus()
		{
			return this.status;
		}
		
		public void setStatus(String status)
		{
			this.status = status;
		}
		
	}
	
}
