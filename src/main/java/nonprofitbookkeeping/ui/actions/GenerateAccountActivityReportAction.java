package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportCriteria;
import nonprofitbookkeeping.service.ReportConfigurationService;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
import nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog; // Uncommented

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Window; // Added for parent window
// import javax.swing.*; // Unused Swing import
// import java.awt.event.ActionEvent; // Unused AWT import
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX action handler for generating an Account Activity Detail report.
 * This action prompts the user for report criteria (date range, funds, accounts)
 * using a {@link ReportCriteriaDialog}, then uses the {@link ReportService}
 * to prepare the data and generate the report (typically as an XLSX file using JXLS).
 * It also handles saving the report configuration if a name is provided.
 */
public class GenerateAccountActivityReportAction implements EventHandler<ActionEvent> {

    // private static final long serialVersionUID = 1L; // Not needed for EventHandler
    /** The specific report type identifier for the Account Activity Detail report. */
    private final String reportType = "account_activity_detail";

    /**
     * The {@link ReportService} instance used to generate the report and prepare data.
     * This field is not strictly needed if only static methods of ReportService are used,
     * but it's kept for consistency or potential future use with instance methods.
     */
    private final ReportService reportService; 

    /**
     * Constructs a new {@code GenerateAccountActivityReportAction}.
     *
     * @param reportService The {@link ReportService} to be used for report generation.
     *                      While the current static {@code ReportService.generate} method is used,
     *                      this parameter is kept for potential future dependency injection or
     *                      use of instance methods on {@code reportService}.
     */
    public GenerateAccountActivityReportAction(ReportService reportService) {
        // super("Generate Account Activity Detail"); // Not needed for EventHandler
        this.reportService = reportService; // Stored if needed by non-static methods later
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles the action event for generating an Account Activity Detail report. It performs these steps:
     * <ol>
     *   <li>Checks if a company is open; shows an error and returns if not.</li>
     *   <li>Displays a {@link ReportCriteriaDialog} to gather report parameters from the user (dates, funds, accounts).</li>
     *   <li>If the user cancels the dialog, the action terminates.</li>
     *   <li>Validates that at least one account was selected; shows an error and returns if not.</li>
     *   <li>If a name for saving the configuration was provided and a company directory is available,
     *       it creates a {@link ReportConfiguration}, saves it using {@link ReportConfigurationService}.</li>
     *   <li>Constructs a {@link ReportContext} from the gathered criteria.</li>
     *   <li>Calls the static {@link ReportService#generate(ReportContext, Ledger, ChartOfAccounts)}
     *       method to produce the report file (typically an XLSX).</li>
     *   <li>Shows an information alert with the path to the generated report, or an error alert if generation fails.</li>
     * </ol>
     * Catches and displays {@link IOException} related to configuration saving and any general {@link Exception}
     * during report generation.
     * </p>
     * @param event The {@link ActionEvent} that triggered this handler.
     */
    @Override
    public void handle(ActionEvent event) {
        Company currentCompany = CurrentCompany.getCompany();
        if (currentCompany == null) {
            AlertBox.showError(null, "No company is currently open.");
            return;
        }

        ChartOfAccounts coa = currentCompany.getChartOfAccounts();
        if (coa == null) {
            AlertBox.showError(null, "Chart of Accounts not available.");
            return;
        }

        File companyDir = null;
        if (currentCompany.getCompanyFile() != null) {
            companyDir = currentCompany.getCompanyFile().getParentFile();
        }
        // companyDir can be null if companyFile is not set, saving config will be skipped.

        List<Fund> availableFunds = new ArrayList<>(); // Placeholder, as Fund list isn't available from Company/COA
        // Attempt to get the parent window from the event source
        Window parentWindow = null;
        if (event.getSource() instanceof javafx.scene.Node) {
            parentWindow = ((javafx.scene.Node)event.getSource()).getScene().getWindow();
        }

        try {
            Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
                parentWindow, 
                "Account Activity Report Criteria",
                availableFunds,
                coa, // Pass ChartOfAccounts for account selection
                DateSelectionMode.DATE_RANGE_MANDATORY_START,
                true, // Show fund selector
                true, // Show account selector
                null  // No initial configuration to load
            );

            if (!criteriaOpt.isPresent()) {
                return; // User cancelled
            }

            ReportCriteria criteria = criteriaOpt.get();
            
            // Validate that at least one account was selected
            if (criteria.getSelectedAccountIds() == null || criteria.getSelectedAccountIds().isEmpty()) {
                AlertBox.showError(null, "Please select at least one account for the report.");
                // Optionally, re-show the dialog or simply return
                // For simplicity, returning here. To re-show, call showDialog again.
                return; 
            }

            LocalDate startDate = criteria.getStartDate(); // Dialog ensures this is not null
            LocalDate endDate = criteria.getEndDate();     // Dialog ensures this is not null
            List<String> selectedFundIds = criteria.getSelectedFundIds();
            List<String> selectedAccountIds = criteria.getSelectedAccountIds();
            String configNameToSave = criteria.getNameForSaving();

            if (configNameToSave != null && !configNameToSave.trim().isEmpty()) {
                if (companyDir != null) {
                    ReportConfigurationService configService = new ReportConfigurationService();
                    ReportConfiguration newConfig = new ReportConfiguration(
                        configNameToSave,
                        this.reportType,
                        criteria.getDateSelectionMode(),
                        startDate,
                        endDate,
                        selectedFundIds
                    );
                    newConfig.setAccountIdsForDetailReport(selectedAccountIds); // Set account IDs
                    newConfig.setOutputFormat("xlsx"); // Default or from criteria if available

                    try {
                        List<ReportConfiguration> allConfigs = configService.loadConfigurations(companyDir);
                        allConfigs.add(newConfig); 
                        configService.saveConfigurations(allConfigs, companyDir);
                        AlertBox.showInfo(null, "Report configuration '" + configNameToSave + "' saved.");
                    } catch (IOException ex) {
                        System.err.println("Error saving report configuration: " + ex.getMessage());
                        ex.printStackTrace();
                        AlertBox.showError(null, "Error saving report configuration: " + ex.getMessage());
                    }
                } else {
                     AlertBox.showInfo(null, "Could not determine company directory. Configuration not saved."); // Changed from WARNING to INFO
                }
            }

            ReportContext ctx = new ReportContext();
            ctx.setReportType(this.reportType);
            ctx.setStartDate(startDate);
            ctx.setEndDate(endDate);
            ctx.setFundIds(selectedFundIds);
            ctx.setAccountIdsForDetailReport(selectedAccountIds);
            ctx.setOutputFormat("xlsx");

            Ledger ledger = currentCompany.getLedger();
            if (ledger == null) {
                AlertBox.showError(null, "Ledger not available for the current company.");
                return;
            }

            File f = ReportService.generate(ctx, ledger, coa); // Assuming ReportService is static or instance is available
            AlertBox.showInfo(parentWindow, "Account Activity Detail report saved to: " + f.getAbsolutePath());

        } catch (IOException ex) { 
            System.err.println("IO Error related to report configuration loading/saving: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow, "IO Error with report configuration: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Error during Account Activity Detail report generation: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow, "Failed to generate report: " + ex.getMessage());
        }
    }

	/**
	 * Placeholder method, potentially from a previous Swing ActionListener implementation.
	 * This method is not part of the JavaFX {@link EventHandler} interface and is currently a stub.
	 *
	 * @param actionEvent The event object (parameter name changed from 'object' for clarity, though type is Object).
	 * @return Currently returns null as it's a stub. The intended return type and behavior are undefined.
	 */
	public Object actionPerformed(Object actionEvent)
	{
		// TODO Auto-generated method stub
		// This method signature resembles ActionListener.actionPerformed(ActionEvent),
		// but with a generic Object parameter. It's not used in the JavaFX event handling flow.
		return null;
	}
}
