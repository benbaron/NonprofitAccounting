package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.importer.sclx.AccountImportMode;
import nonprofitbookkeeping.importer.sclx.NonprofitBookkeepingSclxImportTarget;
import nonprofitbookkeeping.importer.sclx.SclxImportOptions;
import nonprofitbookkeeping.importer.sclx.SclxImportResult;
import nonprofitbookkeeping.importer.sclx.SclxImportService;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.CurrentCompany;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Simple JavaFX panel for importing an SCLX file into the current company.
 */
public class SclxImportPanelFX extends VBox
{
    private final Stage owner;
    private final TextField sclxFileField = new TextField();
    private final TextField cashAccountField = new TextField();
    private final ComboBox<AccountImportMode> accountModeCombo = new ComboBox<>();
    private final TextField mappingFileField = new TextField();
    private final TextArea outputArea = new TextArea();

    public SclxImportPanelFX(Stage owner)
    {
        this.owner = owner;
        setSpacing(10);
        setPadding(new Insets(12));

        Label intro = new Label(
            "Import an SCLX file into the currently open company. " +
            "Single-sided transactions can be balanced automatically using the Cash account reference.");
        intro.setWrapText(true);

        this.accountModeCombo.getItems().addAll(AccountImportMode.AS_IS, AccountImportMode.MAPPED);
        this.accountModeCombo.getSelectionModel().select(AccountImportMode.AS_IS);

        this.sclxFileField.setPrefColumnCount(40);
        this.mappingFileField.setPrefColumnCount(40);
        this.outputArea.setEditable(false);
        this.outputArea.setWrapText(true);
        VBox.setVgrow(this.outputArea, Priority.ALWAYS);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);

        int row = 0;
        form.add(new Label("SCLX file"), 0, row);
        form.add(this.sclxFileField, 1, row);
        Button browseSclx = new Button("Browse...");
        browseSclx.setOnAction(e -> chooseSclxFile());
        form.add(browseSclx, 2, row++);

        form.add(new Label("Cash account reference"), 0, row);
        form.add(this.cashAccountField, 1, row++, 2, 1);

        form.add(new Label("Account import mode"), 0, row);
        form.add(this.accountModeCombo, 1, row++, 2, 1);

        form.add(new Label("Account mapping file"), 0, row);
        form.add(this.mappingFileField, 1, row);
        Button browseMapping = new Button("Browse...");
        browseMapping.setOnAction(e -> chooseMappingFile());
        form.add(browseMapping, 2, row++);

        Button importButton = new Button("Import SCLX");
        importButton.setOnAction(e -> runImport());

