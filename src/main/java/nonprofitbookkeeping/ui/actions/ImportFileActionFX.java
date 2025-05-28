package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException; // Added
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger; // Added
import nonprofitbookkeeping.exception.ActionCancelledException; // Added
import nonprofitbookkeeping.exception.NoFileCreatedException; // Added
import nonprofitbookkeeping.service.FileImportService;
// AlertBox import might be unused if showFxAlert is consistently used.
// import nonprofitbookkeeping.ui.helpers.AlertBox;


public class ImportFileActionFX implements EventHandler<ActionEvent> {

    private final Stage owner; 

    private static class AccountItem {
        Account account;
        public AccountItem(Account account) { this.account = account; }
        public Account getAccount() { return account; }
        @Override public String toString() { return account.getName() + " (" + account.getAccountNumber() + ")"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AccountItem that = (AccountItem) o;
            return Objects.equals(account.getAccountNumber(), that.account.getAccountNumber());
        }
        @Override public int hashCode() { return Objects.hash(account.getAccountNumber()); }
    }

    public ImportFileActionFX(Stage owner) {
        this.owner = owner;
    }

    @Override
    public void handle(ActionEvent event) {
        Company currentCompany = CurrentCompany.getCompany();
        if (currentCompany == null) {
            showFxAlert(AlertType.ERROR, "No Company Open", "Please open or create a company first.");
            return;
        }
        ChartOfAccounts coa = currentCompany.getChartOfAccounts();
        if (coa == null || coa.getAccounts() == null || coa.getAccounts().isEmpty()) {
            showFxAlert(AlertType.ERROR, "Chart of Accounts Error", "Chart of Accounts is missing or empty.");
            return;
        }
        Ledger ledger = currentCompany.getLedger(); // Get ledger for duplicate check and saving
        if (ledger == null || ledger.getJournal() == null) { // Also check journal as it's used for adding
            showFxAlert(AlertType.ERROR, "Ledger Error", "Could not access the company ledger or journal.");
            return;
        }


        Platform.runLater(() -> { 
            List<Account> importableAccounts = coa.getAccounts().stream()
                .filter(acc -> {
                    String typeStr = acc.getAccountType();
                    if (typeStr == null) return false;
                    try {
                        AccountType type = AccountType.valueOf(typeStr.toUpperCase());
                        return type == AccountType.BANK || type == AccountType.CASH || type == AccountType.CREDITCARD;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(Account::getName))
                .collect(Collectors.toList());

            if (importableAccounts.isEmpty()) {
                showFxAlert(AlertType.WARNING, "No Importable Accounts", "No suitable bank, cash, or credit card accounts found to import into.");
                return;
            }

            Vector<AccountItem> accountItems = importableAccounts.stream()
                                                 .map(AccountItem::new)
                                                 .collect(Collectors.toCollection(Vector::new));
            
            JComboBox<AccountItem> accountComboBox = new JComboBox<>(accountItems);
            JPanel panel = new JPanel();
            panel.add(new JLabel("Select Target Account for Import:"));
            panel.add(accountComboBox);

            int result = JOptionPane.showConfirmDialog(null, panel, "Select Target Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION || accountComboBox.getSelectedItem() == null) {
                return; 
            }
            Account targetAccount = ((AccountItem) accountComboBox.getSelectedItem()).getAccount();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import Transactions File");
            chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OFX/QFX Files", "*.ofx", "*.qfx"),
                new FileChooser.ExtensionFilter("QIF Files", "*.qif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File file = chooser.showOpenDialog(this.owner);
            if (file == null) {
                return; 
            }

            try {
                // Pass ledger to importFile for duplicate checking
                List<AccountingTransaction> newTransactions = FileImportService.importFile(file, targetAccount, coa, ledger); 
                
                // The FileImportService now returns only non-duplicate transactions.
                // The old logic for counting skipped duplicates would need FileImportService to return that info,
                // or we infer it if needed (e.g., parse twice, once raw, once filtered - too complex for now).
                // For now, the log in FileImportService notes skipped duplicates.
                // The user message will focus on what was actually imported.

                if (newTransactions == null || newTransactions.isEmpty()) {
                    showFxAlert(AlertType.WARNING, "Import Result", 
                                "No new, non-duplicate transactions were imported from the file.\n" +
                                "This could be due to an empty file, unparseable content, or all transactions being potential duplicates.");
                    return;
                }

                // Save to Ledger
                for (AccountingTransaction tx : newTransactions) {
                    ledger.getJournal().addTransaction(tx);
                }

                try {
                    CurrentCompany.persist();
                    int importedCount = newTransactions.size();
                    // To report skipped duplicates, FileImportService would need to return this count.
                    // For now, we only know how many were actually imported.
                    String message = "Successfully imported and saved " + importedCount + " new transactions to the ledger.\n" +
                                     "You may need to categorize entries from the '" + FileImportService.NEEDS_CATEGORIZATION_ACCOUNT_NUMBER + "' account.";
                    // If FileImportService could return a structure like:
                    // class ImportResult { List<AccountingTransaction> imported; int skippedCount; }
                    // Then we could say: + " " + result.skippedCount + " potential duplicates were skipped."
                    showFxAlert(AlertType.INFORMATION, "Import Successful", message);
                } catch (IOException | ActionCancelledException | NoFileCreatedException persistEx) {
                    showFxAlert(AlertType.ERROR, "Save Failed", 
                        "Imported " + newTransactions.size() + " transactions into memory, but failed to save changes to company file: " + persistEx.getMessage());
                    persistEx.printStackTrace();
                }
                
            } catch (IllegalArgumentException iae) { 
                 showFxAlert(AlertType.ERROR, "Import Failed", "Error during import: " + iae.getMessage());
            } catch (Exception ex) { 
                showFxAlert(AlertType.ERROR, "Import Failed", "An unexpected error occurred during import: " + ex.getMessage());
                ex.printStackTrace(); 
            }
        });
    }

    private void showFxAlert(AlertType alertType, String title, String content) {
        Platform.runLater(() -> { 
            Alert alert = new Alert(alertType);
            alert.initOwner(this.owner);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
