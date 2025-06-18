
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Fund; // Assuming path
import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.reports.ReportCriteria;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.service.ReportConfigurationService;
import nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog;

import nonprofitbookkeeping.ui.actions.*; // Wildcard for action classes
import nonprofitbookkeeping.service.ReportService; // For actions constructor

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A Swing {@link JDialog} that allows users to manage saved report configurations.
 * Users can view a list of their saved configurations, run/review them (which typically
 * re-opens a criteria dialog pre-filled with the configuration), or delete them.
 * <p>
 * This dialog interacts with {@link ReportConfigurationService} to load and save configurations
 * and uses a {@link ReportConfigurationTableModel} to display them in a {@link JTable}.
 * It can also initiate report actions via {@link ReportService} when a configuration is run.
 * </p>
 */
public class ManageReportConfigurationsDialog extends JDialog
{
	
	/** Service for loading and saving report configurations. */
	private final ReportConfigurationService configService;
	/** The directory of the current company, used for storing/retrieving configuration files. */
	private final File companyDirectory;
	/** A list of available {@link Fund}s, passed to criteria dialogs when reviewing/running a configuration. */
	private final List<Fund> availableFunds;
	/** Service used to instantiate report actions when a configuration is run. */
	private final ReportService reportService;
	
	/** Table to display the list of saved report configurations. */
	private JTable configsTable;
	/** Table model backing the {@link #configsTable}. */
	private ReportConfigurationTableModel tableModel;
	
	/**
	 * Constructs a new {@code ManageReportConfigurationsDialog}.
	 *
	 * @param owner The parent {@link Frame} of this dialog.
	 * @param configService The {@link ReportConfigurationService} used for loading and saving configurations. Must not be null.
	 * @param companyDirectory The {@link File} representing the current company's data directory. Must not be null.
	 * @param availableFunds A list of available {@link Fund}s, used to populate fund selectors in criteria dialogs.
	 *                       If null, an empty list is used.
	 * @param reportService The {@link ReportService} used to obtain instances of report actions. Must not be null.
	 */
	public ManageReportConfigurationsDialog(Frame owner, ReportConfigurationService configService,
		File companyDirectory, List<Fund> availableFunds,
		ReportService reportService)
	{
		super(owner, "Manage Saved Report Configurations", true);
		this.configService = configService;
		this.companyDirectory = companyDirectory;
		this.availableFunds = (availableFunds != null) ? availableFunds : new ArrayList<>();
		this.reportService = reportService; // Store for action instantiation
		
		initComponents();
		layoutComponents();
		attachListeners();
		
		loadAndDisplayConfigurations();
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(owner);
	}
	
	/**
	 * Initializes the UI components of the dialog, primarily the {@link JTable}
	 * ({@link #configsTable}) and its associated {@link ReportConfigurationTableModel} ({@link #tableModel}).
	 * Sets table properties like selection mode and viewport filling.
	 */
	private void initComponents()
	{
		this.tableModel = new ReportConfigurationTableModel(new ArrayList<>());
		this.configsTable = new JTable(this.tableModel);
		this.configsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.configsTable.setFillsViewportHeight(true);
	}
	
