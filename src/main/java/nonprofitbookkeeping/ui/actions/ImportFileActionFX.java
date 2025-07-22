
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
import nonprofitbookkeeping.service.ExcelLedgerImportService;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.model.impex.ExcelLedgerRow;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.ChartOfAccounts;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
		
		ExtensionFilter ofxFilter = new ExtensionFilter("OFX/QFX files", "*.ofx", "*.qfx");
		ExtensionFilter qifFilter = new ExtensionFilter("QIF files", "*.qif");
		ExtensionFilter xlsxFilter = new ExtensionFilter("Excel files", "*.xlsx");
		ExtensionFilter allFilter = new ExtensionFilter("All files", "*.*");
		
		fileChooser.getExtensionFilters().addAll(ofxFilter, qifFilter, xlsxFilter, allFilter);
		fileChooser.setSelectedExtensionFilter(ofxFilter);
		
		File selectedFile = fileChooser.showOpenDialog(this.ownerStage);
		
		if (selectedFile == null)
		{
			return; // User cancelled
		}
		
		List<String> formatOptions = new ArrayList<>();
		formatOptions.add("OFX/QFX");
		formatOptions.add("QIF");
		formatOptions.add("Excel (.xlsx)");
		
		ChoiceDialog<String> formatDialog = new ChoiceDialog<>(formatOptions.get(0), formatOptions);
		formatDialog.initOwner(this.ownerStage);
		formatDialog.setTitle("Import Format");
		formatDialog.setHeaderText("Select the file format to import as:");
		Optional<String> formatOpt = formatDialog.showAndWait();
		
		if (formatOpt.isEmpty())
		{
			return; // user cancelled
		}
		
		String chosenFormat = formatOpt.get();
		
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
			Alert alert =
				new Alert(AlertType.ERROR, "Chart of Accounts is empty. Cannot import file.");
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
			return;
		}
		
		List<String> accountNames = new ArrayList<>();
		
		for (Account a : company.getChartOfAccounts().getAccounts())
		{
			accountNames.add(a.getName());
		}
		
		ChoiceDialog<String> acctDialog = new ChoiceDialog<>(accountNames.get(0), accountNames);
		acctDialog.initOwner(this.ownerStage);
		acctDialog.setTitle("Choose Account");
		acctDialog.setHeaderText("Select account for imported transactions:");
		Optional<String> acctNameOpt = acctDialog.showAndWait();
		
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
		
		List<AccountingTransaction> imported = new ArrayList<>();
		
		// Use the excel file importer
		if ("Excel (.xlsx)".equals(chosenFormat))
		{
			
			try
			{
				List<ExcelLedgerRow> rows =
					ExcelLedgerImportService.importSpreadsheet(selectedFile);
				imported.addAll(convertExcelRows(rows, account, company.getChartOfAccounts()));
			}
			catch (IOException e)
			{
				Alert alert =
					new Alert(AlertType.ERROR, "Error reading Excel file: " + e.getMessage());
				alert.initOwner(this.ownerStage);
				alert.showAndWait();
				return;
			}
			
		}
		else
		{
			imported = FileImportService.importOFXorQIFFile(selectedFile, account,
				company.getChartOfAccounts(), company.getLedger());
		}
		
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
	
	/**
	 * Converts rows read from {@link ExcelLedgerImportService} into simple
	 * {@link AccountingTransaction} instances posted to the chosen account
	 * and the "needs categorization" suspense account.
	 */
	private static List<AccountingTransaction> convertExcelRows(List<ExcelLedgerRow> rows,
																Account targetAccount,
																ChartOfAccounts chartOfAccounts)
	{
		List<AccountingTransaction> results = new ArrayList<>();
		
		if (rows == null || rows.isEmpty())
		{
			return results;
		}
		
		Account suspense =
			chartOfAccounts.getAccount(FileImportService.NEEDS_CATEGORIZATION_ACCOUNT_NUMBER);
		
		if (suspense == null)
		{
			throw new IllegalArgumentException("'Needs Categorization' account not found");
		}
		
		for (ExcelLedgerRow row : rows)
		{
			
			if (row == null || row.getAllocations() == null)
			{
				continue;
			}
			
			String memo = row.getMemoNotes();
			
			if (memo == null || memo.isBlank())
			{
				memo = row.getToFrom();
			}
			
			for (ExcelLedgerRow.Allocation alloc : row.getAllocations())
			{
				
				if (alloc.getAmount() == null)
				{
					continue;
				}
				
				Set<AccountingEntry> entries = new HashSet<>();
				AccountSide targetSide;
				AccountSide suspenseSide;
				
				if (targetAccount.getIncreaseSide() == AccountSide.DEBIT)
				{
					
					if (alloc.getAmount().compareTo(java.math.BigDecimal.ZERO) >= 0)
					{
						targetSide = AccountSide.DEBIT;
						suspenseSide = AccountSide.CREDIT;
					}
					else
					{
						targetSide = AccountSide.CREDIT;
						suspenseSide = AccountSide.DEBIT;
					}
					
				}
				else
				{
					
					if (alloc.getAmount().compareTo(java.math.BigDecimal.ZERO) >= 0)
					{
						targetSide = AccountSide.CREDIT;
						suspenseSide = AccountSide.DEBIT;
					}
					else
					{
						targetSide = AccountSide.DEBIT;
						suspenseSide = AccountSide.CREDIT;
					}
					
				}
				
				java.math.BigDecimal amt = alloc.getAmount().abs();
				entries.add(new AccountingEntry(amt, targetAccount.getAccountNumber(), targetSide,
					targetAccount.getName()));
				entries.add(new AccountingEntry(amt, suspense.getAccountNumber(), suspenseSide,
					suspense.getName()));
				
				AccountingTransaction tx = new AccountingTransaction(targetAccount, entries,
					new HashMap<>(), Instant.now().toEpochMilli());
				
				if (row.getDate() != null)
				{
					tx.setDate(row.getDate().toString());
				}
				
				tx.setMemo(memo);
				results.add(tx);
			}
			
		}
		
		return results;
	}
	
	
}
