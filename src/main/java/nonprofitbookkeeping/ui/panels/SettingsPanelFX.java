package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ReportPeriodPreset;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.ui.ThemeManager;
import nonprofitbookkeeping.ui.UiSpacing;
import nonprofitbookkeeping.util.FormatUtils;

/** JavaFX settings workspace. */
public class SettingsPanelFX extends BorderPane
{
    private final SettingsService service;
    private final Stage primaryStage;
    private final Runnable onSettingsSaved;

    private TextField orgNameField;
    private TextField fiscalStartField;
    private ComboBox<String> currencyBox;
    private ComboBox<String> incomeAccountBox;
    private ComboBox<String> expenseAccountBox;
    private ComboBox<String> donationEditPolicyBox;
    private ComboBox<String> themeCombo;
    private ComboBox<Locale> languageCombo;
    private ComboBox<String> pendingRowTextColorCombo;
    private TextField currencyFormatField;
    private CheckBox preserveJournalLineOrderCheck;
    private CheckBox autosaveEnabledCheck;
    private Spinner<Integer> autosaveIntervalSpinner;
    private TextField defaultDirectoryField;
    private TextField lastFileField;
    private ComboBox<ReportPeriodPreset> defaultReportPeriodCombo;
    private CheckBox yearToDateOptionCheck;
    private CheckBox fullYearOptionCheck;
    private CheckBox lastMonthOptionCheck;

    public SettingsPanelFX(Stage primaryStage, SettingsService service)
    {
        this(primaryStage, service, null);
    }

    public SettingsPanelFX(Stage primaryStage, SettingsService service,
        Runnable onSettingsSaved)
    {
        this.primaryStage = primaryStage;
        this.service = service;
        this.onSettingsSaved = onSettingsSaved;
        loadSettings();

        setPadding(PanelChrome.PANEL_PADDING);
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(companyInfoTab(), accountingTab(), applicationTab(),
            backupTab(), uiPrefsTab());
        setTop(PanelChrome.topSection("Settings"));
        setCenter(tabs);

        Button save = new Button("Save Settings");
        save.setOnAction(event -> saveSettings());
        setBottom(save);
        BorderPane.setMargin(save, new Insets(UiSpacing.SECTION_SPACING));
    }

    public SettingsPanelFX(Stage stage)
    {
        this(stage, new SettingsService(), null);
    }

    private void loadSettings()
    {
        if (this.service == null || !Database.isInitialized())
        {
            return;
        }
        try
        {
            this.service.loadSettings(null);
            SettingsModel current = this.service.getSettings();
            FormatUtils.configureLocale(
                current.getLanguage() == null ? Locale.getDefault() :
                    Locale.forLanguageTag(current.getLanguage()),
                current.getDefaultCurrency());
            FormatUtils.setCurrencyFormat(current.getCurrencyFormat());
            applyWindowSettings(current);
        }
        catch (IOException ex)
        {
            alert("Failed to load settings: " + ex.getMessage());
        }
    }

    private void saveSettings()
    {
        collectFieldValues();
        if (this.service != null && Database.isInitialized())
        {
            try
            {
                this.service.saveSettings(null);
                applyWindowSettings(this.service.getSettings());
                alert("Settings saved");
                if (this.onSettingsSaved != null)
                {
                    this.onSettingsSaved.run();
                }
            }
            catch (IOException ex)
            {
                alert("Failed to save settings: " + ex.getMessage());
            }
        }
        else
        {
            alert("UI preferences saved. Open a company database to save company settings.");
        }
    }

    private void applyWindowSettings(SettingsModel current)
    {
        if (current == null)
        {
            return;
        }
        if (this.primaryStage != null && this.primaryStage.getScene() != null)
        {
            ThemeManager.applyTheme(this.primaryStage.getScene(), current.getTheme());
            String organization = current.getOrganizationName();
            this.primaryStage.setTitle(organization == null || organization.isBlank()
                ? "Nonprofit Bookkeeping"
                : organization + " - Nonprofit Bookkeeping");
        }
        FormatUtils.setCurrencyFormat(current.getCurrencyFormat());
    }

