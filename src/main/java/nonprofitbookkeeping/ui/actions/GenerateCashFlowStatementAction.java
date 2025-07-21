
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.reports.ReportConfiguration; 
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportCriteria;
import nonprofitbookkeeping.service.ReportConfigurationService; 
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox; 
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
import nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog;

import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.stage.Window; 


// import javax.swing.*; // Removed
// import java.awt.event.ActionEvent; // Removed
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList; 
import java.util.List; 
import java.util.Optional;

import javax.swing.AbstractAction;


/**
 * JavaFX action handler for generating a Cash Flow Statement report. This class
 * extends {@link javax.swing.AbstractAction} (Swing-specific) and implements
 * the JavaFX {@link javafx.event.EventHandler} for {@link ActionEvent}. This
 * dual inheritance is atypical for a pure JavaFX application.
 * <p>
 * The action prompts the user for report criteria (date range, funds) using a
 * {@link ReportCriteriaDialog}, then utilizes the {@link ReportService} to
 * generate the report, typically as an XLSX file via JXLS templates. It also
 * supports saving the chosen report configuration.
 * </p>
 */
public class GenerateCashFlowStatementAction extends AbstractAction
	implements EventHandler<ActionEvent>
{
	
	/** The specific report type identifier for the Cash Flow Statement. */
	private final String reportType = "cash_flow_statement";
	/**
	 * Constructs a new {@code GenerateCashFlowStatementAction}.
	 *
	 * @param reportService The {@link ReportService} to be used for report generation.
	 *                      While the current implementation calls a static {@code ReportService.generate} method,
	 *                      this instance is stored for potential future refactoring or extended use.
	 */
	public GenerateCashFlowStatementAction(ReportService reportService)
	{
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the JavaFX action event for generating a Cash Flow Statement. The process includes:
	 * <ol>
	 *   <li>Verifying that a company is currently open.</li>
	 *   <li>Displaying a {@link ReportCriteriaDialog} to collect user input for the report's
	 *       date range and any fund-specific filters. Account selection is disabled for this report type.</li>
	 *   <li>If the user cancels the dialog, the action is aborted.</li>
	 *   <li>If the user provides a name for saving the configuration, the current criteria
	 *       (report type, dates, funds, default output format "xlsx") are saved
	 *       using {@link ReportConfigurationService}.</li>
	 *   <li>A {@link ReportContext} is created based on the user's criteria.</li>
	 *   <li>The static {@link ReportService#generate(ReportContext, Ledger, ChartOfAccounts)} method
	 *       is invoked to produce the report file.</li>
	 *   <li>A confirmation message with the report file path is shown, or an error message if any step fails.</li>
	 * </ol>
	 * Exceptions during configuration saving or report generation are caught and displayed to the user.
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
			
			List<Fund> availableFunds = new ArrayList<>(); // Placeholder; actual list of funds for
															// dialog not populated here
			// ChartOfAccounts is not passed to this ReportCriteriaDialog variant as account
			// selection is disabled.
			
			Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
				parentWindow,
				"Cash Flow Statement Criteria",
				availableFunds, // This list of funds isn't directly used by the dialog in this
								// configuration
				null, // chartOfAccounts - not needed as account selector is false
				DateSelectionMode.DATE_RANGE_MANDATORY_START, // Requires both start and end date
				true, // showFundSelector
				false, // showAccountSelector - false for Cash Flow Statement
				null // initialConfig - no pre-loaded configuration
			);
			
			if (!criteriaOpt.isPresent())
			{
				return; // User cancelled
			}
			
			ReportCriteria criteria = criteriaOpt.get();
			LocalDate startDate = criteria.getStartDate(); // Dialog ensures this is not null for
															// DATE_RANGE_MANDATORY_START
			LocalDate endDate = criteria.getEndDate(); // Dialog ensures this is not null
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
						startDate,
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
						ex.printStackTrace(); // Consider more robust logging
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
			ctx.setStartDate(startDate);
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
			AlertBox.showInfo(parentWindow,
				"Cash Flow Statement saved to: " + f.getAbsolutePath());
			
		}
		catch (IOException ex)
		{ // Specifically for config saving/loading issues
			System.err.println("IO Error related to report configuration: " + ex.getMessage());
			ex.printStackTrace(); // Consider more robust logging
			AlertBox.showError(parentWindow,
				"IO Error with report configuration: " + ex.getMessage());
		}
		catch (Exception ex)
		{ // General catch for other errors
			System.err.println("Error during Cash Flow Statement generation: " + ex.getMessage());
			ex.printStackTrace(); // Consider more robust logging
			AlertBox.showError(parentWindow,
				"Failed to generate Cash Flow Statement: " + ex.getMessage());
		}
		
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
                handle(new ActionEvent());
        }
	
	
}
