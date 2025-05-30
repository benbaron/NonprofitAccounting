
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

public class GenerateCashFlowStatementAction implements EventHandler<ActionEvent> {

    // private static final long serialVersionUID = 1L; // Removed
    private final String reportType = "cash_flow_statement";
    private final ReportService reportService; // Retained

    public GenerateCashFlowStatementAction(ReportService reportService) {
        // super("Generate Cash Flow Statement"); // Removed
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
            // ChartOfAccounts is not passed to this ReportCriteriaDialog variant.
            // Account selector is not shown.

            Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
                    parentWindow,
                    "Cash Flow Statement Criteria",
                    availableFunds,
                    null, // chartOfAccounts
                    DateSelectionMode.DATE_RANGE_MANDATORY_START,
                    true, // showFundSelector
                    false, // showAccountSelector
                    null // initialConfig
            );

            if (!criteriaOpt.isPresent()) {
                return; // User cancelled
            }

            ReportCriteria criteria = criteriaOpt.get();
            LocalDate startDate = criteria.getStartDate();
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
                            startDate,
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
            ctx.setStartDate(startDate);
            ctx.setEndDate(endDate);
            ctx.setFundIds(selectedFundIds);
            ctx.setOutputFormat("xlsx");

            // currentCompany already fetched
            // No need to check currentCompany == null again

            Ledger ledger = currentCompany.getLedger();
            ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();

            if (ledger == null || chartOfAccounts == null) {
                AlertBox.showError(parentWindow,
                        "Ledger or Chart of Accounts not available for the current company.");
                return;
            }

            File f = ReportService.generate(ctx, ledger, chartOfAccounts); // Assuming ReportService is static or instance available
            AlertBox.showInfo(parentWindow,
                    "Cash Flow Statement saved to: " + f.getAbsolutePath());

        } catch (IOException ex) { // Catch IOException from config saving
            System.err.println("IO Error related to report configuration: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow,
                    "IO Error with report configuration: " + ex.getMessage());
        } catch (Exception ex) {
            // Log to console for debugging by developer
            System.err.println("Error during Cash Flow Statement generation: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow,
                    "Failed to generate Cash Flow Statement: " + ex.getMessage());
        }
    }
}
