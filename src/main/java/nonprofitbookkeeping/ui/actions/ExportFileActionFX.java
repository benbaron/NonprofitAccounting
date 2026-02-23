package nonprofitbookkeeping.ui.actions;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.FileExportService;
import nonprofitbookkeeping.service.FileExportService.ExportResult;
import nonprofitbookkeeping.service.FileExportService.StatementFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * JavaFX action that exports account activity to OFX/QFX files.
 */
public class ExportFileActionFX implements EventHandler<ActionEvent>
{

/** The Constant LOGGER. */
private static final Logger LOGGER =
LoggerFactory.getLogger(ExportFileActionFX.class);

/** The owner stage. */
private final Stage ownerStage;

/**
 * Instantiates a new export file action FX.
 *
 * @param primaryStage the primary stage
 */
public ExportFileActionFX(Stage primaryStage)
{
if (primaryStage == null)
{
throw new IllegalArgumentException("Primary stage (owner stage) cannot be null.");
}

this.ownerStage = primaryStage;
}

/**
 * Override @see javafx.event.EventHandler#handle(javafx.event.Event) 
 */
@Override public void handle(ActionEvent event)
{
boolean fxReady;

try
{
fxReady = Platform.isFxApplicationThread();
}
catch (IllegalStateException ex)
{
LOGGER.debug("JavaFX runtime not initialised. Skipping export dialog.", ex);
return;
}

if (!fxReady)
{
LOGGER.debug("Export invoked outside the JavaFX Application Thread; ignoring request.");
return;
}

Company company = CurrentCompany.getCompany();

if (company == null || company.getLedger() == null)
{
showAlert(AlertType.ERROR, "Export Unavailable", "No company is open for export.");
return;
}

ChartOfAccounts chart = company.getChartOfAccounts();

if (chart == null)
{
showAlert(AlertType.ERROR, "Export Unavailable", "Chart of accounts not loaded for current company.");
return;
}

List<Account> exportableAccounts = chart.getAccounts().stream()
.filter(FileExportService::isSupportedStatementAccount)
.sorted(Comparator.comparing(ExportFileActionFX::accountLabel, String.CASE_INSENSITIVE_ORDER))
.toList();

if (exportableAccounts.isEmpty())
{
showAlert(AlertType.INFORMATION,
"No Exportable Accounts",
"No bank or credit card accounts are available for OFX/QFX export.");
return;
}

Map<String, Account> labelToAccount = new LinkedHashMap<>();

for (Account account : exportableAccounts)
{
labelToAccount.put(accountLabel(account), account);
}

ChoiceDialog<String> accountDialog = new ChoiceDialog<>(labelToAccount.keySet().iterator().next(),
new ArrayList<>(labelToAccount.keySet()));
accountDialog.initOwner(this.ownerStage);
accountDialog.setTitle("Select Account");
accountDialog.setHeaderText("Choose the account to export to OFX/QFX:");

Optional<String> choice = accountDialog.showAndWait();

if (choice.isEmpty())
{
return; // cancelled
}

Account selectedAccount = labelToAccount.get(choice.get());

if (selectedAccount == null)
{
showAlert(AlertType.ERROR, "Export Error", "Selected account could not be resolved.");
return;
}

FileChooser fileChooser = new FileChooser();
fileChooser.setTitle("Export Statement");

FileChooser.ExtensionFilter ofxFilter = new FileChooser.ExtensionFilter("OFX files (*.ofx)", "*.ofx");
FileChooser.ExtensionFilter qfxFilter = new FileChooser.ExtensionFilter("QFX files (*.qfx)", "*.qfx");
fileChooser.getExtensionFilters().addAll(ofxFilter, qfxFilter);
fileChooser.setSelectedExtensionFilter(ofxFilter);
fileChooser.setInitialFileName(suggestFileName(selectedAccount));

File chosen = fileChooser.showSaveDialog(this.ownerStage);

if (chosen == null)
{
return;
}

StatementFormat format = determineFormat(fileChooser.getSelectedExtensionFilter(), chosen);
File destination = ensureExtension(chosen, format);

ExportResult result = FileExportService.exportAccountStatement(destination,
company,
selectedAccount,
format);

if (!result.success())
{
showAlert(AlertType.ERROR, "Export Failed", result.message());
return;
}

Alert success = new Alert(AlertType.INFORMATION);
success.setTitle("Export Complete");
success.setHeaderText(null);
success.setContentText(result.message());
success.initOwner(this.ownerStage);
success.showAndWait();
}

/**
 * Account label.
 *
 * @param account the account
 * @return the string
 */
private static String accountLabel(Account account)
{
String name = Optional.ofNullable(account.getName()).filter(n -> !n.isBlank()).orElse("Account");
String number = Optional.ofNullable(account.getAccountNumber()).orElse("?");
return name + " (" + number + ")";
}

/**
 * Suggest file name.
 *
 * @param account the account
 * @return the string
 */
private static String suggestFileName(Account account)
{
String base = Optional.ofNullable(account.getName())
.filter(n -> !n.isBlank())
.orElse(Optional.ofNullable(account.getAccountNumber()).orElse("statement"));
return base.replaceAll("[^A-Za-z0-9-_]", "_") + ".ofx";
}

/**
 * Determine format.
 *
 * @param filter the filter
 * @param chosen the chosen
 * @return the statement format
 */
private static StatementFormat determineFormat(FileChooser.ExtensionFilter filter, File chosen)
{
String lowerName = chosen.getName().toLowerCase();

if (filter != null && filter.getDescription().toLowerCase().contains("qfx"))
{
return StatementFormat.QFX;
}

if (lowerName.endsWith(".qfx"))
{
return StatementFormat.QFX;
}

return StatementFormat.OFX;
}

/**
 * Ensure extension.
 *
 * @param file the file
 * @param format the format
 * @return the file
 */
private static File ensureExtension(File file, StatementFormat format)
{
String lower = file.getName().toLowerCase();

if (lower.endsWith(".ofx") || lower.endsWith(".qfx"))
{
return file;
}

String suffix = (format == StatementFormat.QFX) ? ".qfx" : ".ofx";
return new File(file.getParentFile(), file.getName() + suffix);
}

/**
 * Show alert.
 *
 * @param type the type
 * @param title the title
 * @param message the message
 */
private void showAlert(AlertType type, String title, String message)
{
Alert alert = new Alert(type);
alert.setTitle(title);
alert.setHeaderText(null);
alert.setContentText(message);
alert.initOwner(this.ownerStage);
alert.showAndWait();
}
}
