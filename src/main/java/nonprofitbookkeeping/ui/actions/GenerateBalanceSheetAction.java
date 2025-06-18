
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.reports.ReportConfiguration; // Added
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportCriteria;
import nonprofitbookkeeping.service.ReportConfigurationService; // Added
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox; // Added
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
import nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog;

import javafx.event.ActionEvent; // Added
import javafx.event.EventHandler; // Added
import javafx.stage.Window; // Added


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList; // Added
import java.util.List; // Added
import java.util.Optional;

import javax.swing.AbstractAction;


/**
 * JavaFX action handler for generating a Balance Sheet report.
 * This class extends {@link javax.swing.AbstractAction} (which is Swing-specific) and implements
 * the JavaFX {@link javafx.event.EventHandler} for {@link ActionEvent}. This dual nature is unusual;
 * typically, for a JavaFX application, only the JavaFX event handling mechanism would be used.
 * <p>
 * The action prompts the user for report criteria (end date, funds) using a {@link ReportCriteriaDialog},
 * then uses the {@link ReportService} to generate the report (typically as an XLSX file using JXLS).
 * It also handles saving the report configuration if a name is provided.
 * </p>
 */
public class GenerateBalanceSheetAction extends AbstractAction implements EventHandler<ActionEvent>
{
	
