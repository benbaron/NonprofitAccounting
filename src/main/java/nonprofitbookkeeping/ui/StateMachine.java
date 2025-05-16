/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StateMachine.java
 * StateMachine
 */
package nonprofitbookkeeping.ui;

import javax.swing.*;
import java.util.EnumMap;

public class StateMachine {
    private AppState currentState;
    private final EnumMap<AppState, JMenuItem[]> menuStateMapping;

    public StateMachine(JMenuBar menuBar) {
        // Default state
        this.currentState = AppState.NO_FILE_OPEN;

        // Initialize the menu state mapping
        this.menuStateMapping = new EnumMap<>(AppState.class);

        // Set up mappings for each state to disable/enable menu items
        this.menuStateMapping.put(AppState.NO_FILE_OPEN, new JMenuItem[] {
            PanelContainer.openItem, PanelContainer.saveModifiedItem, PanelContainer.showReportsItem
        });

        this.menuStateMapping.put(AppState.FILE_OPEN, new JMenuItem[] {
            PanelContainer.saveModifiedItem, PanelContainer.showReportsItem, PanelContainer.showDashboardItem
        });

        this.menuStateMapping.put(AppState.TRANSACTION_IN_PROGRESS, new JMenuItem[] {
            PanelContainer.showReportsItem, PanelContainer.showJournalItem
        });

        this.menuStateMapping.put(AppState.REPORT_GENERATION, new JMenuItem[] {
            PanelContainer.showDashboardItem, PanelContainer.showJournalItem
        });
        
        // Initial state updates
        updateMenuItems();
    }

    // Set the current state and update the menu items accordingly
    public void setCurrentState(AppState newState) {
        this.currentState = newState;
        updateMenuItems();
    }

    // Enable or disable menu items based on the current state
    private void updateMenuItems() {
        // Disable all menu items first
        for (JMenuItem item : getAllMenuItems()) {
            item.setEnabled(false);
        }

        // Enable items based on the current state
        for (JMenuItem item : this.menuStateMapping.get(this.currentState)) {
            item.setEnabled(true);
        }
    }

    // Get all JMenuItems (you may want to map all items in your GUI here)
    private JMenuItem[] getAllMenuItems() {
        return new JMenuItem[] {
            PanelContainer.openItem, PanelContainer.saveModifiedItem, PanelContainer.showReportsItem, 
            PanelContainer.showDashboardItem, PanelContainer.showJournalItem
        };
    }
    
    static class PanelContainer {
        public static JMenuItem openItem = new JMenuItem("Open File...");
        public static JMenuItem saveModifiedItem = new JMenuItem("Save Modified Copy");
        public static JMenuItem showReportsItem = new JMenuItem("Show Reports");
        public static JMenuItem showDashboardItem = new JMenuItem("Show Dashboard");
        public static JMenuItem showJournalItem = new JMenuItem("Show Journal");
    }

}

//public class NonprofitBookkeeping {
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            createAndShowGUI();
//        });
//    }
//    private static void createAndShowGUI() {
//        // ... Other GUI setup ...
//
//        JMenuBar menuBar = new JMenuBar();
//        // Initialize the state machine
//        StateMachine stateMachine = new StateMachine(menuBar);
//
//        // Set the state based on your application's logic
//        // Example: when a file is opened, you can update the state
//        stateMachine.setCurrentState(AppState.FILE_OPEN);
//
//        // Example: when a transaction starts
//        // stateMachine.setCurrentState(AppState.TRANSACTION_IN_PROGRESS);
//
//        // Example: after report generation
//        // stateMachine.setCurrentState(AppState.REPORT_GENERATION);
//
//        // Set the menu bar for the frame
//        PanelContainer.frame.setJMenuBar(menuBar);
//        PanelContainer.frame.setLocationRelativeTo(null);
//        PanelContainer.frame.setVisible(true);
//    }
//}


enum AppState {
    NO_FILE_OPEN,
    FILE_OPEN,
    TRANSACTION_IN_PROGRESS,
    REPORT_GENERATION
}
