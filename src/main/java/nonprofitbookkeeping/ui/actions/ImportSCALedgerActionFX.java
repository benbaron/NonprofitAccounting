
package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.service.scaledger.JournalLedgerPersistenceGateway;
import nonprofitbookkeeping.ui.actions.scaledger.LedgerImportService;
import nonprofitbookkeeping.ui.actions.scaledger.LedgerPersistenceGateway;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX action that runs the full ledger import pipeline and persists
 * transactions into the application's journal tables.
 */
public class ImportSCALedgerActionFX implements EventHandler<ActionEvent>
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(ImportSCALedgerActionFX.class);
	
	private final Stage owner;
	
	public ImportSCALedgerActionFX(Stage owner)
	{
		this.owner = owner;
		
	}
	
	/**
	 * Override @see javafx.event.EventHandler#handle(javafx.event.Event) 
	 */
	@Override
	public void handle(ActionEvent event)
	{
		File workbookFile = promptForWorkbook();
		
		if (workbookFile == null)
		{
			return;
		}
		
		File chartMapFile = promptForChartMap();
		
		if (chartMapFile == null)
		{
			return;
		}
		
		Optional<String> sheetName = promptForSheetName();
		
		if (sheetName.isEmpty())
		{
			return;
		}
		
		runImport(chartMapFile.toPath(), workbookFile.toPath(),
			sheetName.get());
		
	}
	
	/**
	 * Prompt for workbook.
	 *
	 * @return the file
	 */
	private File promptForWorkbook()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Ledger Workbook");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx",
				"*.xlsm"));
		return chooser.showOpenDialog(this.owner);
		
	}
	
	/**
	 * Prompt for chart map.
	 *
	 * @return the file
	 */
	private File promptForChartMap()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Chart Translation Map");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("JSON", "*.json"));
		return chooser.showOpenDialog(this.owner);
		
	}
	
	/**
	 * Prompt for sheet name.
	 *
	 * @return the optional
	 */
	private Optional<String> promptForSheetName()
	{
		TextInputDialog dialog = new TextInputDialog("Ledger_Q1");
		
		if (this.owner != null)
		{
			dialog.initOwner(this.owner);
		}
		
		dialog.setTitle("Sheet Name");
		dialog
			.setHeaderText("Enter the sheet name to import (e.g., Ledger_Q1)");
		dialog.setContentText("Sheet name:");
		return dialog.showAndWait().filter(name -> !name.isBlank());
		
	}
	
	/**
	 * Run import.
	 *
	 * @param chartMapPath the chart map path
	 * @param workbookPath the workbook path
	 * @param sheetName the sheet name
	 */
	private void runImport(Path chartMapPath, Path workbookPath,
		String sheetName)
	{
		LedgerPersistenceGateway gateway =
			new JournalLedgerPersistenceGateway();
		LedgerImportService importService = new LedgerImportService(gateway);
		
		try
		{
			List<AccountingTransaction> saved =
				importService.importAndPersist(chartMapPath, workbookPath,
					sheetName);
			
			Alert alert = new Alert(Alert.AlertType.INFORMATION,
				String.format("Imported %d transactions from %s", saved.size(),
					sheetName),
				ButtonType.OK);
			
			if (this.owner != null)
			{
				alert.initOwner(this.owner);
			}
			
			alert.showAndWait();
		}
		catch (IOException ex)
		{
			LOGGER.warn("Failed to import ledger", ex);
			Alert alert = new Alert(Alert.AlertType.ERROR,
				"Failed to import ledger: " + ex.getMessage(), ButtonType.OK);
			
			if (this.owner != null)
			{
				alert.initOwner(this.owner);
			}
			
			alert.showAndWait();
		}
		
	}
	
}