    private Tab companyInfoTab()
    {
        GridPane grid = grid();
        this.orgNameField = new TextField();
        this.fiscalStartField = new TextField();
        this.fiscalStartField.setPromptText("MM-DD");
        this.currencyBox = new ComboBox<>();
        this.currencyBox.getItems().addAll("USD", "EUR", "GBP");

        SettingsModel model = settings();
        this.orgNameField.setText(safe(model.getOrganizationName()));
        this.fiscalStartField.setText(safe(model.getFiscalYearStart()));
        this.currencyBox.setValue(blankDefault(model.getDefaultCurrency(), "USD"));

        add(grid, 0, "Organization Name:", this.orgNameField);
        add(grid, 1, "Fiscal Year Start:", this.fiscalStartField);
        add(grid, 2, "Default Currency:", this.currencyBox);
        return tab("Company Info", "Company Information", grid);
    }

    private Tab accountingTab()
    {
        GridPane grid = grid();
        this.incomeAccountBox = new ComboBox<>();
        this.expenseAccountBox = new ComboBox<>();
        this.donationEditPolicyBox = new ComboBox<>();
        this.donationEditPolicyBox.getItems().addAll("UPDATE_IN_PLACE",
            "REVERSE_AND_REPOST");

        ChartOfAccounts chart = CurrentCompany.getCompany() == null ? null :
            CurrentCompany.getCompany().getChartOfAccounts();
        if (chart != null)
        {
            chart.getAccounts().forEach(account -> {
                this.incomeAccountBox.getItems().add(account.getName());
                this.expenseAccountBox.getItems().add(account.getName());
            });
        }

        SettingsModel model = settings();
        this.incomeAccountBox.setValue(model.getDefaultIncomeAccount());
        this.expenseAccountBox.setValue(model.getDefaultExpenseAccount());
        this.donationEditPolicyBox.setValue(blankDefault(
            model.getDonationEditPostingPolicy(), "UPDATE_IN_PLACE"));

        add(grid, 0, "Default Income Account:", this.incomeAccountBox);
        add(grid, 1, "Default Expense Account:", this.expenseAccountBox);
        add(grid, 2, "Donation Edit Posting:", this.donationEditPolicyBox);
        return tab("Accounting", "Accounting Settings", grid);
    }

