
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Year;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import nonprofitbookkeeping.model.SettingsModel.DefaultReportPeriod;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.ui.ThemeManager;
import nonprofitbookkeeping.util.FormatUtils;

/**
 * JavaFX port of the original Swing {@code SettingsPanel}.
 */
public class SettingsPanelFX extends BorderPane
{
        private final SettingsService service;
        private final Stage primaryStage;
        private final Consumer<SettingsModel> onSettingsSaved;

        private TextField orgNameField;
        private TextField fiscalStartField;
        private ComboBox<String> currencyBox;
        private ComboBox<String> incomeAccountBox;
        private ComboBox<String> expenseAccountBox;
       private ComboBox<String> themeCombo;
       private ComboBox<String> languageCombo;
       /** Text field for customizing the currency format pattern. */
       private TextField currencyFormatField;
       /** Spinner to configure the autosave interval. */
       private Spinner<Integer> autosaveSpinner;
       /** Field storing the default directory used by file choosers. */
       private TextField defaultDirectoryField;
       /** Field storing the last opened file location. */
       private TextField lastFileField;
       /** Combo box to select the default report period. */
       private ComboBox<DefaultReportPeriod> defaultPeriodCombo;
       /** Spinner to capture the fiscal/reporting year when a full year is selected. */
       private Spinner<Integer> reportYearSpinner;
       /** Stores the map of supported languages to locales. */
       private Map<String, Locale> availableLocales;

        /**
         * Constructs a new {@code SettingsPanelFX}.
	 *
	* @param primaryStage reference to the main stage so theme changes can be applied
	 * @param service      settings service for persistence
	 * @param companyDir   directory of the current company
	 */
        public SettingsPanelFX(Stage primaryStage, SettingsService service)
        {
                this(primaryStage, service, null);
        }

        public SettingsPanelFX(Stage primaryStage, SettingsService service,
                Consumer<SettingsModel> onSettingsSaved)
        {
                this.primaryStage = primaryStage;
                this.service = service;
                this.onSettingsSaved = onSettingsSaved;

                if (this.service != null && Database.isInitialized())
                {

                        try
                        {
                                this.service.loadSettings(null);

                               if (this.primaryStage != null && this.primaryStage.getScene() != null)
                               {
                                       ThemeManager.applyTheme(this.primaryStage.getScene(),
                                               this.service.getSettings().getTheme());
                                       FormatUtils.setCurrencyLocale(this.service.getSettings().getCurrencyLocale());
                                       FormatUtils.setCurrencyFormat(this.service.getSettings().getCurrencyFormat());
                               }

                        }
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			
		}
		
		setPadding(new Insets(10));
		TabPane tabs = new TabPane();
		
                this.availableLocales = Map.of(
                        "English (United States)", Locale.US,
                        "English (United Kingdom)", Locale.UK,
                        "French (France)", Locale.FRANCE,
                        "Spanish (Spain)", new Locale("es", "ES"),
                        "Canadian English", Locale.CANADA);

                tabs.getTabs().addAll(companyInfoTab(), accountingTab(), reportsTab(), applicationTab(),
                        backupTab(), uiPrefsTab());
		
		setCenter(tabs);
		
		Button saveBtn = new Button("Save Settings");
		saveBtn.setOnAction(e -> {
			
                        if (this.service != null && Database.isInitialized())
                        {
                                collectFieldValues();

                                try
                                {
                                        this.service.saveSettings(null);

                                       if (this.primaryStage != null && this.primaryStage.getScene() != null)
                                       {
                                       ThemeManager.applyTheme(this.primaryStage.getScene(),
                                               this.service.getSettings().getTheme());
                                       SettingsModel saved = this.service.getSettings();
                                       FormatUtils.setCurrencyLocale(saved.getCurrencyLocale());
                                       FormatUtils.setCurrencyFormat(saved.getCurrencyFormat());
                               }

                                alert("Settings saved");

                               if (this.onSettingsSaved != null)
                               {
                                       this.onSettingsSaved.accept(this.service.getSettings());
                               }
                        }
				catch (IOException ex)
				{
					alert("Failed to save settings: " + ex.getMessage());
				}
				
			}
			
		});
		setBottom(saveBtn);
		BorderPane.setMargin(saveBtn, new Insets(10));
	}
	
