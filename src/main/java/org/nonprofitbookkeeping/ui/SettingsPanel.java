package org.nonprofitbookkeeping.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.ReportPeriodPreset;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.util.FormatUtils;
import org.nonprofitbookkeeping.model.Account;

/** Native alternate workspace for application, database, and company settings. */
public class SettingsPanel implements AppPanel, AppPanel.SaveAware
{
    private final BorderPane root = new BorderPane();
    private final UiServiceProvider services;
    private final SettingsService settingsService;
    private final List<String> accountKeys = new ArrayList<>();
    private final Label status = new Label();
    private final TextField defaultCompanyDirectory = new TextField();
    private final TextField lastUsedCompanyFile = new TextField();
    private final ComboBox<String> theme = new ComboBox<>();
    private final CheckBox autosaveEnabled = new CheckBox("Enable autosave where supported");
    private final Spinner<Integer> autosaveInterval = new Spinner<>(1, 240, 5);
    private final TextField organizationName = new TextField();
    private final TextField fiscalYearStart = new TextField();
    private final ComboBox<String> currency = new ComboBox<>();
    private final TextField currencyFormat = new TextField();
    private final ComboBox<String> incomeAccount = new ComboBox<>();
    private final ComboBox<String> expenseAccount = new ComboBox<>();
    private final ComboBox<ReportPeriodPreset> defaultReportPeriod = new ComboBox<>();
    private final CheckBox yearToDate = new CheckBox("Offer year to date");
    private final CheckBox fullYear = new CheckBox("Offer full fiscal year");
    private final CheckBox lastMonth = new CheckBox("Offer last month");
    private boolean loaded;

    public SettingsPanel()
    {
        this(UiServiceRegistry.provider());
    }

    public SettingsPanel(UiServiceProvider services)
    {
        this.services = services;
        this.settingsService = new SettingsService();
        build();
        reload();
    }

    @Override public String title() { return "Settings"; }
    @Override public Node root() { return this.root; }

    @Override
    public SaveResult save()
    {
        List<String> errors = AlternateSettingsValidator.validate(this.fiscalYearStart.getText(),
            this.incomeAccount.getValue(), this.expenseAccount.getValue(), this.accountKeys);
        if (!errors.isEmpty())
        {
            String message = String.join(" ", errors);
            this.status.setText(message);
            return SaveResult.failed(message, null);
        }

        PreferencesService.setDefaultCompanyDir(this.defaultCompanyDirectory.getText());
        PreferencesService.setLastUsedCompanyFile(this.lastUsedCompanyFile.getText());
        PreferencesService.setThemePreference(this.theme.getValue());

        if (canPersistCompanySettings())
        {
            SettingsModel m = this.settingsService.getSettings();
            m.setOrganizationName(this.organizationName.getText());
            m.setFiscalYearStart(this.fiscalYearStart.getText());
            m.setDefaultCurrency(this.currency.getValue());
            m.setCurrencyFormat(this.currencyFormat.getText());
            m.setDefaultIncomeAccount(this.incomeAccount.getValue());
            m.setDefaultExpenseAccount(this.expenseAccount.getValue());
            m.setDefaultReportPeriod(this.defaultReportPeriod.getValue().name());
            m.setEnableYearToDateOption(this.yearToDate.isSelected());
            m.setEnableFullYearOption(this.fullYear.isSelected());
            m.setEnableLastMonthOption(this.lastMonth.isSelected());
            m.setAutosaveEnabled(this.autosaveEnabled.isSelected());
            m.setAutosaveIntervalMinutes(this.autosaveInterval.getValue());
            m.setDefaultCompanyDirectory(this.defaultCompanyDirectory.getText());
            m.setLastUsedCompanyFile(this.lastUsedCompanyFile.getText());
            m.setTheme(this.theme.getValue());
            try
            {
                this.settingsService.saveSettings(null);
                FormatUtils.setCurrencyFormat(m.getCurrencyFormat());
            }
            catch (IOException ex)
            {
                this.status.setText("Failed to save database/company settings: " + ex.getMessage());
                return SaveResult.failed(this.status.getText(), ex);
            }
        }
        this.status.setText(canPersistCompanySettings()
            ? "Application, database, and company settings saved."
            : "Application settings saved. Open a database and company to edit company settings.");
        this.loaded = true;
        return SaveResult.saved(this.status.getText());
    }

    private void build()
    {
        Label title = new Label("Settings");
        title.getStyleClass().add("panel-title");
        Button save = new Button("Save Settings");
        save.setOnAction(e -> save());
        Button databaseAdmin = new Button("Open Database Administration");
        databaseAdmin.setOnAction(e -> this.status.setText("Use the Database Administration workspace for open, backup, repair, and restore workflows."));
        Button companyAdmin = new Button("Open Company Administration");
        companyAdmin.setOnAction(e -> this.status.setText("Use the Company Administration workspace for create, switch, populate, and delete workflows."));
        this.status.setWrapText(true);
        TabPane tabs = new TabPane(appTab(), databaseTab(), companyTab(), reportsTab());
        this.root.setPadding(new Insets(8));
        this.root.setTop(new VBox(6, title, new HBox(8, save, databaseAdmin, companyAdmin), this.status, new Separator()));
        this.root.setCenter(tabs);
    }

    private Tab appTab()
    {
        this.theme.getItems().setAll("System", "Light", "Dark");
        this.defaultCompanyDirectory.setPromptText("Default company directory");
        this.lastUsedCompanyFile.setPromptText("Last used company file");
        this.autosaveInterval.setEditable(true);
        GridPane grid = grid();
        add(grid, 0, "Theme", this.theme);
        add(grid, 1, "Default company directory", this.defaultCompanyDirectory);
        add(grid, 2, "Last used company file", this.lastUsedCompanyFile);
        add(grid, 3, "Autosave", new HBox(8, this.autosaveEnabled, new Label("minutes"), this.autosaveInterval));
        return tab("Application", grid);
    }

