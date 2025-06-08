
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
// import javax.swing.*; // Removed
// import java.awt.event.ActionEvent; // Removed
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList; // Added
import java.util.List; // Added
import java.util.Optional;

import javax.swing.AbstractAction;



/**
 * JavaFX action handler for generating a Trial Balance report.
 * This class extends {@link javax.swing.AbstractAction} (Swing-specific) and implements
 * the JavaFX {@link javafx.event.EventHandler} for {@link ActionEvent}. This dual inheritance
 * is atypical for a pure JavaFX application.
 * <p>
 * The action prompts the user for report criteria (end date, optional start date, funds) using a
 * {@link ReportCriteriaDialog}, then utilizes the {@link ReportService} to generate the report,
 * typically as an XLSX file via JXLS templates. It also supports saving the chosen
 * report configuration.
 * </p>
 */
public class GenerateTrialBalanceAction extends AbstractAction implements EventHandler<ActionEvent> {

    // private static final long serialVersionUID = 1L; // Not applicable for JavaFX EventHandler
    /** The specific report type identifier for the Trial Balance report. */
    private final String reportType = "trial_balance";
    /**
     * The {@link ReportService} instance used for report generation.
     * Stored for potential use if {@code ReportService.generate} were non-static or if other service methods were needed.
     */
    private final ReportService reportService;

    /**
     * Constructs a new {@code GenerateTrialBalanceAction}.
     *
     * @param reportService The {@link ReportService} to be used for report generation.
     *                      While the current implementation calls a static {@code ReportService.generate} method,
     *                      this instance is stored for potential future refactoring or extended use.
     */
    public GenerateTrialBalanceAction(ReportService reportService) {
        // super("Generate Trial Balance"); // Constructor for AbstractAction, not used for JavaFX
        this.reportService = reportService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles the JavaFX action event for generating a Trial Balance report. The process includes:
     * <ol>
     *   <li>Verifying that a company is currently open.</li>
     *   <li>Displaying a {@link ReportCriteriaDialog} to collect user input for the report's
     *       end date, an optional start date, and any fund-specific filters. Account selection is disabled.</li>
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
    @Override
    public void handle(ActionEvent event) {
        Window parentWindow = null;
        if (event.getSource() instanceof javafx.scene.Node) {
            parentWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        }

        try {
            Company currentCompany = CurrentCompany.getCompany();

            if (currentCompany == null) {
                AlertBox.showError(parentWindow, "No company is currently open.");
                return;
            }

            List<Fund> availableFunds = new ArrayList<>(); // Placeholder; actual list of funds for dialog not populated here
            // ChartOfAccounts is not passed to this ReportCriteriaDialog variant as account selection is disabled.

            Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
                    parentWindow,
                    "Trial Balance Criteria",
                    availableFunds, // This list of funds isn't directly used by the dialog in this configuration
                    null, // chartOfAccounts - not needed as account selector is false
                    DateSelectionMode.DATE_RANGE_OPTIONAL_START, // End date is primary, start date is optional
                    true, // showFundSelector
                    false, // showAccountSelector - false for Trial Balance
                    null // initialConfig - no pre-loaded configuration
            );

            if (!criteriaOpt.isPresent()) {
                return; // User cancelled
            }

            ReportCriteria criteria = criteriaOpt.get();
            LocalDate startDate = criteria.getStartDate(); // Can be null
            LocalDate endDate = criteria.getEndDate();     // Dialog ensures this is not null
            List<String> selectedFundIds = criteria.getSelectedFundIds();
            String configNameToSave = criteria.getNameForSaving();

            if (configNameToSave != null && !configNameToSave.trim().isEmpty()) {
                ReportConfigurationService configService = new ReportConfigurationService();
                File companyDir = null;

                if (currentCompany.getCompanyFile() != null) {
                    companyDir = currentCompany.getCompanyFile().getParentFile();
                }

                if (companyDir != null) {
                    ReportConfiguration newConfig = new ReportConfiguration(
                            configNameToSave,
                            this.reportType,
                            criteria.getDateSelectionMode(),
                            startDate,
                            endDate,
                            selectedFundIds);
                    newConfig.setOutputFormat("xlsx"); // Default output format

                    try {
                        List<ReportConfiguration> allConfigs =
                                configService.loadConfigurations(companyDir);
                        allConfigs.add(newConfig);
                        configService.saveConfigurations(allConfigs, companyDir);
                        AlertBox.showInfo(parentWindow,
                                "Report configuration '" + configNameToSave + "' saved.");
                    } catch (IOException ex) {
                        System.err.println("Error saving report configuration: " + ex.getMessage());
                        ex.printStackTrace(); // Consider more robust logging
                        AlertBox.showError(parentWindow,
                                "Error saving report configuration: " + ex.getMessage());
                    }

                } else {
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

            if (ledger == null || chartOfAccounts == null) {
                AlertBox.showError(parentWindow,
                        "Ledger or Chart of Accounts not available for the current company.");
                return;
            }

            File f = ReportService.generate(ctx, ledger, chartOfAccounts);
            AlertBox.showInfo(parentWindow, "Trial Balance saved to: " + f.getAbsolutePath());

        } catch (IOException ex) { // Specifically for config saving/loading issues
            System.err.println("IO Error related to report configuration: " + ex.getMessage());
            ex.printStackTrace(); // Consider more robust logging
            AlertBox.showError(parentWindow,
                    "IO Error with report configuration: " + ex.getMessage());
        } catch (Exception ex) { // General catch for other errors
            System.err.println("Error during Trial Balance generation: " + ex.getMessage());
            ex.printStackTrace(); // Consider more robust logging
            AlertBox.showError(parentWindow,
                    "Failed to generate Trial Balance: " + ex.getMessage());
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
		// TODO Auto-generated method stub
		// This method would be called if this action were triggered by a Swing component.
	}
}