	/* ───────────────────────── Tab builders ───────────────────────── */
	
	/**  
		 * Constructor SettingsPanelFX
		 * @param stage
		 */
        public SettingsPanelFX(Stage stage)
        {
                this(stage, new SettingsService(), null);
        }
	
	/**
	 * Builds and returns the "Company Info" tab for the settings panel.
	 * This tab contains fields for editing basic organization information such as
	 * name, fiscal year start, and default currency.
	 *
	 * @return A {@link Tab} configured with company information settings.
	 */
	private Tab companyInfoTab()
	{
		GridPane grid = grid(3, 2);
		this.orgNameField = new TextField();
		this.fiscalStartField = new TextField();
		this.currencyBox = new ComboBox<>();
                this.currencyBox.getItems().addAll("USD", "EUR", "GBP");
                this.currencyBox.setTooltip(new Tooltip("Select the default currency for new companies."));

                if (this.service != null)
                {
			SettingsModel m = this.service.getSettings();
			if (m.getOrganizationName() != null)
				this.orgNameField.setText(m.getOrganizationName());
			if (m.getFiscalYearStart() != null)
				this.fiscalStartField.setText(m.getFiscalYearStart());
			if (m.getDefaultCurrency() != null)
				this.currencyBox.setValue(m.getDefaultCurrency());
			else
				this.currencyBox.setValue("USD");
		}
		else
		{
			this.currencyBox.setValue("USD");
		}
		
                Label orgLabel = new Label("Organization Name:");
                orgLabel.setTooltip(new Tooltip("Displayed in reports and exported documents."));
                grid.add(orgLabel, 0, 0);
                grid.add(this.orgNameField, 1, 0);
                Label fiscalLabel = new Label("Fiscal Year Start:");
                fiscalLabel.setTooltip(new Tooltip("Specify as MM-DD. Used for default reporting ranges."));
                grid.add(fiscalLabel, 0, 1);
                grid.add(this.fiscalStartField, 1, 1);
                Label currencyLabel = new Label("Default Currency:");
                currencyLabel.setTooltip(new Tooltip("Applied to new companies that do not specify a currency."));
                grid.add(currencyLabel, 0, 2);
                grid.add(this.currencyBox, 1, 2);
		
		TitledPane wrapper = titled("Company Information", grid);
		return new Tab("Company Info", wrapper);
	}
	
	/**
	 * Builds and returns the "Users" tab for the settings panel.
	 * This tab displays a table of users ({@link UserRow}) with their usernames and roles.
	 * Currently, it shows demo data.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * uses reflection and can lead to type safety warnings if property names don't strictly match
	 * Java bean conventions or if raw types are inferred. "deprecation" might relate to older patterns.
	 * 
	 * @return A {@link Tab} configured with user management settings.
	 */
        /**
         * Builds and returns the "Accounting" tab for the settings panel.
         * This tab includes settings related to default accounts (income, expense)
         * and options like auto-numbering for vouchers.
         *
         * @return A {@link Tab} configured with accounting-related settings.
         */
        private Tab accountingTab()
        {
                GridPane grid = grid(2, 2);
		this.incomeAccountBox = new ComboBox<>();
		this.expenseAccountBox = new ComboBox<>();
		
		ChartOfAccounts coa = CurrentCompany.getCompany() != null ?
			CurrentCompany.getCompany().getChartOfAccounts() : null;
		
		if (coa != null)
		{
			
			for (nonprofitbookkeeping.model.Account a : coa.getAccounts())
			{
				String name = a.getName();
				this.incomeAccountBox.getItems().add(name);
				this.expenseAccountBox.getItems().add(name);
			}
			
		}
		
		if (this.service != null)
		{
			SettingsModel m = this.service.getSettings();
			if (m.getDefaultIncomeAccount() != null)
				this.incomeAccountBox.setValue(m.getDefaultIncomeAccount());
			if (m.getDefaultExpenseAccount() != null)
				this.expenseAccountBox.setValue(m.getDefaultExpenseAccount());
		}
		
                Label defaultIncome = new Label("Default Income Account:");
                defaultIncome.setTooltip(new Tooltip("Used when creating new revenue transactions."));
                grid.add(defaultIncome, 0, 0);
                grid.add(this.incomeAccountBox, 1, 0);
                Label defaultExpense = new Label("Default Expense Account:");
                defaultExpense.setTooltip(new Tooltip("Used when creating new expense transactions."));
                grid.add(defaultExpense, 0, 1);
                grid.add(this.expenseAccountBox, 1, 1);

                TitledPane wrapper = titled("Accounting Settings", grid);
                return new Tab("Accounting", wrapper);
        }

