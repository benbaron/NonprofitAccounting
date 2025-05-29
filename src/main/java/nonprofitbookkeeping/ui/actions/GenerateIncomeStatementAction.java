
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
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
import nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList; // Added
import java.util.List; // Added
import java.util.Optional;

public class GenerateIncomeStatementAction extends AbstractAction
{
	
	private static final long serialVersionUID = -6004544138078222664L;
	private final String reportType = "income_statement"; // Added field
	
	public GenerateIncomeStatementAction(ReportService reportService)
	{
		super("Generate Income Statement");
	}
	
	@Override public void actionPerformed(ActionEvent e)
	{
		
		try
		{
			Company currentCompany = CurrentCompany.getCompany();
			
			if (currentCompany == null)
			{
				JOptionPane.showMessageDialog(null, "No company is currently open.", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// Attempt to get available funds. Fallback to empty list.
			List<Fund> availableFunds = new ArrayList<>();
			// if (currentCompany.getChartOfAccounts() != null &&
			// currentCompany.getChartOfAccounts().getFunds() != null) {
			// availableFunds = currentCompany.getChartOfAccounts().getFunds();
			// } else if (currentCompany.getFunds() != null) {
			// availableFunds = currentCompany.getFunds();
			// }
			// Since neither Company nor ChartOfAccounts has getFunds(), availableFunds
			// remains empty.
			
			Optional<ReportCriteria> criteriaOpt = ReportCriteriaDialog.showDialog(
				null, // Or SwingUtilities.getWindowAncestor((Component) e.getSource()),
				"Income Statement Criteria",
				availableFunds,
				DateSelectionMode.DATE_RANGE_MANDATORY_START,
				true // Show fund selector
			);
			
			if (!criteriaOpt.isPresent())
			{
				return; // User cancelled
			}
			
			ReportCriteria criteria = criteriaOpt.get();
			LocalDate startDate = criteria.getStartDate();
			LocalDate endDate = criteria.getEndDate();
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
					newConfig.setOutputFormat("xlsx"); // Default or from criteria if available
					
					try
					{
						List<ReportConfiguration> allConfigs =
							configService.loadConfigurations(companyDir);
						allConfigs.add(newConfig); // Add new, no update/duplicate check for V1
						configService.saveConfigurations(allConfigs, companyDir);
						JOptionPane.showMessageDialog(null,
							"Report configuration '" + configNameToSave + "' saved.",
							"Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
					}
					catch (IOException ex)
					{
						System.err.println("Error saving report configuration: " + ex.getMessage());
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null,
							"Error saving report configuration: " + ex.getMessage(), "Save Error",
							JOptionPane.ERROR_MESSAGE);
						// Continue to run the report even if saving config failed
					}
					
				}
				else
				{
					JOptionPane.showMessageDialog(null,
						"Could not determine company directory. Configuration not saved.",
						"Save Error", JOptionPane.WARNING_MESSAGE);
				}
				
			}
			
			ReportContext ctx = new ReportContext();
			ctx.setReportType(this.reportType); // Use field
			ctx.setStartDate(startDate);
			ctx.setEndDate(endDate);
			ctx.setFundIds(selectedFundIds);
			ctx.setOutputFormat("xlsx");
			
			// currentCompany already fetched
			if (currentCompany == null)
			{
				JOptionPane.showMessageDialog(null, "No company is currently open.", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Ledger ledger = currentCompany.getLedger();
			ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();
			
			if (ledger == null || chartOfAccounts == null)
			{
				JOptionPane.showMessageDialog(null,
					"Ledger or Chart of Accounts not available for the current company.", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			File f = ReportService.generate(ctx, ledger, chartOfAccounts);
			JOptionPane.showMessageDialog(null, "Income Statement saved to: " + f.getAbsolutePath(),
				"Success", JOptionPane.INFORMATION_MESSAGE);
			
		}
		catch (IOException ex)
		{ // Catch IOException from config saving specifically if not caught within block
			System.err.println("IO Error related to report configuration: " + ex.getMessage());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"IO Error with report configuration: " + ex.getMessage(), "Configuration IO Error",
				JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex)
		{
			System.err.println("Error during Income Statement generation: " + ex.getMessage());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"Failed to generate Income Statement: " + ex.getMessage(), "Generation Error",
				JOptionPane.ERROR_MESSAGE);
		}

		
	}
	
}
