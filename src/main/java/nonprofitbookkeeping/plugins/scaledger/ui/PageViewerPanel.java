
package nonprofitbookkeeping.plugins.scaledger.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector; // For DefaultTableModel data

/**
 * A Swing JPanel designed to display tabular data using a JTable.
 * It includes functionality to load data from a {@link DefaultTableModel}
 * and to display itself within a JDialog window.
 * This panel is typically used to view pages or sheets of data, for example,
 * from an imported ledger or spreadsheet.
 */
public class PageViewerPanel extends JPanel
{
	
	private JTable table;
	private DefaultTableModel tableModel;
	private JScrollPane scrollPane;
	
	/**
	 * Constructs a new PageViewerPanel.
	 * Initializes the JTable, its DefaultTableModel, and a JScrollPane to contain the table.
	 * The table is configured to fill the viewport height and to allow auto-sorting by columns.
	 */
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
	 * Loads new data into the table from the provided {@link DefaultTableModel}.
	 * This method replaces any existing data and columns in the table.
	 * If {@code newModelData} is null, the table will be cleared (both data and columns).
	 * <p>
	 * Note: Column identifiers are now correctly extracted from {@code newModelData}.
	 * </p>
	 * @param newModelData The {@link DefaultTableModel} containing the new data and column structure.
	 *                     If null, clears the table.
	 */
	public void loadData(DefaultTableModel newModelData)
	{
		
		if (newModelData == null)
		{
			// Clear existing data if new data is null
			this.tableModel.setDataVector(new Vector<>(), new Vector<>()); // Empty
																			// data
																			// and
																			// columns
			return;
		}
		
		// Extract column identifiers from the newModelData
		Vector<String> columnIdentifiers = new Vector<>();
		
		for (int i = 0; i < newModelData.getColumnCount(); i++)
		{
			columnIdentifiers.add(newModelData.getColumnName(i));
		}
		
		// Replace data and columns with newModelData's content
		this.tableModel.setDataVector(
			newModelData.getDataVector(),
			columnIdentifiers);
		
		// No need to call fireTableStructureChanged() as setDataVector does
		// this.
	}
	
	/**
	 * Displays this PageViewerPanel in a new non-modal {@link JDialog} window.
	 * The dialog is configured to dispose on close. It attempts to find a parent Frame
	 * from {@code parentComponentForLocation} to act as its owner.
	 * The dialog is packed and then sized to a reasonable default if it's too small.
	 *
	 * @param parentComponentForLocation The {@link Component} to which the dialog's location will be relative.
	 *                                   Can be null, in which case the dialog may be centered on the screen.
	 * @param title The title for the dialog window.
	 */
	public void displayInWindow(Component parentComponentForLocation,
		String title)
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
			Component ancestor =
				SwingUtilities.getWindowAncestor(parentComponentForLocation);
			
			if (ancestor instanceof Frame)
			{
				ownerFrame = (Frame) ancestor;
			}
			
		}
		
		JDialog dialog = new JDialog(ownerFrame, title, false); // false for
																// modeless
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // Dispose
																			// on
																			// close
																			// to
																			// free
		// resources
		dialog.setContentPane(this); // Add this panel to the dialog
		dialog.pack(); // Size the dialog to fit its contents
		
		// Set preferred size if panel is small, or use a default
		if (dialog.getWidth() < 400 || dialog.getHeight() < 300)
		{
			dialog.setSize(new Dimension(600, 400)); // A reasonable default
		}
		
		dialog.setLocationRelativeTo(parentComponentForLocation); // Center
																	// relative
																	// to parent
		dialog.setVisible(true);
		
	}
	
	/**
	 * Main method for testing the {@link PageViewerPanel} independently.
	 * Creates a PageViewerPanel, loads it with sample data, and displays it in a JFrame.
	 * This is primarily for development and testing purposes.
	 * @param args Command line arguments (not used).
	 */
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
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setContentPane(panel); // Add panel directly or use
											// displayInWindow
			// panel.displayInWindow(null, "Page Viewer Panel Test"); //
			// Alternative using
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