        /**
         * Builds the Reports tab where users can select default reporting periods.
         *
         * @return configured reports {@link Tab}
         */
        private Tab reportsTab()
        {
                GridPane grid = grid(2, 2);

                this.defaultPeriodCombo = new ComboBox<>();
                this.defaultPeriodCombo.getItems().addAll(DefaultReportPeriod.values());
                this.defaultPeriodCombo.setTooltip(new Tooltip(
                        "Select the default date range used in Account Details and reports."));

                this.reportYearSpinner = new Spinner<>();
                int currentYear = Year.now().getValue();
                SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100, currentYear);
                this.reportYearSpinner.setValueFactory(factory);
                this.reportYearSpinner.setEditable(true);
                this.reportYearSpinner.setTooltip(new Tooltip(
                        "Fiscal year to use when the default period spans an entire year."));

                if (this.service != null)
                {
                        SettingsModel m = this.service.getSettings();
                        this.defaultPeriodCombo.setValue(m.getDefaultReportPeriodEnum());
                        if (m.getDefaultReportYear() != null)
                        {
                                this.reportYearSpinner.getValueFactory().setValue(m.getDefaultReportYear());
                        }
                }
                else
                {
                        this.defaultPeriodCombo.setValue(DefaultReportPeriod.YEAR_TO_DATE);
                }

                Label periodLabel = new Label("Default Period:");
                periodLabel.setTooltip(this.defaultPeriodCombo.getTooltip());
                Label yearLabel = new Label("Fiscal Year:");
                yearLabel.setTooltip(this.reportYearSpinner.getTooltip());

                grid.add(periodLabel, 0, 0);
                grid.add(this.defaultPeriodCombo, 1, 0);
                grid.add(yearLabel, 0, 1);
                grid.add(this.reportYearSpinner, 1, 1);

