package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.CompanyManagementService;
import nonprofitbookkeeping.service.CompanyManagementService.CompanyDefinition;
import nonprofitbookkeeping.service.CompanyManagementService.CompanySummary;
import nonprofitbookkeeping.service.CompanyStartupPreferenceStore;
import nonprofitbookkeeping.service.CompanyStartupPreferenceStore.StartupBehavior;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.preferences.PreferencesManager;

/** Shared company selection and administration surface for both UI systems. */
public class CompanyManagementPanelFX extends BorderPane
{
    private static final DateTimeFormatter DATE_TIME =
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
            .withZone(ZoneId.systemDefault());

    private final CompanyManagementService service;
    private final ObservableList<CompanySummary> source =
        FXCollections.observableArrayList();
    private final FilteredList<CompanySummary> filtered =
        new FilteredList<>(this.source, row -> true);
    private final SortedList<CompanySummary> sorted =
        new SortedList<>(this.filtered);
    private final TableView<CompanySummary> table = new TableView<>();
    private final TextArea preview = new TextArea();
    private final Label status = new Label();
    private final TextField search = new TextField();
    private final CheckBox showArchived = new CheckBox("Show archived");
    private final ComboBox<StartupBehavior> startupBehavior =
        new ComboBox<>(FXCollections.observableArrayList(
            StartupBehavior.values()));

    private Consumer<Company> companyOpenedHandler;
    private Runnable openDatabaseAction;

    public CompanyManagementPanelFX()
    {
        this(new CompanyManagementService());
    }

    public CompanyManagementPanelFX(CompanyManagementService service)
    {
        this.service = service;
        build();
        refreshCompanyList();
    }

    public void setOnCompanyOpened(Consumer<Company> handler)
    {
        this.companyOpenedHandler = handler;
    }

    public void setOpenDatabaseAction(Runnable action)
    {
        this.openDatabaseAction = action;
    }

    public void refreshCompanyList()
    {
        Long selectedId = selected() == null ? null : selected().id();
        try
        {
            this.source.setAll(this.service.listCompanies(
                this.showArchived.isSelected()));
            selectPreferred(selectedId);
            setStatus(this.source.isEmpty() ?
                "No companies in the active database." :
                "Loaded " + this.source.size() + " companies.", false);
        }
        catch (Exception ex)
        {
            this.source.clear();
            this.preview.setText("Unable to load companies: " + ex.getMessage());
            setStatus("Unable to load companies.", true);
        }
    }

    private void build()
    {
        setPadding(PanelChrome.PANEL_PADDING);
        this.search.setPromptText("Search companies");
        HBox.setHgrow(this.search, Priority.ALWAYS);
        this.startupBehavior.setValue(CompanyStartupPreferenceStore.get());
        this.startupBehavior.valueProperty().addListener((obs, oldValue,
            newValue) -> CompanyStartupPreferenceStore.set(newValue));
        this.showArchived.setOnAction(event -> refreshCompanyList());
        this.search.textProperty().addListener((obs, oldValue, newValue) ->
            applySearch(newValue));

        Button openDatabase = new Button("Open Database…");
        openDatabase.setOnAction(event -> {
            if (this.openDatabaseAction != null)
            {
                this.openDatabaseAction.run();
            }
        });
        Label database = new Label("Database: " + databaseLabel());
        HBox controls = new HBox(8,
            database, openDatabase, this.search, this.showArchived,
            new Label("At startup"), this.startupBehavior);
        controls.setPadding(new Insets(0, 0, 8, 0));
        setTop(PanelChrome.topSection("Companies", controls));

        buildTable();
        this.preview.setEditable(false);
        this.preview.setWrapText(true);
        SplitPane split = new SplitPane(this.table, this.preview);
        split.setDividerPositions(0.62);
        setCenter(split);

        Button open = new Button("Open");
        Button create = new Button("Create…");
        Button edit = new Button("Edit…");
        Button archive = new Button("Archive / Restore");
        Button backupDelete = new Button("Export Backup and Delete…");
        Button refresh = new Button("Refresh");
        open.setOnAction(event -> openSelected());
        create.setOnAction(event -> createCompany());
        edit.setOnAction(event -> editSelected());
        archive.setOnAction(event -> archiveSelected());
        backupDelete.setOnAction(event -> backupAndDeleteSelected());
        refresh.setOnAction(event -> refreshCompanyList());
        HBox actions = new HBox(8, open, create, edit, archive,
            backupDelete, refresh);
        VBox footer = new VBox(6, this.status, actions);
        footer.setPadding(new Insets(8, 0, 0, 0));
        setBottom(footer);
    }