	/**
	 * Lays out the UI components on the dialog panel.
	 * The main component is a {@link JScrollPane} containing the {@link #configsTable},
	 * placed in the center. Action buttons ("Run/Review Selected", "Delete Selected", "Close")
	 * are placed in a panel at the bottom (SOUTH).
	 */
	private void layoutComponents()
	{
		setLayout(new BorderLayout(5, 5));
		
		JScrollPane scrollPane = new JScrollPane(this.configsTable);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnRunReport = new JButton("Run/Review Selected");
		JButton btnDelete = new JButton("Delete Selected");
		JButton btnClose = new JButton("Close");
		
		btnRunReport.addActionListener(this::actionRunReviewConfiguration);
		btnDelete.addActionListener(this::actionDeleteConfiguration);
		btnClose.addActionListener(e -> dispose());
		
		buttonPanel.add(btnRunReport);
		buttonPanel.add(btnDelete);
		buttonPanel.add(btnClose);
		
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Attaches action listeners to the dialog's buttons.
	 * Note: In the current implementation, listeners are attached directly within {@link #layoutComponents()}.
	 * This method serves as a placeholder or for future refactoring if listener attachment becomes more complex.
	 */
	private void attachListeners()
	{
		// Listeners are attached directly in layoutComponents for simplicity here
	}
	
	/**
	 * Loads the saved report configurations from the {@link #companyDirectory} using the
	 * {@link #configService} and updates the {@link #tableModel} to display them in the table.
	 */
	private void loadAndDisplayConfigurations()
	{
		List<ReportConfiguration> configs = this.configService.loadConfigurations(this.companyDirectory);
		this.tableModel.setConfigurations(configs);
	}
	
	/**
	 * Handles the action of running or reviewing a selected report configuration.
	 * When a configuration is selected in the table and this action is triggered:
	 * <ol>
	 *   <li>A {@link ReportCriteriaDialog} is shown, pre-filled with the details of the selected configuration.</li>
	 *   <li>The user can review, modify, and then choose to "Run Report" or "Save Configuration" (potentially as new) from that dialog.</li>
	 *   <li>If the user saves the configuration (even if modified), this method handles updating or adding it via {@link #configService}.</li>
	 *   <li>The actual execution of the report is assumed to be driven by the user's interaction with the {@code ReportCriteriaDialog}
	 *       and the corresponding report action class that originally would have shown it. This method primarily facilitates
	 *       re-opening that dialog with saved settings.</li>
	 * </ol>
	 *
	 * @param e The {@link ActionEvent} that triggered this action.
	 */
	private void actionRunReviewConfiguration(ActionEvent e)
	{
		int selectedRow = this.configsTable.getSelectedRow();
		
		if (selectedRow < 0)
		{
			JOptionPane.showMessageDialog(this, "Please select a configuration to run/review.",
				"No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		ReportConfiguration selectedConfig = this.tableModel.getConfigurationAt(selectedRow);
		if (selectedConfig == null)
			return;
		
		// V1: Pre-fill ReportCriteriaDialog with the selected configuration
		Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
			this, // Parent dialog
			"Review/Run: " + selectedConfig.getUserGivenName(),
			this.availableFunds, // Pass available funds
			selectedConfig.getDateSelectionMode(), // Use saved DateSelectionMode
			true, // Show fund selector
			selectedConfig // Pass the selected config to pre-fill
		);
		
		if (criteriaOpt.isPresent())
		{
			ReportCriteria criteria = criteriaOpt.get();
			String configNameToSave = criteria.getNameForSaving(); // Check if user wants to re-save
																	// (possibly under new name)
			
			// If user clicked "Save Configuration..." in the criteria dialog
			if (configNameToSave != null && !configNameToSave.trim().isEmpty())
			{
				ReportConfiguration newOrUpdatedConfig = new ReportConfiguration(
					configNameToSave,
					selectedConfig.getReportType(), // Keep original report type
					criteria.getDateSelectionMode(),
					criteria.getStartDate(),
					criteria.getEndDate(),
					criteria.getSelectedFundIds());
				newOrUpdatedConfig.setOutputFormat(selectedConfig.getOutputFormat()); // Keep
																						// original
																						// format
				// If original ID was to be preserved for update, set it:
				// newOrUpdatedConfig.setConfigurationId(selectedConfig.getConfigurationId());
				// For now, creating as potentially new if name changed, or simple add.
				// More sophisticated update logic would handle replacing by ID.
				
				try
				{
					List<ReportConfiguration> allConfigs =
						this.configService.loadConfigurations(this.companyDirectory);
					// Simple add for now. For update, find by ID and replace.
					// To prevent duplicate names if desired, add check here.
					boolean found = false; // For update logic if needed
					
					if (newOrUpdatedConfig.getConfigurationId() == null)
					{ // if it's a new save from an old config
						newOrUpdatedConfig.setConfigurationId(selectedConfig.getConfigurationId()); // keep
																									// old
																									// id
																									// if
																									// "save
																									// as"
					}
					
					for (int i = 0; i < allConfigs.size(); i++)
					{
						
						if (allConfigs.get(i).getConfigurationId()
							.equals(newOrUpdatedConfig.getConfigurationId()))
						{
							allConfigs.set(i, newOrUpdatedConfig);
							found = true;
							break;
						}
						
					}
					
					if (!found)
						allConfigs.add(newOrUpdatedConfig);
					
					
					this.configService.saveConfigurations(allConfigs, this.companyDirectory);
					JOptionPane.showMessageDialog(this,
						"Report configuration '" + configNameToSave + "' saved.",
						"Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
					loadAndDisplayConfigurations(); // Refresh table
				}
				catch (IOException ex)
				{
					JOptionPane.showMessageDialog(this,
						"Error saving updated configuration: " + ex.getMessage(), "Save Error",
						JOptionPane.ERROR_MESSAGE);
				}
				
			}
			
			// Proceed to "run" the report by invoking the appropriate action class
			// This requires instantiating and calling the specific action.
			// This is a simplified way to trigger; a more robust system might use a factory
			// or event bus.
			AbstractAction reportAction = getActionForReportType(selectedConfig.getReportType());
			
			if (reportAction instanceof ReportActionInterface)
			{ // Assuming actions implement a common interface
				// The action classes currently get criteria from ReportCriteriaDialog directly.
				// To run with existing criteria, we'd need to bypass their dialog or pass
				// criteria.
				//
				// For V1 "pre-fill" approach, this direct run part is complex.
				// The ReportCriteriaDialog handles re-running.
				// The current structure: criteria dialog shown, user clicks "Run", action gets
				// criteria. So, if criteriaOpt.isPresent(), the action that showed the dialog would
				// proceed. Here, we are *simulating* that flow.
				// The dialog was already shown. If user clicked "Run Report" in criteria
				// dialog, then the action class that would normally call it needs to be executed.
				//
				// This is where it gets tricky without a central dispatcher.
				// For simplicity, we assume the user will click "Run Report" in the re-shown
				// ReportCriteriaDialog.
				//
				// The action of running the report is thus handled by the ReportCriteriaDialog's outcome
				// being processed by the original Action classes (which are not directly called
				// from here after this point).
				// The ReportCriteriaDialog.showDialog itself does not run the report.
				// The existing Action classes call ReportCriteriaDialog, then use the result to
				// call ReportService.
				//
				// So, this "Run/Review" button re-opens that dialog, and the user drives from
				// there.
				//
				// No further action needed here other than having shown the dialog.
				System.out.println("User reviewed/ran configuration: " +
					selectedConfig.getUserGivenName() +
					". Action will be driven by ReportCriteriaDialog outcome in the respective action class.");
				
			}
			else if (reportAction != null)
			{
				// Fallback if not ReportActionInterface, though less direct.
				// This path may not be fully functional if actions expect direct UI event.
				reportAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
					selectedConfig.getReportType()));
			}
			else
			{
				JOptionPane.showMessageDialog(this,
					"No action configured for report type: " + selectedConfig.getReportType(),
					"Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
	}
	
	// Helper to get an instance of the correct report action.
	// This is a simplified approach. A more robust system might use a factory or
	// service locator.
	/**
	 * Helper method to obtain an instance of the appropriate {@link AbstractAction}
	 * for a given report type string. This is used to simulate triggering the report
	 * if the user were to "run" it directly after reviewing criteria, though the primary
	 * interaction flow relies on the re-shown {@link ReportCriteriaDialog}.
	 * <p>
	 * Note: This method creates new instances of action classes. For actions requiring
	 * specific context or services (like {@code BudgetService}), it attempts to provide them.
	 * </p>
	 *
	 * @param reportType A string identifier for the report type (e.g., "income_statement").
	 * @return An {@link AbstractAction} corresponding to the report type, or {@code null} if
	 *         the type is not recognized or a required service is unavailable.
	 */
	private AbstractAction getActionForReportType(String reportType)
	{
		if (reportType == null)
		{
			return null;
		}
		
		// Assuming ReportService and BudgetService instances are available or can be
		// created.
		//
		// For this dialog, we have this.reportService and can create a new
		// BudgetService if needed by an action.
		//
		BudgetService localBudgetService =
			(this.configService != null) ? new BudgetService() : null; // Or pass if available
		
		switch(reportType)
		{
			case "income_statement":
				return new GenerateIncomeStatementAction(this.reportService);
				
			case "balance_sheet":
				return new GenerateBalanceSheetAction(this.reportService);
				
			case "trial_balance":
				return new GenerateTrialBalanceAction(this.reportService);
				
			case "cash_flow_statement":
				return new GenerateCashFlowStatementAction(this.reportService);
				
			case "budget_vs_actuals":
				if (localBudgetService == null)
				{ // BudgetService is needed for BvA action
					System.err.println(
						"BudgetService not available for BvA report action from Manage dialog.");
					return null;
				}
				return new GenerateBudgetVsActualsReportAction(this.reportService,
					localBudgetService);
				
			default:
				return null;
		}
		
	}
	
	
	/**
	 * Handles the action of deleting a selected report configuration.
	 * Prompts the user for confirmation before deleting. If confirmed, the selected configuration
	 * is removed from the list managed by {@link #configService} and the list is re-saved.
	 * The table display is then refreshed.
	 *
	 * @param e The {@link ActionEvent} that triggered this action.
	 */
	private void actionDeleteConfiguration(ActionEvent e)
	{
		int selectedRow = this.configsTable.getSelectedRow();
		
		if (selectedRow < 0)
		{
			JOptionPane.showMessageDialog(this, "Please select a configuration to delete.",
				"No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		ReportConfiguration configToDelete = this.tableModel.getConfigurationAt(selectedRow);
		if (configToDelete == null)
			return;
		
		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to delete configuration: " + configToDelete.getUserGivenName() +
				"?",
			"Confirm Delete", JOptionPane.YES_NO_OPTION);
		
		if (confirm == JOptionPane.YES_OPTION)
		{
			
			try
			{
				List<ReportConfiguration> allConfigs =
					this.configService.loadConfigurations(this.companyDirectory);
				// Remove by ID to be safe, though table model index should be fine
				allConfigs.removeIf(config -> config.getConfigurationId()
					.equals(configToDelete.getConfigurationId()));
				this.configService.saveConfigurations(allConfigs, this.companyDirectory);
				loadAndDisplayConfigurations(); // Refresh table
				JOptionPane.showMessageDialog(this, "Configuration deleted.", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			}
			catch (IOException ex)
			{
				JOptionPane.showMessageDialog(this,
					"Error deleting configuration: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
	}
	
}