                TitledPane wrapper = titled("Reporting Defaults", grid);
                return new Tab("Reports", wrapper);
        }

        /**
         * Builds the Application tab with autosave and filesystem preferences.
         *
         * @return application {@link Tab}
         */
        private Tab applicationTab()
        {
                GridPane grid = grid(3, 2);

                this.autosaveSpinner = new Spinner<>();
                SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 720, 5);
                this.autosaveSpinner.setValueFactory(valueFactory);
                this.autosaveSpinner.setEditable(true);
                this.autosaveSpinner.setTooltip(new Tooltip(
                        "Minutes between automatic saves. Set to 0 to disable autosave."));

                this.defaultDirectoryField = new TextField();
                this.defaultDirectoryField.setEditable(false);
                this.defaultDirectoryField.setTooltip(new Tooltip(
                        "Default folder shown when opening or exporting files."));
                Button browseDefaultDir = new Button("Browse...");
                browseDefaultDir.setOnAction(e -> chooseDefaultDirectory());
                browseDefaultDir.setTooltip(new Tooltip("Pick the directory used by file choosers."));

                this.lastFileField = new TextField();
                this.lastFileField.setEditable(false);
                this.lastFileField.setTooltip(new Tooltip(
                        "Remember the most recently opened company or data file."));
                Button browseLastFile = new Button("Select...");
                browseLastFile.setOnAction(e -> chooseLastFile());
                browseLastFile.setTooltip(new Tooltip("Choose the last company/data file you worked with."));

                if (this.service != null)
                {
                        SettingsModel m = this.service.getSettings();
                        this.autosaveSpinner.getValueFactory().setValue(m.getAutosaveIntervalMinutes());
                        if (m.getDefaultDirectory() != null)
                        {
                                this.defaultDirectoryField.setText(m.getDefaultDirectory());
                        }
                        else
                        {
                                this.defaultDirectoryField.setText(PreferencesManager.getLastDirectory());
                        }
                        if (m.getLastOpenedFile() != null)
                        {
                                this.lastFileField.setText(m.getLastOpenedFile());
                        }
                        else
                        {
                                String lastDb = PreferencesManager.getLastDatabasePath();
                                if (lastDb != null)
                                {
                                        this.lastFileField.setText(lastDb);
                                }
                        }
                }

                Label autosaveLabel = new Label("Autosave Interval (minutes):");
                autosaveLabel.setTooltip(this.autosaveSpinner.getTooltip());
                Label defaultDirLabel = new Label("Default Directory:");
                defaultDirLabel.setTooltip(this.defaultDirectoryField.getTooltip());
                Label lastFileLabel = new Label("Last Used File:");
                lastFileLabel.setTooltip(this.lastFileField.getTooltip());

                grid.add(autosaveLabel, 0, 0);
                grid.add(this.autosaveSpinner, 1, 0);
                grid.add(new Label(), 2, 0);

                grid.add(defaultDirLabel, 0, 1);
                grid.add(this.defaultDirectoryField, 1, 1);
                grid.add(browseDefaultDir, 2, 1);

                grid.add(lastFileLabel, 0, 2);
                grid.add(this.lastFileField, 1, 2);
                grid.add(browseLastFile, 2, 2);

                TitledPane wrapper = titled("Application Preferences", grid);
                return new Tab("Application", wrapper);
        }
	
	/**
	 * Builds and returns the "Backup" tab for the settings panel.
	 * This tab provides buttons for creating and restoring backups.
	 * Current actions are placeholders that show alert messages.
	 * 
	 * @return A {@link Tab} configured with backup and restore options.
	 */
        private Tab backupTab()
        {
                HBox box = new HBox(10);
                Button backupBtn = new Button("Create Backup");
                Button restoreBtn = new Button("Restore Backup");

                backupBtn.setOnAction(e -> createDatabaseBackup());
                restoreBtn.setOnAction(e -> restoreDatabaseBackup());

                box.getChildren().addAll(backupBtn, restoreBtn);
                box.setPadding(new Insets(10));

                TitledPane wrapper = titled("Backup & Restore", box);
                return new Tab("Backup", wrapper);
        }

        private void createDatabaseBackup()
        {
                if (!Database.isInitialized())
                {
                        alert("Initialize the H2 database before exporting a backup.");
                        return;
                }

                if (CurrentCompany.isOpen())
                {
                        try
                        {
                                CurrentCompany.persist();
                        }
                        catch (IOException e)
                        {
                                alert("Failed to save the current company before backup: " + e.getMessage());
                                return;
                        }
                }

                FileChooser fc = new FileChooser();
                fc.setTitle("Export Database Script");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Script", "*.sql"));
                File out = fc.showSaveDialog(this.primaryStage);

                if (out == null)
                {
                        return;
                }

                String target = out.toPath().toAbsolutePath().toString().replace("'", "''");

                try (Connection connection = Database.get().getConnection();
                        Statement statement = connection.createStatement())
                {
                        statement.execute("SCRIPT TO '" + target + "'");
                        alert("Database script exported to " + out.getAbsolutePath());
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
                        alert("Initialize the H2 database before importing a backup.");
                        return;
                }

                FileChooser fc = new FileChooser();
                fc.setTitle("Import Database Script");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Script", "*.sql"));
                File in = fc.showOpenDialog(this.primaryStage);

                if (in == null)
                {
                        return;
                }

                String source = in.toPath().toAbsolutePath().toString().replace("'", "''");

                try (Connection connection = Database.get().getConnection();
                        Statement statement = connection.createStatement())
                {
                        statement.execute("RUNSCRIPT FROM '" + source + "'");
                        CurrentCompany.close();
                        alert("Database restored from " + in.getName()
                                + ". Reopen a company to continue working.");
                }
                catch (SQLException ex)
                {
                        alert("Restore failed: " + ex.getMessage());
                }
        }
	
	/**
	 * Builds and returns the "UI Preferences" tab for the settings panel.
	 * This tab includes options for selecting UI theme and language.
	 * 
	 * @return A {@link Tab} configured with UI preference settings.
	 */
	private Tab uiPrefsTab()
	{
		GridPane grid = grid(2, 2);
		
                grid.add(new Label("Theme:"), 0, 0);
                this.themeCombo = new ComboBox<>();
                this.themeCombo.getItems().addAll("Light", "Dark", "System");
                this.themeCombo.setValue("System");
		
		if (this.service != null)
		{
			String theme = this.service.getSettings().getTheme();
			if (theme != null)
				this.themeCombo.setValue(theme);
		}
		
		grid.add(this.themeCombo, 1, 0);
		
                grid.add(new Label("Language:"), 0, 1);
                this.languageCombo = new ComboBox<>();
                this.languageCombo.getItems().addAll(this.availableLocales.keySet());
                this.languageCombo.setTooltip(new Tooltip("Select UI language and locale."));

                if (this.service != null)
                {
                        String lang = this.service.getSettings().getLanguage();
                        if (lang != null && this.availableLocales.containsKey(lang))
                        {
                                this.languageCombo.setValue(lang);
                        }
                        else
                        {
                                Locale locale = this.service.getSettings().getCurrencyLocale();
                                this.languageCombo.setValue(this.availableLocales.entrySet().stream()
                                        .filter(e -> e.getValue().equals(locale))
                                        .map(Map.Entry::getKey).findFirst()
                                        .orElse("English (United States)"));
                        }
                }
                else
                {
                        this.languageCombo.setValue("English (United States)");
                }

               grid.add(this.languageCombo, 1, 1);

               grid.add(new Label("Currency Format:"), 0, 2);
               this.currencyFormatField = new TextField();

               if (this.service != null)
               {
                       String fmt = this.service.getSettings().getCurrencyFormat();
                       if (fmt != null)
                               this.currencyFormatField.setText(fmt);
                       else
                               this.currencyFormatField.setText(FormatUtils.getCurrencyFormat());
               }
               else
               {
                       this.currencyFormatField.setText(FormatUtils.getCurrencyFormat());
               }

               grid.add(this.currencyFormatField, 1, 2);
		
		TitledPane wrapper = titled("UI Preferences", grid);
		return new Tab("UI Preferences", wrapper);
	}
	
	/* ───────────────────────── Helpers ───────────────────────── */
	
	/**
	 * Utility method to create a {@link GridPane} with a predefined two-column layout
	 * (each 50% width) and standard padding and gap settings.
	 * The {@code @SuppressWarnings("unused")} indicates that the parameters {@code rows} and {@code cols}
	 * are not currently used in the method body, though they might have been intended for future use
	 * or were part of an earlier design.
	 *
	 * @param rows The number of rows intended for the grid (currently unused).
	 * @param cols The number of columns intended for the grid (currently unused, fixed at 2).
	 * @return A configured {@link GridPane} instance.
	 */
	@SuppressWarnings("unused") private static GridPane grid(int rows, int cols)
	{
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(10);
		g.setPadding(new Insets(10));
		ColumnConstraints cc = new ColumnConstraints();
		cc.setPercentWidth(50);
		g.getColumnConstraints().addAll(cc, cc);
		return g;
	}
	
	/**
	 * Creates a non-collapsible {@link TitledPane} with the given title and content node.
	 * This is a utility method for wrapping content within tabs or sections of the settings panel.
	 * 
	 * @param title The title to be displayed on the pane.
	 * @param content The {@link javafx.scene.Node} to be set as the content of the pane.
	 * @return A configured {@link TitledPane} instance.
	 */
	private static TitledPane titled(String title, javafx.scene.Node content)
	{
		TitledPane tp = new TitledPane(title, content);
		tp.setCollapsible(false);
		return tp;
	}
	
	/**
	 * Displays a simple informational alert dialog with an OK button.
	 * 
	 * @param msg The message to be displayed in the alert dialog.
	 */
        private static void alert(String msg)
        {
                new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
        }

        /**
         * Collects values from the UI controls into the {@link SettingsModel} held by the service.
         */
	private void collectFieldValues()
	{
		
		if (this.service == null)
		{
			return;
		}
		
		SettingsModel m = this.service.getSettings();
		if (this.orgNameField != null)
			m.setOrganizationName(this.orgNameField.getText());
		if (this.fiscalStartField != null)
			m.setFiscalYearStart(this.fiscalStartField.getText());
		if (this.currencyBox != null && this.currencyBox.getValue() != null)
			m.setDefaultCurrency(this.currencyBox.getValue());
		if (this.incomeAccountBox != null && this.incomeAccountBox.getValue() != null)
			m.setDefaultIncomeAccount(this.incomeAccountBox.getValue());
		if (this.expenseAccountBox != null && this.expenseAccountBox.getValue() != null)
			m.setDefaultExpenseAccount(this.expenseAccountBox.getValue());
		
                if (this.themeCombo != null && this.themeCombo.getValue() != null)
                        m.setTheme(this.themeCombo.getValue());
                if (this.languageCombo != null && this.languageCombo.getValue() != null)
                {
                        String selection = this.languageCombo.getValue();
                        m.setLanguage(selection);
                        Locale locale = this.availableLocales.getOrDefault(selection, Locale.US);
                        m.setCurrencyLocale(locale);
                        FormatUtils.setCurrencyLocale(locale);
                }
               if (this.currencyFormatField != null)
               {
                       String text = this.currencyFormatField.getText();
                       if (text == null || text.isBlank())
                       {
                               m.setCurrencyFormat(null);
                               FormatUtils.setCurrencyFormat(null);
                       }
                       else
                       {
                               m.setCurrencyFormat(text);
                               FormatUtils.setCurrencyFormat(text);
                       }
               }

               if (this.autosaveSpinner != null)
               {
                       m.setAutosaveIntervalMinutes(this.autosaveSpinner.getValue());
               }

               if (this.defaultDirectoryField != null)
               {
                       String path = this.defaultDirectoryField.getText();
                       m.setDefaultDirectory(path == null || path.isBlank() ? null : path);
                       if (path != null && !path.isBlank())
                       {
                               PreferencesManager.setLastDirectory(path);
                               PreferencesManager.setLastWriteDirectory(path);
                       }
               }

               if (this.lastFileField != null)
               {
                       String file = this.lastFileField.getText();
                       m.setLastOpenedFile(file == null || file.isBlank() ? null : file);
                       if (file != null && !file.isBlank())
                       {
                               PreferencesManager.setLastDatabasePath(file);
                       }
               }

               if (this.defaultPeriodCombo != null && this.defaultPeriodCombo.getValue() != null)
               {
                       m.setDefaultReportPeriodEnum(this.defaultPeriodCombo.getValue());
               }

               if (this.reportYearSpinner != null && this.reportYearSpinner.getValue() != null)
               {
                       m.setDefaultReportYear(this.reportYearSpinner.getValue());
               }

        }

        private void chooseDefaultDirectory()
        {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Select Default Directory");
                if (this.defaultDirectoryField != null && !this.defaultDirectoryField.getText().isBlank())
                {
                        File current = new File(this.defaultDirectoryField.getText());
                        if (current.exists())
                        {
                                chooser.setInitialDirectory(current);
                        }
                }
                File selected = chooser.showDialog(this.primaryStage);
                if (selected != null)
                {
                        this.defaultDirectoryField.setText(selected.getAbsolutePath());
                }
        }

        private void chooseLastFile()
        {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select Last Used File");
                File selected = chooser.showOpenDialog(this.primaryStage);
                if (selected != null)
                {
                        this.lastFileField.setText(selected.getAbsolutePath());
                }
        }

}
