/**
 * nonprofit-scaledger-ribbon.zip_expanded StateMachine.java StateMachine
 */

package nonprofitbookkeeping.core;

import javax.swing.*;
import java.util.EnumMap;

public class StateMachine
{
	private AppState currentState;
	private final EnumMap<AppState, JMenuItem[]> menuStateMapping;
	
	/**
	 * Constructs a StateMachine.
	 * Initializes the state to {@link AppState#NO_FILE_OPEN} and sets up menu item states.
	 * @param menuBar The JMenuBar whose items will be managed by this state machine.
	 */
	public StateMachine(JMenuBar menuBar)
	{
		// Default state
		this.currentState = AppState.NO_FILE_OPEN;
		
		// Initialize the menu state mapping
		this.menuStateMapping = new EnumMap<>(AppState.class);
		
		// Set up mappings for each state to disable/enable menu items
		this.menuStateMapping.put(AppState.NO_FILE_OPEN, new JMenuItem[]
		{
			PanelContainer.openItem, PanelContainer.saveModifiedItem, PanelContainer.showReportsItem
		});
		
		this.menuStateMapping.put(AppState.FILE_OPEN, new JMenuItem[]
		{
			PanelContainer.saveModifiedItem, PanelContainer.showReportsItem,
			PanelContainer.showDashboardItem
		});
		
		this.menuStateMapping.put(AppState.TRANSACTION_IN_PROGRESS, new JMenuItem[]
		{
			PanelContainer.showReportsItem, PanelContainer.showJournalItem
		});
		
		this.menuStateMapping.put(AppState.REPORT_GENERATION, new JMenuItem[]
		{
			PanelContainer.showDashboardItem, PanelContainer.showJournalItem
		});
		
		// Initial state updates
		updateMenuItems();
	}
	
	/**
	 * Sets the current state of the application and updates the menu items accordingly.
	 * @param newState The new application state.
	 */
	public void setCurrentState(AppState newState)
	{
		this.currentState = newState;
		updateMenuItems();
	}
	
	/**
	 * Enables or disables menu items based on the current application state.
	 * It first disables all relevant menu items and then enables only those
	 * that are appropriate for the current state.
	 */
	private void updateMenuItems()
	{
		
		// Disable all menu items first
		for (JMenuItem item : getAllMenuItems())
		{
			item.setEnabled(false);
		}
		
		// Enable items based on the current state
		for (JMenuItem item : this.menuStateMapping.get(this.currentState))
		{
			item.setEnabled(true);
		}
		
	}
	
	/**
	 * Retrieves all JMenuItems that are managed by the state machine.
	 * This is a helper method for {@link #updateMenuItems()}.
	 * @return An array of JMenuItems.
	 */
	private JMenuItem[] getAllMenuItems()
	{
		return new JMenuItem[]
		{
			PanelContainer.openItem, PanelContainer.saveModifiedItem,
			PanelContainer.showReportsItem,
			PanelContainer.showDashboardItem, PanelContainer.showJournalItem
		};
	}
	
	/**
	 * Static inner class serving as a container for JMenuItem instances.
	 * This allows menu items to be centrally defined and accessed.
	 */
	static class PanelContainer
	{
		public static JMenuItem openItem = new JMenuItem("Open File...");
		public static JMenuItem saveModifiedItem = new JMenuItem("Save Modified Copy");
		public static JMenuItem showReportsItem = new JMenuItem("Show Reports");
		public static JMenuItem showDashboardItem = new JMenuItem("Show Dashboard");
		public static JMenuItem showJournalItem = new JMenuItem("Show Journal");
		
	}
	
}

/**
 * Enum representing the different states of the application.
 * These states are used by the {@link StateMachine} to control UI behavior.
 */
// public class NonprofitBookkeeping {
// public static void main(String[] args) {
// SwingUtilities.invokeLater(() -> {
// createAndShowGUI();
// });
// }
// private static void createAndShowGUI() {
// // ... Other GUI setup ...
//
// JMenuBar menuBar = new JMenuBar();
// // Initialize the state machine
// StateMachine stateMachine = new StateMachine(menuBar);
//
// // Set the state based on your application's logic
// // Example: when a file is opened, you can update the state
// stateMachine.setCurrentState(AppState.FILE_OPEN);
//
// // Example: when a transaction starts
// // stateMachine.setCurrentState(AppState.TRANSACTION_IN_PROGRESS);
//
// // Example: after report generation
// // stateMachine.setCurrentState(AppState.REPORT_GENERATION);
//
// // Set the menu bar for the frame
// PanelContainer.frame.setJMenuBar(menuBar);
// PanelContainer.frame.setLocationRelativeTo(null);
// PanelContainer.frame.setVisible(true);
// }
// }

/**
 * Enum representing the different states of the application.
 * These states are used by the {@link StateMachine} to control UI behavior.
 */
enum AppState
{
	/** State when no file is open in the application. */
	NO_FILE_OPEN,
	/** State when a file is open and being viewed or edited. */
	FILE_OPEN,
	/** State when a transaction is actively being entered or processed. */
	TRANSACTION_IN_PROGRESS,
	/** State when a report is being generated. */
	REPORT_GENERATION
}