    private Tab databaseTab()
    {
        Label message = new Label("Database lifecycle actions such as open, backup/export, repair, and restore live in Database Administration to keep destructive workflows explicit.");
        message.setWrapText(true);
        return tab("Database", new VBox(8, message));
    }

    private Tab companyTab()
    {
        this.fiscalYearStart.setPromptText("MM-DD");
        this.currency.getItems().setAll("USD", "CAD", "EUR", "GBP", defaultLocaleCurrencyCode());
        this.currencyFormat.setPromptText("$#,##0.00");
        GridPane grid = grid();
        add(grid, 0, "Organization profile", this.organizationName);
        add(grid, 1, "Fiscal year start", this.fiscalYearStart);
        add(grid, 2, "Currency", this.currency);
        add(grid, 3, "Currency format", this.currencyFormat);
        add(grid, 4, "Default income account", this.incomeAccount);
        add(grid, 5, "Default expense account", this.expenseAccount);
        return tab("Company", grid);
    }

    private Tab reportsTab()
    {
        this.defaultReportPeriod.getItems().setAll(ReportPeriodPreset.values());
        return tab("Reports", new VBox(8, new Label("Report defaults"), this.defaultReportPeriod, this.yearToDate, this.fullYear, this.lastMonth));
    }

    private String defaultLocaleCurrencyCode()
    {
        Locale locale = Locale.getDefault();
        if (locale == null || locale.getCountry() == null || locale.getCountry().isBlank())
        {
            return "USD";
        }
        return Currency.getInstance(locale).getCurrencyCode();
    }

    private void reload()
    {
        this.theme.setValue(PreferencesService.getThemePreference());
        this.defaultCompanyDirectory.setText(PreferencesService.getDefaultCompanyDir());
        this.lastUsedCompanyFile.setText(PreferencesService.getLastUsedCompanyFile());
        this.autosaveEnabled.setSelected(true);
        this.yearToDate.setSelected(true); this.fullYear.setSelected(true); this.lastMonth.setSelected(true);
        this.defaultReportPeriod.setValue(ReportPeriodPreset.YEAR_TO_DATE);
        this.currency.setValue("USD"); this.currencyFormat.setText("$#,##0.00");
        if (canPersistCompanySettings())
        {
            try
            {
                this.settingsService.loadSettings(null);
                loadAccountChoices();
                SettingsModel m = this.settingsService.getSettings();
                this.organizationName.setText(blankToEmpty(m.getOrganizationName()));
                this.fiscalYearStart.setText(blankToEmpty(m.getFiscalYearStart()));
                if (m.getDefaultCurrency() != null) this.currency.setValue(m.getDefaultCurrency());
                this.currencyFormat.setText(blankToDefault(m.getCurrencyFormat(), "$#,##0.00"));
                this.incomeAccount.setValue(m.getDefaultIncomeAccount());
                this.expenseAccount.setValue(m.getDefaultExpenseAccount());
                this.defaultReportPeriod.setValue(ReportPeriodPreset.fromString(m.getDefaultReportPeriod(), ReportPeriodPreset.YEAR_TO_DATE));
                this.yearToDate.setSelected(m.isEnableYearToDateOption());
                this.fullYear.setSelected(m.isEnableFullYearOption());
                this.lastMonth.setSelected(m.isEnableLastMonthOption());
                this.autosaveEnabled.setSelected(m.isAutosaveEnabled());
                this.autosaveInterval.getValueFactory().setValue(Math.max(1, m.getAutosaveIntervalMinutes()));
                this.status.setText("Company-level settings are enabled for " + this.services.sessionContext().activeCompanyDisplayLabel() + ".");
            }
            catch (Exception ex)
            {
                this.status.setText("Settings loaded with limited account choices: " + ex.getMessage());
            }
        }
        else
        {
            setCompanyControlsDisabled(true);
            this.status.setText("Company-level settings are disabled until a database and company are open.");
        }
        this.loaded = true;
    }

    private boolean canPersistCompanySettings()
    {
        return this.services.sessionContext().isCompanyOpen() && Database.isInitialized();
    }

    private void loadAccountChoices()
    {
        this.accountKeys.clear();
        for (Account account : this.services.accountLookup().listActivePostingAccounts())
        {
            String key = account.getCode() + " — " + account.getName();
            this.accountKeys.add(key);
        }
        this.incomeAccount.setItems(FXCollections.observableArrayList(this.accountKeys));
        this.expenseAccount.setItems(FXCollections.observableArrayList(this.accountKeys));
        setCompanyControlsDisabled(false);
    }

    private void setCompanyControlsDisabled(boolean disabled)
    {
        this.organizationName.setDisable(disabled); this.fiscalYearStart.setDisable(disabled); this.currency.setDisable(disabled);
        this.currencyFormat.setDisable(disabled); this.incomeAccount.setDisable(disabled); this.expenseAccount.setDisable(disabled);
        this.defaultReportPeriod.setDisable(disabled); this.yearToDate.setDisable(disabled); this.fullYear.setDisable(disabled); this.lastMonth.setDisable(disabled);
    }

    private static GridPane grid() { GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(12)); return g; }
    private static void add(GridPane g, int row, String label, Node node) { g.add(new Label(label), 0, row); g.add(node, 1, row); GridPane.setHgrow(node, Priority.ALWAYS); }
    private static Tab tab(String title, Node content) { Tab t = new Tab(title, content); t.setClosable(false); return t; }
    private static String blankToEmpty(String value) { return value == null ? "" : value; }
    private static String blankToDefault(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    boolean isLoaded() { return this.loaded; }
}
