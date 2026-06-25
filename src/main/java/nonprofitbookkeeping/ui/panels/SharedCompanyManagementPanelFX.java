package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.FileChooser;
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
import javafx.stage.Window;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;
import nonprofitbookkeeping.service.CompanyManagementService;
import nonprofitbookkeeping.service.CompanyManagementService.CompanyPreview;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.util.FormatUtils;

/** Shared company selection and administration workspace for both UI shells. */
public class SharedCompanyManagementPanelFX extends BorderPane
{
    public interface Host
    {
        String activeDatabaseLabel();
        void switchDatabase(Window owner) throws Exception;
        void openCompany(long id, String label) throws Exception;
        void closeActiveCompany();
        Long activeCompanyId();
        void openDeveloperTools(Window owner);
    }

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
            .withZone(ZoneId.systemDefault());

    private final CompanyManagementService service;
    private final Host host;
    private final ObservableList<CompanyRecord> source =
        FXCollections.observableArrayList();
    private final FilteredList<CompanyRecord> filtered =
        new FilteredList<>(this.source);
    private final TableView<CompanyRecord> table = new TableView<>();
    private final TextArea preview = new TextArea();
    private final TextField search = new TextField();
    private final CheckBox showArchived = new CheckBox("Show archived");
    private final Label databaseLabel = new Label();
    private final Label status = new Label();
    private final ChoiceBox<String> startupBehavior = new ChoiceBox<>();
    private Consumer<Company> companyOpened = company -> { };
    private Consumer<String> errorHandler = message -> { };
    private boolean startupApplied;

    public SharedCompanyManagementPanelFX()
    {
        this(new CompanyManagementService(), new DefaultHost());
    }

    public SharedCompanyManagementPanelFX(CompanyManagementService service,
        Host host)
    {
        this.service = service;
        this.host = host;
        setPadding(PanelChrome.PANEL_PADDING);
        build();
        refreshCompanyList();
    }

    public void setOnCompanyOpened(Consumer<Company> handler)
    {
        this.companyOpened = handler == null ? company -> { } : handler;
    }

    public void setOnError(Consumer<String> handler)
    {
        this.errorHandler = handler == null ? message -> { } : handler;
    }

    public void refreshCompanyList()
    {
        this.databaseLabel.setText(this.host.activeDatabaseLabel());
        this.source.clear();
        if (!Database.isInitialized())
        {
            this.preview.setText("Open a database to manage companies.");
            setStatus("No database open.", false);
            return;
        }
        try
        {
            this.source.setAll(this.service.listCompanies());
            applyFilter();
            applyStartupSelection();
            setStatus("Loaded " + this.source.size() + " companies.", false);
        }
        catch (Exception ex)
        {
            fail("Unable to load companies", ex);
        }
    }

    private void build()
    {
        this.databaseLabel.setMaxWidth(Double.MAX_VALUE);
        Button switchDatabase = new Button("Switch Database…");
        switchDatabase.setOnAction(event -> switchDatabase());

        this.startupBehavior.getItems().addAll(
            PreferencesService.COMPANY_STARTUP_PRESELECT,
            PreferencesService.COMPANY_STARTUP_OPEN,
            PreferencesService.COMPANY_STARTUP_NONE);
        this.startupBehavior.setValue(
            PreferencesService.getCompanyStartupBehavior());
        this.startupBehavior.valueProperty().addListener(
            (obs, oldValue, newValue) ->
                PreferencesService.setCompanyStartupBehavior(newValue));

        HBox database = new HBox(8, new Label("Database:"),
            this.databaseLabel, switchDatabase, new Label("Startup:"),
            this.startupBehavior);
        HBox.setHgrow(this.databaseLabel, Priority.ALWAYS);

        this.search.setPromptText("Search company name, ID, or status");
        this.search.textProperty().addListener((obs, oldValue, newValue) ->
            applyFilter());
        this.showArchived.selectedProperty().addListener(
            (obs, oldValue, newValue) -> applyFilter());
        HBox filters = new HBox(8, new Label("Search:"), this.search,
            this.showArchived);
        HBox.setHgrow(this.search, Priority.ALWAYS);
        setTop(new VBox(8, database, filters));

        buildTable();
        this.preview.setEditable(false);
        this.preview.setWrapText(true);
        SplitPane split = new SplitPane(this.table, this.preview);
        split.setDividerPositions(0.62);
        setCenter(split);

        Button open = new Button("Open");
        open.setOnAction(event -> openSelected());
        Button create = new Button("Create Company…");
        create.setOnAction(event -> editCompany(null));
        Button edit = new Button("Edit Company…");
        edit.setOnAction(event -> editSelected());
        Button archive = new Button("Archive / Restore");
        archive.setOnAction(event -> archiveSelected());
        Button backup = new Button("Export Backup…");
        backup.setOnAction(event -> exportSelected(false));
        Button backupDelete = new Button("Export Backup and Delete…");
        backupDelete.setOnAction(event -> exportSelected(true));
        Button delete = new Button("Delete…");
        delete.setOnAction(event -> deleteSelected(false));
        Button developer = new Button("Developer Tools…");
        developer.setOnAction(event -> this.host.openDeveloperTools(owner()));
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> refreshCompanyList());

