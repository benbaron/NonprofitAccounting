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
    @Override public Node root() { return root; }

    @Override
    public SaveResult save()
    {
        List<String> errors = AlternateSettingsValidator.validate(fiscalYearStart.getText(),
            incomeAccount.getValue(), expenseAccount.getValue(), accountKeys);
        if (!errors.isEmpty())
        {
            String message = String.join(" ", errors);
            status.setText(message);
            return SaveResult.failed(message, null);
        }

        PreferencesService.setDefaultCompanyDir(defaultCompanyDirectory.getText());
        PreferencesService.setLastUsedCompanyFile(lastUsedCompanyFile.getText());
        PreferencesService.setThemePreference(theme.getValue());

        if (canPersistCompanySettings())
        {
            SettingsModel m = settingsService.getSettings();
            m.setOrganizationName(organizationName.getText());
            m.setFiscalYearStart(fiscalYearStart.getText());
            m.setDefaultCurrency(currency.getValue());
            m.setCurrencyFormat(currencyFormat.getText());
            m.setDefaultIncomeAccount(incomeAccount.getValue());
            m.setDefaultExpenseAccount(expenseAccount.getValue());
            m.setDefaultReportPeriod(defaultReportPeriod.getValue().name());
            m.setEnableYearToDateOption(yearToDate.isSelected());
            m.setEnableFullYearOption(fullYear.isSelected());
            m.setEnableLastMonthOption(lastMonth.isSelected());
            m.setAutosaveEnabled(autosaveEnabled.isSelected());
            m.setAutosaveIntervalMinutes(autosaveInterval.getValue());
            m.setDefaultCompanyDirectory(defaultCompanyDirectory.getText());
            m.setLastUsedCompanyFile(lastUsedCompanyFile.getText());
            m.setTheme(theme.getValue());
            try
            {
                settingsService.saveSettings(null);
                FormatUtils.setCurrencyFormat(m.getCurrencyFormat());
            }
            catch (IOException ex)
            {
                status.setText("Failed to save database/company settings: " + ex.getMessage());
                return SaveResult.failed(status.getText(), ex);
            }
        }
        status.setText(canPersistCompanySettings()
            ? "Application, database, and company settings saved."
            : "Application settings saved. Open a database and company to edit company settings.");
        loaded = true;
        return SaveResult.saved(status.getText());
    }

    private void build()
    {
        Label title = new Label("Settings");
        title.getStyleClass().add("panel-title");
        Button save = new Button("Save Settings");
        save.setOnAction(e -> save());
        Button databaseAdmin = new Button("Open Database Administration");
        databaseAdmin.setOnAction(e -> status.setText("Use the Database Administration workspace for open, backup, repair, and restore workflows."));
        Button companyAdmin = new Button("Open Company Administration");
        companyAdmin.setOnAction(e -> status.setText("Use the Company Administration workspace for create, switch, populate, and delete workflows."));
        status.setWrapText(true);
        TabPane tabs = new TabPane(appTab(), databaseTab(), companyTab(), reportsTab());
        root.setPadding(new Insets(8));
        root.setTop(new VBox(6, title, new HBox(8, save, databaseAdmin, companyAdmin), status, new Separator()));
        root.setCenter(tabs);
    }

    private Tab appTab()
    {
        theme.getItems().setAll("System", "Light", "Dark");
        defaultCompanyDirectory.setPromptText("Default company directory");
        lastUsedCompanyFile.setPromptText("Last used company file");
        autosaveInterval.setEditable(true);
        GridPane grid = grid();
        add(grid, 0, "Theme", theme);
        add(grid, 1, "Default company directory", defaultCompanyDirectory);
        add(grid, 2, "Last used company file", lastUsedCompanyFile);
        add(grid, 3, "Autosave", new HBox(8, autosaveEnabled, new Label("minutes"), autosaveInterval));
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
        fiscalYearStart.setPromptText("MM-DD");
        currency.getItems().setAll("USD", "CAD", "EUR", "GBP", defaultLocaleCurrencyCode());
        currencyFormat.setPromptText("$#,##0.00");
        GridPane grid = grid();
        add(grid, 0, "Organization profile", organizationName);
        add(grid, 1, "Fiscal year start", fiscalYearStart);
        add(grid, 2, "Currency", currency);
        add(grid, 3, "Currency format", currencyFormat);
        add(grid, 4, "Default income account", incomeAccount);
        add(grid, 5, "Default expense account", expenseAccount);
        return tab("Company", grid);
    }

    private Tab reportsTab()
    {
        defaultReportPeriod.getItems().setAll(ReportPeriodPreset.values());
        return tab("Reports", new VBox(8, new Label("Report defaults"), defaultReportPeriod, yearToDate, fullYear, lastMonth));
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
        theme.setValue(PreferencesService.getThemePreference());
        defaultCompanyDirectory.setText(PreferencesService.getDefaultCompanyDir());
        lastUsedCompanyFile.setText(PreferencesService.getLastUsedCompanyFile());
        autosaveEnabled.setSelected(true);
        yearToDate.setSelected(true); fullYear.setSelected(true); lastMonth.setSelected(true);
        defaultReportPeriod.setValue(ReportPeriodPreset.YEAR_TO_DATE);
        currency.setValue("USD"); currencyFormat.setText("$#,##0.00");
        if (canPersistCompanySettings())
        {
            try
            {
                settingsService.loadSettings(null);
                loadAccountChoices();
                SettingsModel m = settingsService.getSettings();
                organizationName.setText(blankToEmpty(m.getOrganizationName()));
                fiscalYearStart.setText(blankToEmpty(m.getFiscalYearStart()));
                if (m.getDefaultCurrency() != null) currency.setValue(m.getDefaultCurrency());
                currencyFormat.setText(blankToDefault(m.getCurrencyFormat(), "$#,##0.00"));
                incomeAccount.setValue(m.getDefaultIncomeAccount());
                expenseAccount.setValue(m.getDefaultExpenseAccount());
                defaultReportPeriod.setValue(ReportPeriodPreset.fromString(m.getDefaultReportPeriod(), ReportPeriodPreset.YEAR_TO_DATE));
                yearToDate.setSelected(m.isEnableYearToDateOption());
                fullYear.setSelected(m.isEnableFullYearOption());
                lastMonth.setSelected(m.isEnableLastMonthOption());
                autosaveEnabled.setSelected(m.isAutosaveEnabled());
                autosaveInterval.getValueFactory().setValue(Math.max(1, m.getAutosaveIntervalMinutes()));
                status.setText("Company-level settings are enabled for " + services.sessionContext().activeCompanyDisplayLabel() + ".");
            }
            catch (Exception ex)
            {
                status.setText("Settings loaded with limited account choices: " + ex.getMessage());
            }
        }
        else
        {
            setCompanyControlsDisabled(true);
            status.setText("Company-level settings are disabled until a database and company are open.");
        }
        loaded = true;
    }

    private boolean canPersistCompanySettings()
    {
        return services.sessionContext().isCompanyOpen() && Database.isInitialized();
    }

    private void loadAccountChoices()
    {
        accountKeys.clear();
        for (Account account : services.accountLookup().listActivePostingAccounts())
        {
            String key = account.getCode() + " — " + account.getName();
            accountKeys.add(key);
        }
        incomeAccount.setItems(FXCollections.observableArrayList(accountKeys));
        expenseAccount.setItems(FXCollections.observableArrayList(accountKeys));
        setCompanyControlsDisabled(false);
    }

    private void setCompanyControlsDisabled(boolean disabled)
    {
        organizationName.setDisable(disabled); fiscalYearStart.setDisable(disabled); currency.setDisable(disabled);
        currencyFormat.setDisable(disabled); incomeAccount.setDisable(disabled); expenseAccount.setDisable(disabled);
        defaultReportPeriod.setDisable(disabled); yearToDate.setDisable(disabled); fullYear.setDisable(disabled); lastMonth.setDisable(disabled);
    }

    private static GridPane grid() { GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(12)); return g; }
    private static void add(GridPane g, int row, String label, Node node) { g.add(new Label(label), 0, row); g.add(node, 1, row); GridPane.setHgrow(node, Priority.ALWAYS); }
    private static Tab tab(String title, Node content) { Tab t = new Tab(title, content); t.setClosable(false); return t; }
    private static String blankToEmpty(String value) { return value == null ? "" : value; }
    private static String blankToDefault(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    boolean isLoaded() { return loaded; }
}
