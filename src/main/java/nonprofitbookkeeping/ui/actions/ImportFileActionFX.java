
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
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.FileImportService;
import nonprofitbookkeeping.service.ExcelLedgerImportService;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.model.impex.ExcelLedgerRow;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.core.AccountingTransactionBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	@Override
	public void handle(ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import File");
		
		ExtensionFilter ofxFilter =
			new ExtensionFilter("OFX/QFX files", "*.ofx", "*.qfx");
		ExtensionFilter qifFilter = new ExtensionFilter("QIF files", "*.qif");
		ExtensionFilter xlsxFilter =
			new ExtensionFilter("Excel files", "*.xlsx");
		ExtensionFilter allFilter = new ExtensionFilter("All files", "*.*");
		
		fileChooser.getExtensionFilters().addAll(ofxFilter, qifFilter,
			xlsxFilter, allFilter);
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
		
		ChoiceDialog<String> formatDialog =
			new ChoiceDialog<>(formatOptions.get(0), formatOptions);
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
		
		if (company == null || company.getLedger() == null ||
			company.getChartOfAccounts() == null)
		{
			Alert alert =
				new Alert(AlertType.ERROR, "No company open to import into.");
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
			return;
		}
		
		if (company.getChartOfAccounts().getAccounts().isEmpty())
		{
			Alert alert =
				new Alert(AlertType.ERROR,
					"Chart of Accounts is empty. Cannot import file.");
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
			return;
		}
		
		List<String> accountNames = new ArrayList<>();
		
		for (Account a : company.getChartOfAccounts().getAccounts())
		{
			accountNames.add(a.getName());
		}
		
		ChoiceDialog<String> acctDialog =
			new ChoiceDialog<>(accountNames.get(0), accountNames);
		acctDialog.initOwner(this.ownerStage);
		acctDialog.setTitle("Choose Account");
		acctDialog.setHeaderText("Select account for imported transactions:");
		Optional<String> acctNameOpt = acctDialog.showAndWait();
		
		if (acctNameOpt.isEmpty())
		{
			return; // user cancelled
		}
		
		String acctName = acctNameOpt.get();
		
		// Search entire chart of accounts case-insensitively
		Account account =
			FileImportService
				.findAccountIgnoreCase(company.getChartOfAccounts(), acctName);
		
		if (account == null)
		{
			Alert alert =
				new Alert(AlertType.ERROR, "Account not found: " + acctName);
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
			return;
		}
		
		List<AccountingTransaction> imported = new ArrayList<>();
		
		// Use the excel file importer
		if ("Excel (.xlsx)".equals(chosenFormat))
		{
			importXlsx(selectedFile, imported);
		}
		else
		{
			imported = FileImportService.importOFXorQIFFile(selectedFile,
				account,
				company.getChartOfAccounts(),
				company.getLedger());
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
			Alert alert = new Alert(AlertType.INFORMATION,
				"Imported " + imported.size() + " transactions from " +
					selectedFile.getName());
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
		}
		catch (IOException ex)
		{
			Alert alert = new Alert(AlertType.ERROR,
				"Failed to save imported transactions: " + ex.getMessage());
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
		}
		
	}
	
	/**
	 * Import XLSX for transactions
	 *
	 * @param selectedFile the Excel file to import
	 * @param imported list where transactions will be added
	 */
	void importXlsx(File selectedFile, List<AccountingTransaction> imported)
	{
		
		try
		{
			// Map spreadsheet to rows
			List<ExcelLedgerRow> rows =
				ExcelLedgerImportService.importSpreadsheet(selectedFile);
			
			// Convert the rows
			imported.addAll(ImportFileActionFX.convertExcelRows(rows));
		}
		catch (IOException e)
		{
			Alert alert =
				new Alert(AlertType.ERROR,
					"Error reading Excel file: " + e.getMessage());
			alert.initOwner(this.ownerStage);
			alert.showAndWait();
		}
		
	}
	
	/**
	 * Converts rows read from {@link ExcelLedgerImportService} into
	 * {@link AccountingTransaction} instances. Allocation account names are
	 * matched against the chart of accounts and missing accounts prompt the
	 * user to add or ignore them.
	     */
	
	static
		List<AccountingTransaction> convertExcelRows(List<ExcelLedgerRow> rows)
	{
		List<AccountingTransaction> results = new ArrayList<>();
		
		if (rows == null)
		{
			return results;
		}
		
		Company company = CurrentCompany.getCompany();
		ChartOfAccounts chart =
			(company != null) ? company.getChartOfAccounts() : null;
		
		for (ExcelLedgerRow row : rows)
		{
			
			if (row == null || row.getAllocations() == null ||
				row.getAllocations().isEmpty())
			{
				continue;
			}
			
			AccountingTransactionBuilder builder =
				AccountingTransactionBuilder.create();
			int entryCount = 0;
			
			for (ExcelLedgerRow.Allocation alloc : row.getAllocations())
			{
				
				if (alloc == null || alloc.getAmount() == null ||
					alloc.getAmount().compareTo(BigDecimal.ZERO) == 0)
				{
					continue;
				}
				
				List<String> names = new ArrayList<>();
				
				if (alloc.getAssetLiabilityAccount() != null &&
					!alloc.getAssetLiabilityAccount().isBlank())
				{
					names.add(alloc.getAssetLiabilityAccount());
				}
				
				if (alloc.getIncomeCategory() != null &&
					!alloc.getIncomeCategory().isBlank())
				{
					names.add(alloc.getIncomeCategory());
				}
				
				if (alloc.getExpenseCategory() != null &&
					!alloc.getExpenseCategory().isBlank())
				{
					names.add(alloc.getExpenseCategory());
				}
				
				if (names.size() != 2)
				{
					continue;
				}
				
				Account acct1 = FileImportService.findAccountIgnoreCase(chart,
					names.get(0));
				Account acct2 = FileImportService.findAccountIgnoreCase(chart,
					names.get(1));
				
				if (acct1 == null || acct2 == null)
				{
					continue;
				}
				
				BigDecimal amt = alloc.getAmount();
				BigDecimal absAmt = amt.abs();
				
				AccountSide side1 =
					(amt.signum() >= 0) ? acct1.getIncreaseSide() :
						(acct1.getIncreaseSide() == AccountSide.DEBIT ?
							AccountSide.CREDIT :
							AccountSide.DEBIT);
				AccountSide side2 =
					(side1 == AccountSide.DEBIT) ? AccountSide.CREDIT :
						AccountSide.DEBIT;
				
				if (side1 == AccountSide.DEBIT)
				{
					builder.debit(absAmt, acct1.getAccountNumber());
				}
				else
				{
					builder.credit(absAmt, acct1.getAccountNumber());
				}
				
				if (side2 == AccountSide.DEBIT)
				{
					builder.debit(absAmt, acct2.getAccountNumber());
				}
				else
				{
					builder.credit(absAmt, acct2.getAccountNumber());
				}
				
				entryCount += 2;
			}
			
			if (entryCount < 2)
			{
				continue;
			}
			
			AccountingTransaction tx = builder.build();
			
			if (row.getDate() != null)
			{
				tx.setDate(row.getDate().toString());
			}
			
			String memo =
				(row.getMemoNotes() != null && !row.getMemoNotes().isBlank()) ?
					row.getMemoNotes() : row.getToFrom();
			
			if (memo != null)
			{
				tx.setMemo(memo);
			}
			
			tx.setToFrom(row.getToFrom());
			tx.setCheckNumber(row.getCheckNumber());
			tx.setClearBank(row.getClearBank());
			tx.setBudgetTracking(row.getBudgetTracking());
			
			results.add(tx);
		}
		
		return results;
		
	}
	
}