        HBox actions = new HBox(8, open, create, edit, archive, backup,
            backupDelete, delete, developer, refresh);
        actions.setPadding(new Insets(10, 0, 0, 0));
        this.status.setPadding(new Insets(6, 0, 0, 0));
        setBottom(new VBox(actions, this.status));
    }

    private void buildTable()
    {
        TableColumn<CompanyRecord, String> name =
            new TableColumn<>("Company Name");
        name.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            value.getValue().name()));
        name.setPrefWidth(270);

        TableColumn<CompanyRecord, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(value -> new ReadOnlyLongWrapper(
            value.getValue().id()));
        id.setPrefWidth(80);

        TableColumn<CompanyRecord, String> updated =
            new TableColumn<>("Last Updated");
        updated.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            format(value.getValue().updatedAt())));
        updated.setPrefWidth(190);

        TableColumn<CompanyRecord, String> opened =
            new TableColumn<>("Last Opened");
        opened.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            format(value.getValue().lastOpenedAt())));
        opened.setPrefWidth(190);

        TableColumn<CompanyRecord, String> state =
            new TableColumn<>("Status");
        state.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            statusFor(value.getValue())));
        state.setPrefWidth(110);

        this.table.getColumns().setAll(name, id, updated, opened, state);
        this.table.setItems(this.filtered);
        this.table.setPlaceholder(new Label("No companies match the filter."));
        this.table.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> showPreview(newValue));
        this.table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                openSelected();
            }
        });
        this.table.setRowFactory(view -> {
            TableRow<CompanyRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    open(row.getItem());
                }
            });
            row.itemProperty().addListener((obs, oldValue, newValue) ->
                row.setContextMenu(newValue == null ? null :
                    contextMenu(newValue)));
            return row;
        });
    }

    private ContextMenu contextMenu(CompanyRecord record)
    {
        MenuItem open = new MenuItem("Open");
        open.setOnAction(event -> open(record));
        MenuItem edit = new MenuItem("Edit…");
        edit.setOnAction(event -> editCompany(record));
        MenuItem archive = new MenuItem(record.archived() ? "Restore" :
            "Archive");
        archive.setOnAction(event -> archive(record));
        MenuItem backup = new MenuItem("Export Backup…");
        backup.setOnAction(event -> export(record, false));
        MenuItem delete = new MenuItem("Delete…");
        delete.setOnAction(event -> delete(record, false));
        return new ContextMenu(open, edit, archive, backup, delete);
    }

    private void applyFilter()
    {
        String needle = this.search.getText() == null ? "" :
            this.search.getText().trim().toLowerCase();
        this.filtered.setPredicate(record -> {
            if (record == null)
            {
                return false;
            }
            if (record.archived() && !this.showArchived.isSelected())
            {
                return false;
            }
            return needle.isEmpty() ||
                record.name().toLowerCase().contains(needle) ||
                Long.toString(record.id()).contains(needle) ||
                statusFor(record).toLowerCase().contains(needle);
        });
    }

    private void applyStartupSelection()
    {
        if (this.startupApplied)
        {
            return;
        }
        this.startupApplied = true;
        String behavior = PreferencesService.getCompanyStartupBehavior();
        if (PreferencesService.COMPANY_STARTUP_NONE.equals(behavior))
        {
            return;
        }
        Long lastId = PreferencesService.getLastUsedCompanyId();
        CompanyRecord record = lastId == null ? null : this.source.stream()
            .filter(item -> item.id() == lastId && !item.archived())
            .findFirst().orElse(null);
        if (record != null)
        {
            this.table.getSelectionModel().select(record);
            this.table.scrollTo(record);
            if (PreferencesService.COMPANY_STARTUP_OPEN.equals(behavior))
            {
                Platform.runLater(() -> open(record));
            }
        }
        else if (!this.filtered.isEmpty())
        {
            this.table.getSelectionModel().selectFirst();
        }
    }

    private void showPreview(CompanyRecord record)
    {
        if (record == null)
        {
            this.preview.clear();
            return;
        }
        try
        {
            CompanyPreview value = this.service.preview(record);
            CompanyProfileModel profile = value.profile();
            StringBuilder text = new StringBuilder();
            text.append("Company: ").append(record.name()).append('\n');
            text.append("ID: ").append(record.id()).append('\n');
            text.append("Status: ").append(statusFor(record)).append("\n\n");
            if (profile != null)
            {
                text.append("Legal structure: ")
                    .append(safe(profile.getLegalStructure())).append('\n');
                text.append("Fiscal year start: ")
                    .append(safe(profile.getFiscalYearStart())).append('\n');
                text.append("Base currency: ")
                    .append(safe(profile.getBaseCurrency())).append('\n');
                text.append("Default bank account: ")
                    .append(safe(profile.getDefaultBankAccount())).append('\n');
                text.append("Chart template: ")
                    .append(safe(profile.getChartOfAccountsType())).append('\n');
                text.append("Fund accounting: ")
                    .append(profile.isEnableFundAccounting()).append('\n');
                text.append("Inventory: ")
                    .append(profile.isEnableInventory()).append('\n');
                text.append("Multi-currency: ")
                    .append(profile.isEnableMultiCurrency()).append("\n\n");
            }
            text.append("Accounts: ").append(value.accountCount()).append('\n');
            text.append("Funds referenced: ").append(value.fundCount()).append('\n');
            text.append("Transactions: ").append(value.transactionCount())
                .append('\n');
            text.append("Transaction range: ")
                .append(value.earliestTransaction() == null ? "—" :
                    value.earliestTransaction())
                .append(" to ")
                .append(value.latestTransaction() == null ? "—" :
                    value.latestTransaction()).append('\n');
            text.append("Last updated: ").append(format(record.updatedAt()))
                .append('\n');
            text.append("Last opened: ").append(format(record.lastOpenedAt()))
                .append('\n');
            text.append("Database: ").append(this.host.activeDatabaseLabel())
                .append('\n');
            if (!value.warnings().isEmpty())
            {
                text.append("\nValidation warnings:\n");
                value.warnings().forEach(warning -> text.append("• ")
                    .append(warning).append('\n'));
            }
            this.preview.setText(text.toString());
        }
        catch (Exception ex)
        {
            this.preview.setText("Unable to preview company: " +
                ex.getMessage());
        }
    }

    private void switchDatabase()
    {
        try
        {
            this.host.switchDatabase(owner());
            this.startupApplied = false;
            refreshCompanyList();
        }
        catch (Exception ex)
        {
            fail("Unable to switch database", ex);
        }
    }

    private void openSelected()
    {
        CompanyRecord selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        open(selected);
    }

    private void open(CompanyRecord record)
    {
        if (record.archived())
        {
            setStatus("Restore the archived company before opening it.", true);
            return;
        }
        try
        {
            this.host.openCompany(record.id(), record.name());
            this.service.markOpened(record.id());
            PreferencesService.setLastUsedCompanyId(record.id());
            Company company = this.service.load(record.id());
            if (company.getCompanyProfileModel() != null)
            {
                FormatUtils.configureLocale(null,
                    company.getCompanyProfileModel().getBaseCurrency());
            }
            this.companyOpened.accept(company);
            refreshCompanyList();
            select(record.id());
            setStatus("Opened company: " + record.name(), false);
        }
        catch (Exception ex)
        {
            fail("Unable to open company", ex);
        }
    }

    private void editSelected()
    {
        editCompany(this.table.getSelectionModel().getSelectedItem());
    }

    private void editCompany(CompanyRecord record)
    {
        try
        {
            Company company = record == null ? new Company() :
                this.service.load(record.id());
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(record == null ? "Create Company" :
                "Edit Company");
            CompanyProfileWizardFX wizard = new CompanyProfileWizardFX(
                company, profile -> {
                    try
                    {
                        long id = this.service.save(
                            record == null ? null : record.id(), profile);
                        dialog.setResult(null);
                        dialog.close();
                        refreshCompanyList();
                        select(id);
                        setStatus((record == null ? "Created" : "Updated") +
                            " company: " + profile.getCompanyName(), false);
                    }
                    catch (Exception ex)
                    {
                        fail("Unable to save company", ex);
                    }
                });
            dialog.getDialogPane().setContent(wizard);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.getDialogPane().setPrefSize(820, 620);
            dialog.setResizable(true);
            if (owner() != null)
            {
                dialog.initOwner(owner());
            }
            dialog.showAndWait();
        }
        catch (Exception ex)
        {
            fail("Unable to edit company", ex);
        }
    }

    private void archiveSelected()
    {
        CompanyRecord record = this.table.getSelectionModel().getSelectedItem();
        if (record == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        archive(record);
    }

    private void archive(CompanyRecord record)
    {
        try
        {
            if (this.host.activeCompanyId() != null &&
                this.host.activeCompanyId() == record.id())
            {
                this.host.closeActiveCompany();
            }
            this.service.setArchived(record.id(), !record.archived());
            refreshCompanyList();
            setStatus((record.archived() ? "Restored " : "Archived ") +
                record.name(), false);
        }
        catch (Exception ex)
        {
            fail("Unable to change archive status", ex);
        }
    }

    private void exportSelected(boolean deleteAfter)
    {
        CompanyRecord record = this.table.getSelectionModel().getSelectedItem();
        if (record == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        export(record, deleteAfter);
    }

    private void export(CompanyRecord record, boolean deleteAfter)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Company Backup");
        chooser.setInitialFileName(record.name().replaceAll("[^A-Za-z0-9._-]",
            "_") + ".npbk-company");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
            "Company Backup", "*.npbk-company"));
        File destination = chooser.showSaveDialog(owner());
        if (destination == null)
        {
            return;
        }
        try
        {
            Files.write(destination.toPath(),
                this.service.exportCompany(record.id()));
            setStatus("Exported backup: " + destination, false);
            if (deleteAfter)
            {
                delete(record, true);
            }
        }
        catch (Exception ex)
        {
            fail("Unable to export company backup", ex);
        }
    }

    private void deleteSelected(boolean backupAlreadyExported)
    {
        CompanyRecord record = this.table.getSelectionModel().getSelectedItem();
        if (record == null)
        {
            setStatus("Select a company first.", true);
            return;
        }
        delete(record, backupAlreadyExported);
    }

    private void delete(CompanyRecord record, boolean backupAlreadyExported)
    {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Company");
        dialog.setHeaderText("Delete " + record.name() + " (ID " +
            record.id() + ")");
        TextField typed = new TextField();
        typed.setPromptText("Type company name exactly");
        CheckBox backup = new CheckBox(
            "I have exported or otherwise backed up this company.");
        backup.setSelected(backupAlreadyExported);
        CheckBox understand = new CheckBox(
            "I understand this permanently removes the company row.");
        GridPane content = new GridPane();
        content.setHgap(8);
        content.setVgap(8);
        content.addRow(0, new Label("Company:"), new Label(record.name()));
        content.addRow(1, new Label("ID:"), new Label(
            Long.toString(record.id())));
        try
        {
            content.addRow(2, new Label("Transactions:"), new Label(
                Integer.toString(this.service.preview(record)
                    .transactionCount())));
        }
        catch (Exception ignored)
        {
            content.addRow(2, new Label("Transactions:"), new Label("Unknown"));
        }
        content.addRow(3, new Label("Confirm name:"), typed);
        content.add(backup, 1, 4);
        content.add(understand, 1, 5);
        dialog.getDialogPane().setContent(content);
        ButtonType deleteType = new ButtonType("Delete Permanently",
            ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteType,
            ButtonType.CANCEL);
        if (owner() != null)
        {
            dialog.initOwner(owner());
        }
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != deleteType)
        {
            return;
        }
        if (!record.name().equals(typed.getText()) ||
            !backup.isSelected() || !understand.isSelected())
        {
            setStatus("Deletion confirmation was incomplete.", true);
            return;
        }
        try
        {
            if (this.host.activeCompanyId() != null &&
                this.host.activeCompanyId() == record.id())
            {
                this.host.closeActiveCompany();
            }
            this.service.delete(record.id());
            refreshCompanyList();
            setStatus("Deleted company: " + record.name(), false);
        }
        catch (Exception ex)
        {
            fail("Unable to delete company", ex);
        }
    }

    private void select(long id)
    {
        this.source.stream().filter(record -> record.id() == id)
            .findFirst().ifPresent(record -> {
                this.table.getSelectionModel().select(record);
                this.table.scrollTo(record);
            });
    }

    private String statusFor(CompanyRecord record)
    {
        Long activeId = this.host.activeCompanyId();
        if (activeId != null && activeId == record.id())
        {
            return "Open";
        }
        return record.status();
    }

    private Window owner()
    {
        return getScene() == null ? null : getScene().getWindow();
    }

    private void setStatus(String message, boolean error)
    {
        this.status.setText(message == null ? "" : message);
        this.status.setStyle(error ? "-fx-text-fill: #b00020;" :
            "-fx-text-fill: #1b5e20;");
    }

    private void fail(String operation, Exception ex)
    {
        String message = operation + ": " + ex.getMessage();
        setStatus(message, true);
        this.errorHandler.accept(message);
    }

    private static String format(java.time.Instant instant)
    {
        return instant == null ? "Never" : TIME_FORMAT.format(instant);
    }

    private static String safe(String value)
    {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static final class DefaultHost implements Host
    {
        @Override
        public String activeDatabaseLabel()
        {
            return Database.isInitialized() ? Database.get().getJdbcUrl() :
                "No database open";
        }

        @Override
        public void switchDatabase(Window owner) throws Exception
        {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open H2 Database");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "H2 Database", "*.mv.db", "*.db"));
            File selected = chooser.showOpenDialog(owner);
            if (selected == null)
            {
                return;
            }
            String path = selected.getAbsolutePath();
            if (path.endsWith(".mv.db"))
            {
                path = path.substring(0, path.length() - 6);
            }
            CurrentCompany.close();
            Database.close();
            Database.init(Path.of(path));
            Database.get().ensureSchema();
        }

        @Override
        public void openCompany(long id, String label) throws Exception
        {
            CurrentCompany.loadFromPersistent(id);
        }

        @Override
        public void closeActiveCompany()
        {
            CurrentCompany.close();
        }

        @Override
        public Long activeCompanyId()
        {
            return CurrentCompany.getCurrentCompanyId();
        }

        @Override
        public void openDeveloperTools(Window owner)
        {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Developer Tools");
            dialog.getDialogPane().setContent(new DeveloperToolsPanelFX());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(700, 420);
            if (owner != null)
            {
                dialog.initOwner(owner);
            }
            dialog.showAndWait();
        }
    }
}
