package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.ChartOfAccountsIOService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportCoaJsonActionFX implements EventHandler<ActionEvent> {

    private static final Logger logger = Logger.getLogger(ImportCoaJsonActionFX.class.getName());

    private final Stage ownerStage;

    public ImportCoaJsonActionFX(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    @Override
    public void handle(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Chart of Accounts from JSON");
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(jsonFilter);
        fileChooser.setSelectedExtensionFilter(jsonFilter);

        File selectedFile = fileChooser.showOpenDialog(ownerStage);

        if (selectedFile != null) {
            ChartOfAccountsIOService coaService = new ChartOfAccountsIOService();
            try {
                ChartOfAccounts<Account> importedCOA = coaService.importFromJson(selectedFile.toPath());
                Company currentCompany = CurrentCompany.getCompany();

                if (currentCompany != null) {
                    currentCompany.setChartOfAccounts(importedCOA);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Import Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Chart of Accounts imported successfully.");
                    alert.initOwner(ownerStage);
                    alert.showAndWait();
                    logger.log(Level.INFO, "Chart of Accounts imported successfully from " + selectedFile.getName());
                } else {
                    // This case should ideally not happen if UI enables this action only when a company is open
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Import Warning");
                    alert.setHeaderText(null);
                    alert.setContentText("No company is currently open. The imported Chart of Accounts has not been assigned.");
                    alert.initOwner(ownerStage);
                    alert.showAndWait();
                    logger.log(Level.WARNING, "Chart of Accounts imported but no company was open.");
                }

            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Import Error");
                alert.setHeaderText("Error Importing Chart of Accounts");
                alert.setContentText("An error occurred while importing the Chart of Accounts: " + e.getMessage());
                alert.initOwner(ownerStage);
                alert.showAndWait();
                logger.log(Level.SEVERE, "Error importing Chart of Accounts from " + selectedFile.getName(), e);
            } catch (Exception e) { // Catching broader exceptions from Jackson or other issues
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Import Error");
                alert.setHeaderText("Unexpected Error Importing Chart of Accounts");
                alert.setContentText("An unexpected error occurred: " + e.getMessage());
                alert.initOwner(ownerStage);
                alert.showAndWait();
                logger.log(Level.SEVERE, "Unexpected error importing Chart of Accounts from " + selectedFile.getName(), e);
            }
        } else {
            // User cancelled the dialog
            logger.log(Level.INFO, "Chart of Accounts import was cancelled by the user.");
        }
    }
}