    private void buildTable()
    {
        TableColumn<CompanySummary, String> name = new TableColumn<>(
            "Company Name");
        name.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            value.getValue().name()));
        name.setPrefWidth(260);
        TableColumn<CompanySummary, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(value -> new ReadOnlyLongWrapper(
            value.getValue().id()));
        id.setPrefWidth(80);
        TableColumn<CompanySummary, String> updated = new TableColumn<>(
            "Last Updated");
        updated.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            format(value.getValue().updatedAt())));
        updated.setPrefWidth(180);
        TableColumn<CompanySummary, String> opened = new TableColumn<>(
            "Last Opened");
        opened.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            format(value.getValue().lastOpenedAt())));
        opened.setPrefWidth(180);
        TableColumn<CompanySummary, String> statusColumn = new TableColumn<>(
            "Status");
        statusColumn.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            value.getValue().status()));
        statusColumn.setPrefWidth(120);
        this.table.getColumns().setAll(name, id, updated, opened, statusColumn);
        this.sorted.comparatorProperty().bind(this.table.comparatorProperty());
        this.table.setItems(this.sorted);
        this.table.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> showPreview(newValue));
        this.table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                openSelected();
                event.consume();
            }
        });
        this.table.setRowFactory(view -> {
            TableRow<CompanySummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    openSelected();
                }
            });
            ContextMenu menu = new ContextMenu();
            MenuItem open = new MenuItem("Open");
            open.setOnAction(event -> openSelected());
            MenuItem edit = new MenuItem("Edit");
            edit.setOnAction(event -> editSelected());
            MenuItem archive = new MenuItem("Archive / Restore");
            archive.setOnAction(event -> archiveSelected());
            MenuItem backupDelete = new MenuItem(
                "Export Backup and Delete");
            backupDelete.setOnAction(event -> backupAndDeleteSelected());
            menu.getItems().addAll(open, edit, archive, backupDelete);
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null).otherwise(menu));
            return row;
        });
    }

    private void applySearch(String text)
    {
        String needle = text == null ? "" : text.trim().toLowerCase();
        this.filtered.setPredicate(row -> needle.isBlank() ||
            row.name().toLowerCase().contains(needle) ||
            Long.toString(row.id()).contains(needle) ||
            row.status().toLowerCase().contains(needle));
    }

    private void selectPreferred(Long previousId)
    {
        Long target = previousId;
        if (target == null && CompanyStartupPreferenceStore.get() !=
            StartupBehavior.ALWAYS_ASK)
        {
            target = PreferencesService.getLastUsedCompanyId();
        }
        if (target != null)
        {
            for (CompanySummary row : this.sorted)
            {
                if (row.id() == target)
                {
                    this.table.getSelectionModel().select(row);
                    this.table.scrollTo(row);
                    if (CompanyStartupPreferenceStore.get() ==
                        StartupBehavior.AUTO_OPEN_LAST && previousId == null)
                    {
                        openSelected();
                    }
                    return;
                }
            }
        }
        this.table.getSelectionModel().clearSelection();
        this.preview.clear();
    }

    private void showPreview(CompanySummary row)
    {
        if (row == null)
        {
            this.preview.clear();
            return;
        }
        String warnings = row.warnings().isEmpty() ? "None" :
            String.join("\n  • ", row.warnings());
        this.preview.setText(
            "Company: " + row.name() + "\n" +
            "ID: " + row.id() + "\n" +
            "Status: " + row.status() + "\n" +
            "Legal structure: " + row.legalStructure() + "\n" +
            "Fiscal year starts: " + row.fiscalYearStart() + "\n" +
            "Base currency: " + row.baseCurrency() + "\n" +
            "Default bank account: " + row.defaultBankAccount() + "\n" +
            "Fund accounting: " + row.fundAccounting() + "\n" +
            "Chart template: " + row.chartTemplate() + "\n" +
            "Accounts: " + row.accountCount() + "\n" +
            "Funds: " + row.fundCount() + "\n" +
            "Transactions: " + row.transactionCount() + "\n" +
            "Transaction dates: " + value(row.earliestTransactionDate()) +
                " through " + value(row.latestTransactionDate()) + "\n" +
            "Last updated: " + format(row.updatedAt()) + "\n" +
            "Last opened: " + format(row.lastOpenedAt()) + "\n" +
            "Database: " + databaseLabel() + "\n" +
            "Validation warnings: " + warnings);
    }

    private void openSelected()
    {
        CompanySummary row = selected();
        if (row == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        try
        {
            Company company = this.service.open(row.id());
            setStatus("Opened " + row.name() + ".", false);
            if (this.companyOpenedHandler != null)
            {
                this.companyOpenedHandler.accept(company);
            }
            refreshCompanyList();
        }
        catch (Exception ex)
        {
            setStatus("Unable to open company: " + ex.getMessage(), true);
        }
    }

    private void createCompany()
    {
        CompanySetupWizardFX.show(null, false).ifPresent(definition -> {
            try
            {
                long id = this.service.create(definition);
                refreshCompanyList();
                selectById(id);
                setStatus("Created " + definition.companyName() + ".", false);
            }
            catch (Exception ex)
            {
                setStatus("Unable to create company: " + ex.getMessage(), true);
            }
        });
    }

    private void editSelected()
    {
        CompanySummary row = selected();
        if (row == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        try
        {
            CompanyDefinition existing = this.service.definitionFor(row.id());
            CompanySetupWizardFX.show(existing, true).ifPresent(definition -> {
                try
                {
                    this.service.update(row.id(), definition);
                    refreshCompanyList();
                    selectById(row.id());
                    setStatus("Updated " + definition.companyName() + ".",
                        false);
                }
                catch (Exception ex)
                {
                    setStatus("Unable to update company: " + ex.getMessage(),
                        true);
                }
            });
        }
        catch (Exception ex)
        {
            setStatus("Unable to edit company: " + ex.getMessage(), true);
        }
    }

    private void archiveSelected()
    {
        CompanySummary row = selected();
        if (row == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        try
        {
            this.service.setArchived(row.id(), !row.archived());
            setStatus((row.archived() ? "Restored " : "Archived ") +
                row.name() + ".", false);
            refreshCompanyList();
        }
        catch (Exception ex)
        {
            setStatus("Unable to change archive status: " + ex.getMessage(),
                true);
        }
    }

    private void backupAndDeleteSelected()
    {
        CompanySummary row = selected();
        if (row == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Company Backup");
        chooser.setInitialFileName(safeFileName(row.name()) + "-company.bin");
        java.io.File file = chooser.showSaveDialog(getScene() == null ? null :
            getScene().getWindow());
        if (file == null)
        {
            return;
        }
        try
        {
            Files.write(file.toPath(), this.service.exportBackup(row.id()));
        }
        catch (Exception ex)
        {
            setStatus("Unable to export backup: " + ex.getMessage(), true);
            return;
        }

        TextField typedName = new TextField();
        typedName.setPromptText(row.name());
        CheckBox backup = new CheckBox(
            "I confirm the backup was exported successfully.");
        CheckBox destroy = new CheckBox(
            "I understand this permanently deletes company ID " + row.id() +
                " with " + row.transactionCount() + " transactions.");
        GridPane content = new GridPane();
        content.setHgap(8);
        content.setVgap(8);
        content.addRow(0, new Label("Type company name"), typedName);
        content.add(backup, 1, 1);
        content.add(destroy, 1, 2);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Company " + row.id());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.CANCEL, ButtonType.OK);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.orElse(ButtonType.CANCEL) != ButtonType.OK)
        {
            return;
        }
        try
        {
            this.service.delete(row.id(), typedName.getText(),
                backup.isSelected(), destroy.isSelected());
            refreshCompanyList();
            setStatus("Deleted " + row.name() + ".", false);
        }
        catch (Exception ex)
        {
            setStatus("Unable to delete company: " + ex.getMessage(), true);
        }
    }

    private CompanySummary selected()
    {
        return this.table.getSelectionModel().getSelectedItem();
    }

    private void selectById(long id)
    {
        for (CompanySummary row : this.sorted)
        {
            if (row.id() == id)
            {
                this.table.getSelectionModel().select(row);
                this.table.scrollTo(row);
                break;
            }
        }
    }

    private void setStatus(String message, boolean error)
    {
        this.status.setText(message == null ? "" : message);
        this.status.setStyle(error ? "-fx-text-fill: #b00020;" :
            "-fx-text-fill: #1b5e20;");
    }

    private String databaseLabel()
    {
        String path = PreferencesManager.getLastDatabasePath();
        return path == null || path.isBlank() ? "No database open" : path;
    }

    private String format(Instant value)
    {
        return value == null ? "Never" : DATE_TIME.format(value);
    }

    private String value(Object value)
    {
        return value == null ? "None" : value.toString();
    }

    private String safeFileName(String name)
    {
        return name.replaceAll("[^A-Za-z0-9._-]+", "-");
    }
}
