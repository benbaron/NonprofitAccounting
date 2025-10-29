
package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.ChartOfAccountsIOService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles exporting the current company's Chart of Accounts to an XLSX file.
 */
public class ExportCoaXlsxActionFX implements EventHandler<ActionEvent>
{
	
	private static final Logger logger = Logger.getLogger(ExportCoaXlsxActionFX.class.getName());
	
	private final Stage ownerStage;
	
	public ExportCoaXlsxActionFX(Stage ownerStage)
	{
		this.ownerStage = ownerStage;
	}
	
	@Override public void handle(ActionEvent event)
	{
		Company currentCompany = CurrentCompany.getCompany();
		
		if (currentCompany == null)
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Export Warning");
			alert.setHeaderText(null);
			alert.setContentText(
				"No company is currently open. Please open a company to export its Chart of Accounts.");
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
			logger.log(Level.WARNING, "Export COA action triggered but no company is open.");
			return;
		}
		
		ChartOfAccounts coa = currentCompany.getChartOfAccounts();
		
		if (coa == null)
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Export Warning");
			alert.setHeaderText(null);
			alert
				.setContentText("The current company does not have a Chart of Accounts to export.");
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
			logger.log(Level.WARNING, "Export COA action triggered but company '" +
				currentCompany.getCompanyProfile() + "' has no COA.");
			return;
		}
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export Chart of Accounts to XLSX");
		FileChooser.ExtensionFilter xlsxFilter =
			new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
		fileChooser.getExtensionFilters().add(xlsxFilter);
		fileChooser.setSelectedExtensionFilter(xlsxFilter);
		
		String suggestedName = "chart_of_accounts.xlsx";
		
		if (currentCompany.getCompanyProfile() != null)
		{
			suggestedName = currentCompany.getCompanyProfile().getCompanyFileName();
		}
		
		fileChooser.setInitialFileName(suggestedName);
		
		File selectedFile = fileChooser.showSaveDialog(this.ownerStage);
		
		if (selectedFile != null)
		{
			try
			{
				ChartOfAccountsIOService.exportToXlsx(coa, selectedFile.toPath());
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Export Successful");
				alert.setHeaderText(null);
				alert.setContentText(
					"Chart of Accounts exported successfully to " + selectedFile.getAbsolutePath());
				alert.initOwner(this.ownerStage);
				alert.showAndWait();
				logger.log(Level.INFO,
					"Chart of Accounts exported successfully to " + selectedFile.getAbsolutePath());
				
			}
			catch (IOException e)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Export Error");
				alert.setHeaderText("Error Exporting Chart of Accounts");
				alert.setContentText(
					"An error occurred while exporting the Chart of Accounts: " + e.getMessage());
				alert.initOwner(this.ownerStage);
				alert.showAndWait();
				logger.log(Level.SEVERE,
					"Error exporting Chart of Accounts to " + selectedFile.getAbsolutePath(), e);
			}
			
		}
		else
		{
			logger.log(Level.INFO, "Chart of Accounts export was cancelled by the user.");
		}
		
	}
	
}
