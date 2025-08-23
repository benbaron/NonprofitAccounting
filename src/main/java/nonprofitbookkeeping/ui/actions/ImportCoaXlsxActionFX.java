
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
 * Handles the action of importing a Chart of Accounts from an XLSX file.
 */
public class ImportCoaXlsxActionFX implements EventHandler<ActionEvent>
{
	
	private static final Logger logger = Logger.getLogger(ImportCoaXlsxActionFX.class.getName());
	
	private final Stage ownerStage;
	
	public ImportCoaXlsxActionFX(Stage ownerStage)
	{
		this.ownerStage = ownerStage;
	}
	
	@Override public void handle(ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Chart of Accounts from XLSX");
		FileChooser.ExtensionFilter xlsxFilter =
			new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
		fileChooser.getExtensionFilters().add(xlsxFilter);
		fileChooser.setSelectedExtensionFilter(xlsxFilter);
		
		File selectedFile = fileChooser.showOpenDialog(this.ownerStage);
		
		if (selectedFile != null)
		{
			try
			{
				ChartOfAccounts importedCOA = ChartOfAccountsIOService.importFromXlsx(selectedFile.toPath());
				Company currentCompany = CurrentCompany.getCompany();
				
                                if (currentCompany != null)
                                {
                                        currentCompany.setChartOfAccounts(importedCOA);
                                        CurrentCompany.flushToDatabase();
                                        CurrentCompany.markCompanyOpen();
                                        Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Import Successful");
					alert.setHeaderText(null);
					alert.setContentText("Chart of Accounts imported successfully.");
					alert.initOwner(this.ownerStage);
					alert.showAndWait();
					logger.log(Level.INFO,
						"Chart of Accounts imported successfully from " + selectedFile.getName());
				}
				else
				{
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Import Warning");
					alert.setHeaderText(null);
					alert.setContentText(
						"No company is currently open. The imported Chart of Accounts has not been assigned.");
					alert.initOwner(this.ownerStage);
					alert.showAndWait();
					logger.log(Level.WARNING,
						"Chart of Accounts imported but no company was open.");
				}
				
			}
			catch (IOException e)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Import Error");
				alert.setHeaderText("Error Importing Chart of Accounts");
				alert.setContentText(
					"An error occurred while importing the Chart of Accounts: " + e.getMessage());
				alert.initOwner(this.ownerStage);
				alert.showAndWait();
				logger.log(Level.SEVERE,
					"Error importing Chart of Accounts from " + selectedFile.getName(), e);
			}
			catch (Exception e)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Import Error");
				alert.setHeaderText("Unexpected Error Importing Chart of Accounts");
				alert.setContentText("An unexpected error occurred: " + e.getMessage());
				alert.initOwner(this.ownerStage);
				alert.showAndWait();
				logger.log(Level.SEVERE,
					"Unexpected error importing Chart of Accounts from " + selectedFile.getName(),
					e);
			}
			
		}
		else
		{
			logger.log(Level.INFO, "Chart of Accounts import was cancelled by the user.");
		}
		
	}
	
}