    private Tab applicationTab()
    {
        GridPane grid = grid();
        SettingsModel model = settings();

        this.autosaveEnabledCheck = new CheckBox("Enable background autosave");
        this.autosaveEnabledCheck.setSelected(model.isAutosaveEnabled());
        this.autosaveIntervalSpinner = new Spinner<>(1, 240,
            Math.max(1, model.getAutosaveIntervalMinutes()));
        this.autosaveIntervalSpinner.setEditable(true);
        HBox autosave = new HBox(10, this.autosaveEnabledCheck,
            new Label("Interval (minutes):"), this.autosaveIntervalSpinner);

        this.defaultDirectoryField = new TextField(
            safe(model.getDefaultCompanyDirectory()));
        this.defaultDirectoryField.setEditable(false);
        Button chooseDirectory = new Button("Browse...");
        chooseDirectory.setOnAction(event -> chooseDirectory());
        HBox directory = new HBox(8, this.defaultDirectoryField,
            chooseDirectory);
        HBox.setHgrow(this.defaultDirectoryField, Priority.ALWAYS);

        this.lastFileField = new TextField(safe(model.getLastUsedCompanyFile()));
        this.lastFileField.setEditable(false);
        Button chooseFile = new Button("Browse...");
        chooseFile.setOnAction(event -> chooseCompanyFile());
        HBox file = new HBox(8, this.lastFileField, chooseFile);
        HBox.setHgrow(this.lastFileField, Priority.ALWAYS);

        this.defaultReportPeriodCombo = new ComboBox<>();
        this.defaultReportPeriodCombo.getItems().setAll(
            ReportPeriodPreset.values());
        this.defaultReportPeriodCombo.setValue(ReportPeriodPreset.fromString(
            model.getDefaultReportPeriod(), ReportPeriodPreset.YEAR_TO_DATE));
        this.defaultReportPeriodCombo.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(ReportPeriodPreset preset)
            {
                if (preset == null)
                {
                    return "";
                }
                return switch (preset)
                {
                    case YEAR_TO_DATE -> "Year to Date";
                    case FULL_YEAR -> "Full Fiscal Year";
                    case LAST_MONTH -> "Last Month";
                };
            }

            @Override
            public ReportPeriodPreset fromString(String value)
            {
                return ReportPeriodPreset.fromString(value,
                    ReportPeriodPreset.YEAR_TO_DATE);
            }
        });

        this.yearToDateOptionCheck = new CheckBox("Offer Year to Date");
        this.fullYearOptionCheck = new CheckBox("Offer Full Year");
        this.lastMonthOptionCheck = new CheckBox("Offer Last Month");
        this.yearToDateOptionCheck.setSelected(model.isEnableYearToDateOption());
        this.fullYearOptionCheck.setSelected(model.isEnableFullYearOption());
        this.lastMonthOptionCheck.setSelected(model.isEnableLastMonthOption());
        VBox reportOptions = new VBox(6, this.yearToDateOptionCheck,
            this.fullYearOptionCheck, this.lastMonthOptionCheck);

        add(grid, 0, "Autosave:", autosave);
        add(grid, 1, "Default Directory:", directory);
        add(grid, 2, "Last Used File:", file);
        add(grid, 3, "Default Report Period:",
            this.defaultReportPeriodCombo);
        add(grid, 4, "Enable Report Filters:", reportOptions);
        return tab("Application", "Application Settings", grid);
    }

    private Tab backupTab()
    {
        HBox box = new HBox(10);
        Button backup = new Button("Create Backup");
        Button restore = new Button("Restore Backup");
        backup.setOnAction(event -> createDatabaseBackup());
        restore.setOnAction(event -> restoreDatabaseBackup());
        box.getChildren().addAll(backup, restore);
        box.setPadding(PanelChrome.PANEL_PADDING);
        return tab("Backup", "Backup & Restore", box);
    }

    private Tab uiPrefsTab()
    {
        GridPane grid = grid();
        SettingsModel model = settings();

        this.themeCombo = new ComboBox<>();
        this.themeCombo.getItems().addAll("Light", "Dark", "System");
        this.themeCombo.setValue(blankDefault(model.getTheme(), "System"));

        this.languageCombo = new ComboBox<>();
        this.languageCombo.getItems().addAll(Locale.forLanguageTag("en-US"),
            Locale.forLanguageTag("es-ES"), Locale.forLanguageTag("fr-FR"));
        this.languageCombo.setValue(Locale.forLanguageTag(
            blankDefault(model.getLanguage(), "en-US")));
        this.languageCombo.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(Locale locale)
            {
                return locale == null ? "" : locale.getDisplayLanguage(locale)
                    + " (" + locale.getCountry() + ")";
            }

            @Override
            public Locale fromString(String value)
            {
                return value == null ? null : Locale.forLanguageTag(value);
            }
        });

        this.pendingRowTextColorCombo = new ComboBox<>();
        this.pendingRowTextColorCombo.getItems().addAll("Black", "System");
        this.pendingRowTextColorCombo.setValue(blankDefault(
            model.getPendingRowTextColor(), "Black"));

        this.currencyFormatField = new TextField(blankDefault(
            model.getCurrencyFormat(), FormatUtils.getCurrencyFormat()));

        this.preserveJournalLineOrderCheck = new CheckBox(
            "Preserve the stored debit/credit line order");
        this.preserveJournalLineOrderCheck.setSelected(
            PreferencesService.isJournalStoredLineOrderPreserved());
        this.preserveJournalLineOrderCheck.setTooltip(new Tooltip(
            "When cleared, journal entries display debit lines first and credit lines second."));

        add(grid, 0, "Theme:", this.themeCombo);
        add(grid, 1, "Language:", this.languageCombo);
        add(grid, 2, "Pending Row Text:", this.pendingRowTextColorCombo);
        add(grid, 3, "Currency Format:", this.currencyFormatField);
        add(grid, 4, "Journal Line Order:",
            this.preserveJournalLineOrderCheck);
        return tab("UI Preferences", "UI Preferences", grid);
    }

    private void chooseDirectory()
    {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Default Directory");
        File current = fileOrNull(this.defaultDirectoryField.getText());
        if (current != null)
        {
            chooser.setInitialDirectory(current.isDirectory() ? current :
                current.getParentFile());
        }
        File selected = chooser.showDialog(this.primaryStage);
        if (selected != null)
        {
            this.defaultDirectoryField.setText(selected.getAbsolutePath());
        }
    }

    private void chooseCompanyFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Company File");
        File current = fileOrNull(this.lastFileField.getText());
        if (current != null)
        {
            chooser.setInitialDirectory(current.getParentFile());
            chooser.setInitialFileName(current.getName());
        }
        File selected = chooser.showOpenDialog(this.primaryStage);
        if (selected != null)
        {
            this.lastFileField.setText(selected.getAbsolutePath());
        }
    }

    private File fileOrNull(String path)
    {
        if (path == null || path.isBlank())
        {
            return null;
        }
        File file = new File(path);
        return file.exists() ? file : null;
    }

    private void createDatabaseBackup()
    {
        if (!Database.isInitialized())
        {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Database Script");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL Script", "*.sql"));
        File output = chooser.showSaveDialog(this.primaryStage);
        if (output == null)
        {
            return;
        }
        String target = output.toPath().toAbsolutePath().toString()
            .replace("'", "''");
        try (Connection connection = Database.get().getConnection();
             Statement statement = connection.createStatement())
        {
            statement.execute("SCRIPT TO '" + target + "'");
            alert("Database script exported to " + output.getAbsolutePath());
        }
        catch (SQLException ex)
        {
            alert("Backup failed: " + ex.getMessage());
        }
    }

    private void restoreDatabaseBackup()
    {
        if (!Database.isInitialized())
        {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Database Script");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL Script", "*.sql"));
        File input = chooser.showOpenDialog(this.primaryStage);
        if (input == null)
        {
            return;
        }
        String source = input.toPath().toAbsolutePath().toString()
            .replace("'", "''");
        try (Connection connection = Database.get().getConnection();
             Statement statement = connection.createStatement())
        {
            statement.execute("RUNSCRIPT FROM '" + source + "'");
            CurrentCompany.close();
            alert("Database restored from " + input.getName()
                + ". Reopen a company to continue working.");
        }
        catch (SQLException ex)
        {
            alert("Restore failed: " + ex.getMessage());
        }
    }

    private void collectFieldValues()
    {
        PreferencesService.setJournalStoredLineOrderPreserved(
            this.preserveJournalLineOrderCheck == null ||
                this.preserveJournalLineOrderCheck.isSelected());

        if (this.service == null)
        {
            return;
        }
        SettingsModel model = this.service.getSettings();
        model.setOrganizationName(this.orgNameField.getText());
        model.setFiscalYearStart(this.fiscalStartField.getText());
        model.setDefaultCurrency(this.currencyBox.getValue());
        model.setDefaultIncomeAccount(this.incomeAccountBox.getValue());
        model.setDefaultExpenseAccount(this.expenseAccountBox.getValue());
        model.setDonationEditPostingPolicy(this.donationEditPolicyBox.getValue());
        model.setTheme(this.themeCombo.getValue());
        model.setPendingRowTextColor(this.pendingRowTextColorCombo.getValue());
        if (this.languageCombo.getValue() != null)
        {
            Locale locale = this.languageCombo.getValue();
            model.setLanguage(locale.toLanguageTag());
            FormatUtils.configureLocale(locale, this.currencyBox.getValue());
        }
        model.setCurrencyFormat(this.currencyFormatField.getText());
        model.setAutosaveEnabled(this.autosaveEnabledCheck.isSelected());
        model.setAutosaveIntervalMinutes(
            this.autosaveIntervalSpinner.getValue());
        model.setDefaultCompanyDirectory(this.defaultDirectoryField.getText());
        model.setLastUsedCompanyFile(this.lastFileField.getText());
        model.setDefaultReportPeriod(
            this.defaultReportPeriodCombo.getValue().name());
        model.setEnableYearToDateOption(
            this.yearToDateOptionCheck.isSelected());
        model.setEnableFullYearOption(this.fullYearOptionCheck.isSelected());
        model.setEnableLastMonthOption(this.lastMonthOptionCheck.isSelected());
    }

    private SettingsModel settings()
    {
        return this.service == null ? new SettingsModel() :
            this.service.getSettings();
    }

    private GridPane grid()
    {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(PanelChrome.PANEL_PADDING);
        ColumnConstraints labels = new ColumnConstraints();
        labels.setPercentWidth(35);
        ColumnConstraints controls = new ColumnConstraints();
        controls.setPercentWidth(65);
        controls.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, controls);
        return grid;
    }

    private void add(GridPane grid, int row, String label, javafx.scene.Node control)
    {
        grid.add(new Label(label), 0, row);
        grid.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private Tab tab(String name, String title, javafx.scene.Node content)
    {
        TitledPane pane = new TitledPane(title, content);
        pane.setCollapsible(false);
        return new Tab(name, pane);
    }

    private static String safe(String value)
    {
        return value == null ? "" : value;
    }

    private static String blankDefault(String value, String fallback)
    {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static void alert(String message)
    {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK)
            .showAndWait();
    }
}