        getChildren().addAll(intro, new Separator(), form, importButton, new Separator(), new Label("Output"), this.outputArea);
    }

    private void chooseSclxFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose SCLX File");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("SCLX JSON", "*.json"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));
        java.io.File file = chooser.showOpenDialog(this.owner);
        if (file != null)
        {
            this.sclxFileField.setText(file.getAbsolutePath());
        }
    }

    private void chooseMappingFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Account Mapping File");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Properties/Text Files", "*.properties", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));
        java.io.File file = chooser.showOpenDialog(this.owner);
        if (file != null)
        {
            this.mappingFileField.setText(file.getAbsolutePath());
        }
    }

    private void runImport()
    {
        if (!Database.isInitialized())
        {
            showError("Database Not Ready", "Initialize/open the database before importing SCLX.");
            return;
        }
        if (!CurrentCompany.isOpen())
        {
            showError("Company Not Open", "Open a company before importing SCLX.");
            return;
        }
        if (this.sclxFileField.getText() == null || this.sclxFileField.getText().isBlank())
        {
            showError("Missing File", "Select an SCLX file first.");
            return;
        }

        Path sclxPath = Path.of(this.sclxFileField.getText().trim());
        Map<String, String> mapping = loadAccountMappingIfNeeded();
        AccountImportMode mode = this.accountModeCombo.getValue() == null ? AccountImportMode.AS_IS : this.accountModeCombo.getValue();
        ensureCashAccountReferenceSelectedWhenMissing();

        SclxImportOptions options = createImportOptions(mode, mapping);
        try
        {
            importFile(sclxPath, options);
        }
        catch (Exception ex)
        {
            if (isMissingCashAccountReferenceFailure(ex))
            {
                String selectedAccount = promptForCashAccountReference();
                if (selectedAccount == null)
                {
                    this.outputArea.appendText("Import canceled: cash account selection is required for unbalanced transactions.\n");
                    return;
                }
                this.cashAccountField.setText(selectedAccount);
                this.outputArea.appendText("Retrying import with cash account reference: " + selectedAccount + "\n");
                try
                {
                    importFile(sclxPath, createImportOptions(mode, mapping));
                    return;
                }
                catch (Exception retryEx)
                {
                    this.outputArea.appendText("Import failed: " + retryEx.getMessage() + "\n");
                    showError("SCLX Import Failed", retryEx.getMessage());
                    return;
                }
            }

            this.outputArea.appendText("Import failed: " + ex.getMessage() + "\n");
            showError("SCLX Import Failed", ex.getMessage());
        }
    }

    private void ensureCashAccountReferenceSelectedWhenMissing()
    {
        if (blankToNull(this.cashAccountField.getText()) != null)
        {
            return;
        }

        String selectedAccount = promptForCashAccountReference();
        if (selectedAccount != null)
        {
            this.cashAccountField.setText(selectedAccount);
            this.outputArea.appendText("Using cash account reference: " + selectedAccount + "\n");
        }
    }

    private SclxImportOptions createImportOptions(AccountImportMode mode, Map<String, String> mapping)
    {
        return new SclxImportOptions(
            true,
            true,
            true,
            true,
            blankToNull(this.cashAccountField.getText()),
            mode,
            mapping);
    }

    private void importFile(Path sclxPath, SclxImportOptions options)
    {
        SclxImportService service = new SclxImportService();
        NonprofitBookkeepingSclxImportTarget target = new NonprofitBookkeepingSclxImportTarget();
        SclxImportResult result = service.importFile(sclxPath, target, options);

        this.outputArea.appendText(
            "Import successful\n" +
            "Version: " + result.version() + "\n" +
            "Accounts: " + result.accountCount() + "\n" +
            "People: " + result.personCount() + "\n" +
            "Transactions: " + result.transactionCount() + "\n" +
            "Transaction Lines: " + result.transactionLineCount() + "\n" +
            "Supplemental Items: " + result.supplementalItemCount() + "\n" +
            "Banking Items: " + result.bankingItemCount() + "\n" +
            "Bank Statement Imports: " + result.bankStatementImportCount() + "\n\n");
    }

    private boolean isMissingCashAccountReferenceFailure(Exception ex)
    {
        return ex.getMessage() != null &&
            ex.getMessage().contains("cashAccountReference is required to import single-sided or unbalanced SCLX transactions.");
    }

    private String promptForCashAccountReference()
    {
        List<Account> accounts = CurrentCompany.getCompany().getChartOfAccounts().getAccounts();
        if (accounts == null || accounts.isEmpty())
        {
            showError(
                "Cash Account Required",
                "Import requires a cash account reference for unbalanced SCLX transactions, but no accounts are available in the chart of accounts.");
            return null;
        }

        Map<String, String> referenceByLabel = new LinkedHashMap<>();
        accounts.stream()
            .filter(Objects::nonNull)
            .filter(account -> account.getAccountNumber() != null && !account.getAccountNumber().isBlank())
            .sorted(Comparator.comparing(Account::getAccountNumber, String.CASE_INSENSITIVE_ORDER))
            .forEach(account -> referenceByLabel.put(
                account.getAccountNumber() + " - " + (account.getName() == null ? "" : account.getName()),
                account.getAccountNumber()));

        if (referenceByLabel.isEmpty())
        {
            showError(
                "Cash Account Required",
                "Import requires a cash account reference for unbalanced SCLX transactions, but no account numbers are available in the chart of accounts.");
            return null;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(referenceByLabel.keySet().iterator().next(), referenceByLabel.keySet());
        dialog.initOwner(this.owner);
        dialog.setTitle("Select Cash Account");
        dialog.setHeaderText("Cash account reference is required");
        dialog.setContentText("Select a Chart of Accounts account to use for balancing:");

        Optional<String> selection = dialog.showAndWait();
        return selection.map(referenceByLabel::get).orElse(null);
    }

    private Map<String, String> loadAccountMappingIfNeeded()
    {
        if (this.accountModeCombo.getValue() != AccountImportMode.MAPPED)
        {
            return Map.of();
        }

        if (this.mappingFileField.getText() == null || this.mappingFileField.getText().isBlank())
        {
            throw new IllegalArgumentException("Select an account mapping file for MAPPED mode.");
        }

        Properties properties = new Properties();
        Path path = Path.of(this.mappingFileField.getText().trim());
        try (InputStream in = Files.newInputStream(path))
        {
            properties.load(in);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Failed to read account mapping file: " + path, ex);
        }

        Map<String, String> mapping = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames())
        {
            mapping.put(key, properties.getProperty(key));
        }
        return mapping;
    }

    private void showError(String header, String content)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(this.owner);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String blankToNull(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
