package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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

/** Shared company-selection and administration workspace for both UI shells. */
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
    private final ObservableList<CompanyRecord> companies =
        FXCollections.observableArrayList();
    private final FilteredList<CompanyRecord> filtered =
        new FilteredList<>(this.companies);
    private final TableView<CompanyRecord> table = new TableView<>();
    private final TextArea preview = new TextArea();
    private final TextField search = new TextField();
    private final CheckBox showArchived = new CheckBox("Show archived");
    private final Label database = new Label();
    private final Label status = new Label();
    private final ChoiceBox<String> startup = new ChoiceBox<>();
    private Consumer<Company> onCompanyOpened = company -> { };
    private Consumer<String> onError = message -> { };
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
        buildUi();
        refreshCompanyList();
    }

    public void setOnCompanyOpened(Consumer<Company> handler)
    {
        this.onCompanyOpened = handler == null ? company -> { } : handler;
    }

    public void setOnError(Consumer<String> handler)
    {
        this.onError = handler == null ? message -> { } : handler;
    }

    public void refreshCompanyList()
    {
        this.database.setText(this.host.activeDatabaseLabel());
        this.companies.clear();
        if (!Database.isInitialized())
        {
            this.preview.setText("Open a database to manage companies.");
            setStatus("No database open.", false);
            return;
        }
        try
        {
            this.companies.setAll(this.service.listCompanies());
            applyFilter();
            applyStartupSelection();
            setStatus("Loaded " + this.companies.size() + " companies.",
                false);
        }
        catch (Exception ex)
        {
            fail("Unable to load companies", ex);
        }
    }

    private void buildUi()
    {
        this.startup.getItems().setAll(
            PreferencesService.COMPANY_STARTUP_PRESELECT,
            PreferencesService.COMPANY_STARTUP_OPEN,
            PreferencesService.COMPANY_STARTUP_NONE);
        this.startup.setValue(PreferencesService.getCompanyStartupBehavior());
        this.startup.valueProperty().addListener((obs, oldValue, newValue) ->
            PreferencesService.setCompanyStartupBehavior(newValue));

        Button switchDatabase = button("Switch Database…", this::switchDatabase);
        this.database.setMaxWidth(Double.MAX_VALUE);
        HBox databaseRow = new HBox(8, new Label("Database:"), this.database,
            switchDatabase, new Label("Startup:"), this.startup);
        HBox.setHgrow(this.database, Priority.ALWAYS);

        this.search.setPromptText("Search company name, ID, or status");
        this.search.textProperty().addListener((obs, oldValue, newValue) ->
            applyFilter());
        this.showArchived.selectedProperty().addListener(
            (obs, oldValue, newValue) -> applyFilter());
        HBox filterRow = new HBox(8, new Label("Search:"), this.search,
            this.showArchived);
        HBox.setHgrow(this.search, Priority.ALWAYS);
        setTop(new VBox(8, databaseRow, filterRow));

        buildTable();
        this.preview.setEditable(false);
        this.preview.setWrapText(true);
        SplitPane split = new SplitPane(this.table, this.preview);
        split.setDividerPositions(0.62);
        setCenter(split);

        HBox actions = new HBox(8,
            button("Open", this::openSelected),
            button("Create Company…", () -> editCompany(null)),
            button("Edit Company…", this::editSelected),
            button("Archive / Restore", this::archiveSelected),
            button("Export Backup…", () -> exportSelected(false)),
            button("Export Backup and Delete…", () -> exportSelected(true)),
            button("Delete…", () -> deleteSelected(false)),
            button("Developer Tools…",
                () -> this.host.openDeveloperTools(owner())),
            button("Refresh", this::refreshCompanyList));
        actions.setPadding(new Insets(10, 0, 0, 0));
        this.status.setPadding(new Insets(6, 0, 0, 0));
        setBottom(new VBox(actions, this.status));
    }

    private void buildTable()
    {
        TableColumn<CompanyRecord, String> name = textColumn("Company Name",
            270, CompanyRecord::name);
        TableColumn<CompanyRecord, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(value ->
            new ReadOnlyLongWrapper(value.getValue().id()));
        id.setPrefWidth(80);
        TableColumn<CompanyRecord, String> updated = textColumn("Last Updated",
            190, record -> format(record.updatedAt()));
        TableColumn<CompanyRecord, String> opened = textColumn("Last Opened",
            190, record -> format(record.lastOpenedAt()));
        TableColumn<CompanyRecord, String> state = textColumn("Status", 110,
            this::statusFor);
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

    private TableColumn<CompanyRecord, String> textColumn(String title,
        double width, java.util.function.Function<CompanyRecord, String> value)
    {
        TableColumn<CompanyRecord, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
            value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private ContextMenu contextMenu(CompanyRecord record)
    {
        return new ContextMenu(
            item("Open", () -> open(record)),
            item("Edit…", () -> editCompany(record)),
            item(record.archived() ? "Restore" : "Archive",
                () -> archive(record)),
            item("Export Backup…", () -> export(record, false)),
            item("Delete…", () -> delete(record, false)));
    }

    private void applyFilter()
    {
        String needle = this.search.getText() == null ? "" :
            this.search.getText().trim().toLowerCase();
        this.filtered.setPredicate(record -> record != null &&
            (!record.archived() || this.showArchived.isSelected()) &&
            (needle.isEmpty() ||
                record.name().toLowerCase().contains(needle) ||
                Long.toString(record.id()).contains(needle) ||
                statusFor(record).toLowerCase().contains(needle)));
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
        CompanyRecord last = lastId == null ? null : this.companies.stream()
            .filter(record -> record.id() == lastId && !record.archived())
            .findFirst().orElse(null);
        if (last != null)
        {
            select(last.id());
            if (PreferencesService.COMPANY_STARTUP_OPEN.equals(behavior))
            {
                Platform.runLater(() -> open(last));
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
            StringBuilder text = new StringBuilder()
                .append("Company: ").append(record.name()).append('\n')
                .append("ID: ").append(record.id()).append('\n')
                .append("Status: ").append(statusFor(record)).append("\n\n");
            appendProfile(text, profile);
            text.append("Accounts: ").append(value.accountCount()).append('\n')
                .append("Funds referenced: ").append(value.fundCount()).append('\n')
                .append("Transactions: ").append(value.transactionCount())
                .append('\n')
                .append("Transaction range: ")
                .append(value.earliestTransaction() == null ? "—" :
                    value.earliestTransaction()).append(" to ")
                .append(value.latestTransaction() == null ? "—" :
                    value.latestTransaction()).append('\n')
                .append("Last updated: ").append(format(record.updatedAt()))
                .append('\n')
                .append("Last opened: ").append(format(record.lastOpenedAt()))
                .append('\n')
                .append("Database: ").append(this.host.activeDatabaseLabel())
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

    private static void appendProfile(StringBuilder text,
        CompanyProfileModel profile)
    {
        if (profile == null)
        {
            return;
        }
        text.append("Legal structure: ")
            .append(display(profile.getLegalStructure())).append('\n')
            .append("Fiscal year start: ")
            .append(display(profile.getFiscalYearStart())).append('\n')
            .append("Base currency: ")
            .append(display(profile.getBaseCurrency())).append('\n')
            .append("Default bank account: ")
            .append(display(profile.getDefaultBankAccount())).append('\n')
            .append("Chart template: ")
            .append(display(profile.getChartOfAccountsType())).append('\n')
            .append("Fund accounting: ")
            .append(profile.isEnableFundAccounting()).append('\n')
            .append("Inventory: ").append(profile.isEnableInventory())
            .append('\n')
            .append("Multi-currency: ")
            .append(profile.isEnableMultiCurrency()).append("\n\n");
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
        CompanyRecord record = selected();
        if (record == null)
        {
            setStatus("Select a company first.", true);
        }
        else
        {
            open(record);
        }
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
            CompanyProfileModel profile = company.getCompanyProfileModel();
            if (profile != null)
            {
                FormatUtils.configureLocale(null, profile.getBaseCurrency());
            }
            this.onCompanyOpened.accept(company);
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
        editCompany(selected());
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
            CompanyProfileWizardFX wizard = new CompanyProfileWizardFX(company,
                profile -> saveFromWizard(dialog, record, profile));
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

    private void saveFromWizard(Dialog<Void> dialog, CompanyRecord record,
        CompanyProfileModel profile)
    {
        try
        {
            long id = this.service.save(record == null ? null : record.id(),
                profile);
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
    }

    private void archiveSelected()
    {
        CompanyRecord record = selected();
        if (record == null)
        {
            setStatus("Select a company first.", true);
        }
        else
        {
            archive(record);
        }
    }

    private void archive(CompanyRecord record)
    {
        try
        {
            closeIfActive(record.id());
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
        CompanyRecord record = selected();
        if (record == null)
        {
            setStatus("Select a company first.", true);
        }
        else
        {
            export(record, deleteAfter);
        }
    }

    private void export(CompanyRecord record, boolean deleteAfter)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Company Backup");
        chooser.setInitialFileName(record.name().replaceAll(
            "[^A-Za-z0-9._-]", "_") + ".npbk-company");
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
        CompanyRecord record = selected();
        if (record == null)
        {
            setStatus("Select a company first.", true);
        }
        else
        {
            delete(record, backupAlreadyExported);
        }
    }

    private void delete(CompanyRecord record, boolean backupAlreadyExported)
    {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Company");
        dialog.getDialogPane().setHeaderText("Delete " + record.name() +
            " (ID " + record.id() + ")");
        TextField typedName = new TextField();
        typedName.setPromptText("Type company name exactly");
        CheckBox backup = new CheckBox(
            "I have exported or otherwise backed up this company.");
        backup.setSelected(backupAlreadyExported);
        CheckBox understand = new CheckBox(
            "I understand this permanently removes the company row.");
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("Company:"), new Label(record.name()));
        grid.addRow(1, new Label("ID:"), new Label(
            Long.toString(record.id())));
        grid.addRow(2, new Label("Transactions:"), new Label(
            transactionCount(record)));
        grid.addRow(3, new Label("Confirm name:"), typedName);
        grid.add(backup, 1, 4);
        grid.add(understand, 1, 5);
        dialog.getDialogPane().setContent(grid);
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
        if (!record.name().equals(typedName.getText()) ||
            !backup.isSelected() || !understand.isSelected())
        {
            setStatus("Deletion confirmation was incomplete.", true);
            return;
        }
        try
        {
            closeIfActive(record.id());
            this.service.delete(record.id());
            refreshCompanyList();
            setStatus("Deleted company: " + record.name(), false);
        }
        catch (Exception ex)
        {
            fail("Unable to delete company", ex);
        }
    }

    private void closeIfActive(long id)
    {
        Long activeId = this.host.activeCompanyId();
        if (activeId != null && activeId == id)
        {
            this.host.closeActiveCompany();
        }
    }

    private String transactionCount(CompanyRecord record)
    {
        try
        {
            return Integer.toString(this.service.preview(record)
                .transactionCount());
        }
        catch (Exception ex)
        {
            return "Unknown";
        }
    }

    private CompanyRecord selected()
    {
        return this.table.getSelectionModel().getSelectedItem();
    }

    private void select(long id)
    {
        this.companies.stream().filter(record -> record.id() == id)
            .findFirst().ifPresent(record -> {
                this.table.getSelectionModel().select(record);
                this.table.scrollTo(record);
            });
    }

    private String statusFor(CompanyRecord record)
    {
        Long activeId = this.host.activeCompanyId();
        return activeId != null && activeId == record.id() ? "Open" :
            record.status();
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
        this.onError.accept(message);
    }

    private Window owner()
    {
        return getScene() == null ? null : getScene().getWindow();
    }

    private static Button button(String text, Runnable action)
    {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static MenuItem item(String text, Runnable action)
    {
        MenuItem item = new MenuItem(text);
        item.setOnAction(event -> action.run());
        return item;
    }

    private static String format(Instant instant)
    {
        return instant == null ? "Never" : TIME_FORMAT.format(instant);
    }

    private static String display(String value)
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

        @Override public void closeActiveCompany() { CurrentCompany.close(); }
        @Override public Long activeCompanyId()
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
