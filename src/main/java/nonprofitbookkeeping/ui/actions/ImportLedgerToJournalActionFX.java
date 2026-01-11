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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX action that runs the full ledger import pipeline and persists
 * transactions into the application's journal tables.
 */
public class ImportLedgerToJournalActionFX implements EventHandler<ActionEvent>
{
	private static final Logger LOGGER =
		Logger.getLogger(ImportLedgerToJournalActionFX.class.getName());

	private final Stage owner;

	public ImportLedgerToJournalActionFX(Stage owner)
	{
		this.owner = owner;
	}

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

		runImport(chartMapFile.toPath(), workbookFile.toPath(), sheetName.get());
	}

	private File promptForWorkbook()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Ledger Workbook");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx", "*.xlsm"));
		return chooser.showOpenDialog(this.owner);
	}

	private File promptForChartMap()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Chart Translation Map");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("JSON", "*.json"));
		return chooser.showOpenDialog(this.owner);
	}

	private Optional<String> promptForSheetName()
	{
		TextInputDialog dialog = new TextInputDialog("Ledger_Q1");
		if (this.owner != null)
		{
			dialog.initOwner(this.owner);
		}
		dialog.setTitle("Sheet Name");
		dialog.setHeaderText("Enter the sheet name to import (e.g., Ledger_Q1)");
		dialog.setContentText("Sheet name:");
		return dialog.showAndWait().filter(name -> !name.isBlank());
	}

	private void runImport(Path chartMapPath, Path workbookPath, String sheetName)
	{
		LedgerPersistenceGateway gateway = new JournalLedgerPersistenceGateway();
		LedgerImportService importService = new LedgerImportService(gateway);

		try
		{
			List<AccountingTransaction> saved =
				importService.importAndPersist(chartMapPath, workbookPath, sheetName);
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
			LOGGER.log(Level.WARNING, "Failed to import ledger", ex);
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