	/** The unique identifier for this serializable class (used by AbstractAction). */
	private static final long serialVersionUID = -5351611721074763080L;
	/** The specific report type identifier for the Balance Sheet report. */
	private final String reportType = "balance_sheet";
	/**
	 * Constructs a new {@code GenerateBalanceSheetAction}.
	 *
	 * @param reportService The {@link ReportService} to be used for report generation.
	 *                      Currently, the static {@code ReportService.generate} method is called,
	 *                      but this instance is stored for potential future changes.
	 */
	public GenerateBalanceSheetAction(ReportService reportService)
	{
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the JavaFX action event for generating a Balance Sheet report. It performs these steps:
	 * <ol>
	 *   <li>Checks if a company is open; shows an error and returns if not.</li>
	 *   <li>Displays a {@link ReportCriteriaDialog} to gather report parameters (end date, funds).</li>
	 *   <li>If the user cancels the dialog, the action terminates.</li>
	 *   <li>If a name for saving the configuration was provided and a company directory is available,
	 *       it creates a {@link ReportConfiguration} and saves it using {@link ReportConfigurationService}.</li>
	 *   <li>Constructs a {@link ReportContext} from the gathered criteria.</li>
	 *   <li>Calls the static {@link ReportService#generate(ReportContext, Ledger, ChartOfAccounts)}
	 *       method to produce the report file (typically an XLSX).</li>
	 *   <li>Shows an information alert with the path to the generated report, or an error alert if generation fails.</li>
	 * </ol>
	 * Catches and displays {@link IOException} related to configuration saving and any general {@link Exception}
	 * during report generation.
	 * </p>
	 * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
	 */
	@Override public void handle(ActionEvent event)
	{
		Window parentWindow = null;
		
		if (event.getSource() instanceof javafx.scene.Node)
		{
			parentWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
		}
		
		try
		{
			Company currentCompany = CurrentCompany.getCompany();
			
			if (currentCompany == null)
			{
				AlertBox.showError(parentWindow, "No company is currently open.");
				return;
			}
			
			List<Fund> availableFunds = new ArrayList<>(); // Placeholder, as actual fund list isn't
															// directly passed to dialog here
			
			Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
				parentWindow,
				"Balance Sheet Criteria",
				availableFunds, // This list of funds isn't directly used by the dialog in this
								// configuration
				null, // chartOfAccounts not needed for this specific dialog call variant for
						// Balance Sheet
				DateSelectionMode.SINGLE_DATE, // Balance sheet is as of a single date
				true, // Show fund selector
				false, // Do not show account selector for balance sheet
				null // No initial configuration to load
			);
			
			if (!criteriaOpt.isPresent())
			{
				return; // User cancelled
			}
			
			ReportCriteria criteria = criteriaOpt.get();
			LocalDate endDate = criteria.getEndDate(); // Dialog ensures this is not null for
														// SINGLE_DATE mode
			List<String> selectedFundIds = criteria.getSelectedFundIds();
			String configNameToSave = criteria.getNameForSaving();
			
			if (configNameToSave != null && !configNameToSave.trim().isEmpty())
			{
				ReportConfigurationService configService = new ReportConfigurationService();
				File companyDir = null;
				
				if (currentCompany.getCompanyFile() != null)
				{
					companyDir = currentCompany.getCompanyFile().getParentFile();
				}
				
				if (companyDir != null)
				{
					ReportConfiguration newConfig = new ReportConfiguration(
						configNameToSave,
						this.reportType,
						criteria.getDateSelectionMode(),
						criteria.getStartDate(), // Will be null for SINGLE_DATE mode from dialog
													// typically
						endDate,
						selectedFundIds);
					newConfig.setOutputFormat("xlsx"); // Default output format
					
					try
					{
						List<ReportConfiguration> allConfigs =
							configService.loadConfigurations(companyDir);
						allConfigs.add(newConfig);
						configService.saveConfigurations(allConfigs, companyDir);
						AlertBox.showInfo(parentWindow,
							"Report configuration '" + configNameToSave + "' saved.");
					}
					catch (IOException ex)
					{
						System.err.println("Error saving report configuration: " + ex.getMessage());
						ex.printStackTrace(); // Consider more robust logging/user feedback
						AlertBox.showError(parentWindow,
							"Error saving report configuration: " + ex.getMessage());
					}
					
				}
				else
				{
					AlertBox.showInfo(parentWindow,
						"Could not determine company directory. Configuration not saved.");
				}
				
			}
			
			ReportContext ctx = new ReportContext();
			ctx.setReportType(this.reportType);
			ctx.setStartDate(criteria.getStartDate()); // Pass along, may be null
			ctx.setEndDate(endDate);
			ctx.setFundIds(selectedFundIds);
			ctx.setOutputFormat("xlsx"); // Default or could be from criteria/config
			
			Ledger ledger = currentCompany.getLedger();
			ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();
			
			if (ledger == null || chartOfAccounts == null)
			{
				AlertBox.showError(parentWindow,
					"Ledger or Chart of Accounts not available for the current company.");
				return;
			}
			
			File f = ReportService.generate(ctx, ledger, chartOfAccounts);
			AlertBox.showInfo(parentWindow, "Balance Sheet saved to: " + f.getAbsolutePath());
			
		}
		catch (IOException ex)
		{ // Specifically for config saving/loading issues
			System.err.println("IO Error related to report configuration: " + ex.getMessage());
			ex.printStackTrace(); // Consider more robust logging/user feedback
			AlertBox.showError(parentWindow,
				"IO Error with report configuration: " + ex.getMessage());
		}
		catch (Exception ex)
		{ // General catch for other errors during report generation
			System.err.println("Error during Balance Sheet generation: " + ex.getMessage());
			ex.printStackTrace(); // Consider more robust logging/user feedback
			AlertBox.showError(parentWindow,
				"Failed to generate Balance Sheet: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Performs the action. This method is part of the Swing {@link javax.swing.Action} interface,
	 * implemented via {@link javax.swing.AbstractAction}.
	 * Note: This is a stub implementation and currently does nothing.
	 * For JavaFX, the {@link #handle(ActionEvent)} method is used.
	 */
	public void performAction()
	{
		// TODO Auto-generated method stub
		// This method would contain logic if this action were used in a Swing context.
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Invoked when a Swing action occurs. This method is part of the {@link java.awt.event.ActionListener} interface,
	 * implemented via {@link javax.swing.AbstractAction}.
	 * Note: This is a stub implementation and currently does nothing.
	 * For JavaFX, the {@link #handle(ActionEvent)} method is the primary event handler.
	 * </p>
	 * @param e The {@link java.awt.event.ActionEvent} that occurred.
	 */
	@Override public void actionPerformed(java.awt.event.ActionEvent e)
	{
		// TODO Auto-generated method stub
		// This method would be called if this action were triggered by a Swing
		// component.
	}
	
}
