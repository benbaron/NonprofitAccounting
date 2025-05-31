
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

public class GenerateBalanceSheetAction implements EventHandler<ActionEvent> {

    // private static final long serialVersionUID = 1L; // Removed
    private final String reportType = "balance_sheet";
    private final ReportService reportService; // Retained if needed

    public GenerateBalanceSheetAction(ReportService reportService) {
        // super("Generate Balance Sheet"); // Removed
        this.reportService = reportService;
    }

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

            List<Fund> availableFunds = new ArrayList<>(); // No source for funds yet

            Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
                    parentWindow,
                    "Balance Sheet Criteria",
                    availableFunds,
                    null, // chartOfAccounts not needed for this specific call variant
                    DateSelectionMode.SINGLE_DATE,
                    true, // Show fund selector
                    false, // Do not show account selector for balance sheet
                    null // No initial configuration
            );

            if (!criteriaOpt.isPresent()) {
                return; // User cancelled
            }

            ReportCriteria criteria = criteriaOpt.get();
            LocalDate endDate = criteria.getEndDate();
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
                            criteria.getStartDate(), // Will be null for SINGLE_DATE mode from dialog
                            endDate,
                            selectedFundIds);
                    newConfig.setOutputFormat("xlsx");

                    try {
                        List<ReportConfiguration> allConfigs =
                                configService.loadConfigurations(companyDir);
                        allConfigs.add(newConfig);
                        configService.saveConfigurations(allConfigs, companyDir);
                        AlertBox.showInfo(parentWindow,
                                "Report configuration '" + configNameToSave + "' saved.");
                    } catch (IOException ex) {
                        System.err.println("Error saving report configuration: " + ex.getMessage());
                        ex.printStackTrace();
                        AlertBox.showError(parentWindow,
                                "Error saving report configuration: " + ex.getMessage());
                    }

                } else {
                    AlertBox.showInfo(parentWindow, // Changed from WARNING to INFO
                            "Could not determine company directory. Configuration not saved.");
                }
            }

            ReportContext ctx = new ReportContext();
            ctx.setReportType(this.reportType);
            ctx.setStartDate(criteria.getStartDate()); // Pass along, even if null for BS
            ctx.setEndDate(endDate);
            ctx.setFundIds(selectedFundIds);
            ctx.setOutputFormat("xlsx");

            // currentCompany already fetched
            // No need to check currentCompany == null again, already done above

            Ledger ledger = currentCompany.getLedger();
            ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();

            if (ledger == null || chartOfAccounts == null) {
                AlertBox.showError(parentWindow,
                        "Ledger or Chart of Accounts not available for the current company.");
                return;
            }

            File f = ReportService.generate(ctx, ledger, chartOfAccounts); // Assuming ReportService is static or instance is available
            AlertBox.showInfo(parentWindow, "Balance Sheet saved to: " + f.getAbsolutePath());

        } catch (IOException ex) { // Catch IOException from config saving
            System.err.println("IO Error related to report configuration: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow,
                    "IO Error with report configuration: " + ex.getMessage());
        } catch (Exception ex) {
            // Log to console for debugging by developer
            System.err.println("Error during Balance Sheet generation: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow,
                    "Failed to generate Balance Sheet: " + ex.getMessage());
        }
    }
}
