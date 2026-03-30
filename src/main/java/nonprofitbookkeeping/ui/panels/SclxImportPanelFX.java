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
import nonprofitbookkeeping.model.CurrentCompany;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        if (!ensureCashAccountReferenceSelectedWhenMissing())
        {
            return;
        }

        try
        {
            runImportAttempt(sclxPath, mode, mapping);
        }
        catch (Exception ex)
        {
            if (isMissingCashAccountReferenceError(ex) &&
                promptForCashAccountReference())
            {
                this.outputArea.appendText(
                    "Retrying import with selected cash account reference: " +
                        this.cashAccountField.getText().trim() + "\n");
                try
                {
                    runImportAttempt(sclxPath, mode, mapping);
                    return;
                }
                catch (Exception retryEx)
                {
                    this.outputArea.appendText(
                        "Import failed after retry: " +
                            retryEx.getMessage() + "\n");
                    showError("SCLX Import Failed", retryEx.getMessage());
                    return;
                }
            }

            this.outputArea.appendText("Import failed: " + ex.getMessage() + "\n");
            showError("SCLX Import Failed", ex.getMessage());
        }
    }

    private void runImportAttempt(Path sclxPath,
        AccountImportMode mode,
        Map<String, String> mapping)
    {
        SclxImportOptions options = new SclxImportOptions(
            true,
            true,
            true,
            true,
            blankToNull(this.cashAccountField.getText()),
            mode,
            mapping);

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

    private boolean ensureCashAccountReferenceSelectedWhenMissing()
    {
        if (blankToNull(this.cashAccountField.getText()) != null)
        {
            return true;
        }

        if (!promptForCashAccountReference())
        {
            this.outputArea.appendText(
                "Import canceled: cash account reference was not selected.\n");
            return false;
        }

        this.outputArea.appendText(
            "Using selected cash account reference: " +
                this.cashAccountField.getText().trim() + "\n");
        return true;
    }

    private boolean promptForCashAccountReference()
    {
        if (CurrentCompany.getCompany() == null ||
            CurrentCompany.getCompany().getChartOfAccounts() == null)
        {
            showError("Cash Account Required",
                "No chart of accounts is available to select a cash account.");
            return false;
        }

        List<String> references = CurrentCompany.getCompany()
            .getChartOfAccounts()
            .getAccounts()
            .stream()
            .map(a -> a.getAccountNumber())
            .filter(ref -> ref != null && !ref.isBlank())
            .sorted(Comparator.naturalOrder())
            .toList();

        if (references.isEmpty())
        {
            showError("Cash Account Required",
                "No account references are available in the chart of accounts.");
            return false;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(references.get(0), references);
        dialog.initOwner(this.owner);
        dialog.setTitle("Select Cash Account Reference");
        dialog.setHeaderText("Select a cash account reference for balancing transactions.");
        dialog.setContentText("Account reference:");
        Optional<String> selected = dialog.showAndWait();

        if (selected.isEmpty())
        {
            return false;
        }

        this.cashAccountField.setText(selected.get());
        return true;
    }

    private boolean isMissingCashAccountReferenceError(Exception ex)
    {
        return ex.getMessage() != null &&
            ex.getMessage().contains("cashAccountReference is required to import single-sided or unbalanced SCLX transactions");
    }
}
