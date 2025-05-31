
package nonprofitbookkeeping.plugins.scaledger.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector; // For DefaultTableModel data

public class PageViewerPanel extends JPanel
{
	
	private JTable table;
	private DefaultTableModel tableModel;
	private JScrollPane scrollPane;
	
	public PageViewerPanel()
	{
		// Initialize components
		this.tableModel = new DefaultTableModel();
		this.table = new JTable(this.tableModel);
		
		// Configure table properties
		this.table.setFillsViewportHeight(true);
		this.table.setAutoCreateRowSorter(true);
		
		// Initialize scroll pane and add table to it
		this.scrollPane = new JScrollPane(this.table);
		
		// Set layout for the panel and add scrollPane
		setLayout(new BorderLayout());
		add(this.scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Gets the underlying DefaultTableModel of the JTable.
	 * @return The DefaultTableModel.
	 */
	public DefaultTableModel getTableModel()
	{
		return this.tableModel;
	}
	
	/**
	 * Loads new data into the table, replacing existing data and columns.
	 * @param newModelData The DefaultTableModel containing the new data.
	 */
	public void loadData(DefaultTableModel newModelData)
	{
		
		if (newModelData == null)
		{
			// Clear existing data if new data is null
			this.tableModel.setDataVector(new Vector<>(), new Vector<>()); // Empty data and columns
			return;
		}
		
		// Replace data and columns with newModelData's content
		this.tableModel.setDataVector(
			newModelData.getDataVector(),
			null);
			// FIXME newModelData.getColumnIdentifiers());
		// No need to call fireTableStructureChanged() as setDataVector does this.
	}
	
	/**
	 * Displays this PageViewerPanel in a new non-modal JDialog window.
	 * @param parentComponentForLocation Component to center the dialog relative to (can be null).
	 * @param title The title for the dialog window.
	 */
	public void displayInWindow(Component parentComponentForLocation, String title)
	{
		// Determine owner Frame for JDialog
		Frame ownerFrame = null;
		
		if (parentComponentForLocation instanceof Frame)
		{
			ownerFrame = (Frame) parentComponentForLocation;
		}
		else if (parentComponentForLocation != null)
		{
			// Try to find a Frame ancestor
			Component ancestor = SwingUtilities.getWindowAncestor(parentComponentForLocation);
			
			if (ancestor instanceof Frame)
			{
				ownerFrame = (Frame) ancestor;
			}
			
		}
		
		JDialog dialog = new JDialog(ownerFrame, title, false); // false for modeless
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Dispose on close to free
																	// resources
		dialog.setContentPane(this); // Add this panel to the dialog
		dialog.pack(); // Size the dialog to fit its contents
		
		// Set preferred size if panel is small, or use a default
		if (dialog.getWidth() < 400 || dialog.getHeight() < 300)
		{
			dialog.setSize(new Dimension(600, 400)); // A reasonable default
		}
		
		dialog.setLocationRelativeTo(parentComponentForLocation); // Center relative to parent
		dialog.setVisible(true);
	}
	
	// Example main method for testing the panel (optional)
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			PageViewerPanel panel = new PageViewerPanel();
			
			// Create some dummy data for the table model
			DefaultTableModel testModel = new DefaultTableModel();
			testModel.addColumn("ID");
			testModel.addColumn("Name");
			testModel.addColumn("Value");
			
			for (int i = 1; i <= 20; i++)
			{
				testModel.addRow(new Object[]
				{ i, "Name " + i, Math.random() * 100 });
			}
			
			panel.loadData(testModel);
			
			// Display the panel in a JFrame for testing
			JFrame frame = new JFrame("Page Viewer Panel Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(panel); // Add panel directly or use displayInWindow
			// panel.displayInWindow(null, "Page Viewer Panel Test"); // Alternative using
			// the method
			frame.pack();
			
			if (frame.getWidth() < 400 || frame.getHeight() < 300)
			{
				frame.setSize(new Dimension(600, 400));
			}
			
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
