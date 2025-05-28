package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
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
import nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog; 

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList; // Added
import java.util.List; // Added
import java.util.Optional; 

public class GenerateCashFlowStatementAction extends AbstractAction {

    private static final long serialVersionUID = 1L; 
    private final String reportType = "cash_flow_statement"; // Added field

    public GenerateCashFlowStatementAction(ReportService reportService) {
        super("Generate Cash Flow Statement");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Company currentCompany = CurrentCompany.getCompany();
            if (currentCompany == null) {
                JOptionPane.showMessageDialog(null, "No company is currently open.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<Fund> availableFunds = new ArrayList<>(); // No source for funds yet

            Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
                null, 
                "Cash Flow Statement Criteria",
                availableFunds,
                ReportCriteriaDialog.DateSelectionMode.DATE_RANGE_MANDATORY_START,
                true // Show fund selector
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
                        selectedFundIds
                    );
                    newConfig.setOutputFormat("xlsx");

                    try {
                        List<ReportConfiguration> allConfigs = configService.loadConfigurations(companyDir);
                        allConfigs.add(newConfig);
                        configService.saveConfigurations(allConfigs, companyDir);
                        JOptionPane.showMessageDialog(null, "Report configuration '" + configNameToSave + "' saved.", "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        System.err.println("Error saving report configuration: " + ex.getMessage());
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error saving report configuration: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Could not determine company directory. Configuration not saved.", "Save Error", JOptionPane.WARNING_MESSAGE);
                }
            }
            
            ReportContext ctx = new ReportContext();
            ctx.setReportType(this.reportType);
            ctx.setStartDate(startDate);
            ctx.setEndDate(endDate);
            ctx.setFundIds(selectedFundIds);
            ctx.setOutputFormat("xlsx");

            // currentCompany already fetched
            if (currentCompany == null) {
                JOptionPane.showMessageDialog(null, "No company is currently open.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Ledger ledger = currentCompany.getLedger();
            ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();

            if (ledger == null || chartOfAccounts == null) {
                JOptionPane.showMessageDialog(null, "Ledger or Chart of Accounts not available for the current company.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File f = ReportService.generate(ctx, ledger, chartOfAccounts);
            JOptionPane.showMessageDialog(null, "Cash Flow Statement saved to: " + f.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            // Log to console for debugging by developer
            System.err.println("Error during Cash Flow Statement generation: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to generate Cash Flow Statement: " + ex.getMessage(), "Generation Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) { // Catch IOException from config saving
            System.err.println("IO Error related to report configuration: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "IO Error with report configuration: " + ex.getMessage(), "Configuration IO Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
