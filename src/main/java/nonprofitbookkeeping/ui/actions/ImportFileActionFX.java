
package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.FileImportService;
import nonprofitbookkeeping.service.ReconciliationService;
import java.io.File;

/**
 * Handles the action of importing a financial statement file in a JavaFX
 * application.  The action prompts the user to choose a file (OFX, QFX or QIF),
 * asks which account the transactions should be posted to, and then delegates to
 * {@link FileImportService} to parse the file.  Imported transactions are added
 * to the current company's ledger and queued for reconciliation.
 *
 * <p>This class implements {@link EventHandler} for {@link ActionEvent} and
 * requires an owner {@link Stage} to properly manage dialogs such as the
 * {@link FileChooser}.</p>
 */
public class ImportFileActionFX implements EventHandler<ActionEvent>
{
	
	/** The owner Stage for any dialogs created by this action. */
	private final Stage ownerStage;
	
	/**
	 * Constructs a new {@code ImportFileActionFX}.
	 *
	 * @param ownerStage The primary {@link Stage} of the JavaFX application. This stage will act
	 *                   as the owner for any dialogs (e.g., FileChooser, Alert) displayed by this action.
	 *                   Must not be null.
	 * @throws IllegalArgumentException if {@code ownerStage} is null.
	 */
	public ImportFileActionFX(Stage ownerStage)
	{
		
		if (ownerStage == null)
		{
			throw new IllegalArgumentException("Owner stage cannot be null.");
		}
		
		this.ownerStage = ownerStage;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the action event, typically triggered by a menu item or button click,
	 * to initiate the file import process. This method performs the following steps:
	 * <ol>
	 *   <li>Creates and configures a {@link FileChooser} with the title "Import File" and
	 *       relevant file extension filters (OFX, QFX, QIF, All Files).</li>
	 *   <li>Displays an "open" dialog to the user, owned by the {@code ownerStage}.</li>
	 *   <li>If the user selects a file:
	 *     <ul>
	 *       <li>Determines the likely file format based on the selected extension filter or filename extension.</li>
	 *       <li>Displays an {@link Alert} dialog confirming the selected file and its inferred format,
	 *           noting that the actual import logic is pending implementation.</li>
	 *     </ul>
	 *   </li>
	 *   <li>If the user cancels the dialog, no further action is taken.</li>
	 * </ol>
	 * </p>
	 * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
	 */
        @Override public void handle(ActionEvent event)
        {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Import File");

                ExtensionFilter ofxFilter = new ExtensionFilter("OFX files (*.ofx)", "*.ofx");
                ExtensionFilter qfxFilter = new ExtensionFilter("QFX files (*.qfx)", "*.qfx");
                ExtensionFilter qifFilter = new ExtensionFilter("QIF files (*.qif)", "*.qif");
                ExtensionFilter allFilter = new ExtensionFilter("All files (*.*)", "*.*");

                fileChooser.getExtensionFilters().addAll(ofxFilter, qfxFilter, qifFilter, allFilter);
                fileChooser.setSelectedExtensionFilter(ofxFilter);

                File selectedFile = fileChooser.showOpenDialog(this.ownerStage);

                if (selectedFile == null)
                {
                        return; // User cancelled
                }

                Company company = CurrentCompany.getCompany();

                if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
                {
                        Alert alert = new Alert(AlertType.ERROR, "No company open to import into.");
                        alert.initOwner(this.ownerStage);
                        alert.showAndWait();
                        return;
                }

                if (company.getChartOfAccounts().getAccounts().isEmpty())
                {
                        Alert alert = new Alert(AlertType.ERROR, "Chart of Accounts is empty. Cannot import file.");
                        alert.initOwner(this.ownerStage);
                        alert.showAndWait();
                        return;
                }

                java.util.List<String> accountNames = new java.util.ArrayList<>();
                for (Account a : company.getChartOfAccounts().getAccounts())
                {
                        accountNames.add(a.getName());
                }

                ChoiceDialog<String> acctDialog = new ChoiceDialog<>(accountNames.get(0), accountNames);
                acctDialog.initOwner(this.ownerStage);
                acctDialog.setTitle("Choose Account");
                acctDialog.setHeaderText("Select account for imported transactions:");
                java.util.Optional<String> acctNameOpt = acctDialog.showAndWait();

                if (acctNameOpt.isEmpty())
                {
                        return; // user cancelled
                }

                String acctName = acctNameOpt.get();
                Account account = company.getChartOfAccounts().getAccountByName(acctName);

                if (account == null)
                {
                        Alert alert = new Alert(AlertType.ERROR, "Account not found: " + acctName);
                        alert.initOwner(this.ownerStage);
                        alert.showAndWait();
                        return;
                }

                java.util.List<AccountingTransaction> imported = FileImportService.importFile(
                                selectedFile, account, company.getChartOfAccounts(), company.getLedger());

                if (imported.isEmpty())
                {
                        Alert alert = new Alert(AlertType.INFORMATION,
                                "No transactions imported from " + selectedFile.getName());
                        alert.initOwner(this.ownerStage);
                        alert.showAndWait();
                        return;
                }

                for (AccountingTransaction at : imported)
                {
                        company.getLedger().getJournal().addTransaction(at);
                        new ReconciliationService().addTransactionToReconcile(at);
                }

                try
                {
                        CurrentCompany.persist();
                }
                catch (Exception ex)
                {
                        ex.printStackTrace();
                }

                Alert alert = new Alert(AlertType.INFORMATION,
                        "Imported " + imported.size() + " transactions from " + selectedFile.getName());
                alert.initOwner(this.ownerStage);
                alert.showAndWait();
        }
	
}
