
package nonprofitbookkeeping.ui.panels;


import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 4555820548125432044L;
	// Bound components from the XML.
	private JTabbedPane tabbedPane1; // Bound to "tabbedPane1"
	private JPanel iPane12; // Bound to "iPane12" (first tab)
	private JPanel iPane11; // Bound to "iPane11" (second tab)
	private JPanel iPanel3; // Bound to "iPanel3" (right component of the split pane)
	
	public HelpPanel()
	{
		initComponents();
	}
	
	private void initComponents()
	{
		// Use GridBagLayout for the overall panel.
		setLayout(new GridBagLayout());
		// The XML specifies zero margin.
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		
		// Create a JSplitPane with default horizontal orientation.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setPreferredSize(new Dimension(394, 358));
		splitPane.setBorder(null);
		
		// ------ Left Component of the Split Pane ------
		// Create a panel to hold the tabbed pane.
		JPanel leftPanel = new JPanel(new BorderLayout());
		this.tabbedPane1 = new JTabbedPane(JTabbedPane.SCROLL_TAB_LAYOUT); // value "1" corresponds
																			// to SCROLL_TAB_LAYOUT.
		// Set preferred sizes as indicated in the XML.
		this.tabbedPane1.setPreferredSize(new Dimension(200, 200));
		
		// First tab: bound to iPane12.
		this.iPane12 = new JPanel(); // Empty panel; content can be added as needed.
		this.tabbedPane1.addTab("Untitled", this.iPane12);
		
		// Second tab: bound to iPane11.
		this.iPane11 = new JPanel(); // Empty panel.
		this.tabbedPane1.addTab("Untitled", this.iPane11);
		
		leftPanel.add(this.tabbedPane1, BorderLayout.CENTER);
		splitPane.setLeftComponent(leftPanel);
		
		// ------ Right Component of the Split Pane ------
		// Create an empty panel bound to iPanel3.
		this.iPanel3 = new JPanel(new BorderLayout());
		splitPane.setRightComponent(this.iPanel3);
		
		// Add the split pane to the main panel.
		add(splitPane, gbc);
	}
	
	// Main method for standalone testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("SSHelpPanel Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new HelpPanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
