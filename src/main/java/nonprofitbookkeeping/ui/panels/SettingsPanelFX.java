
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ReportPeriodPreset;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.ui.ThemeManager;
import nonprofitbookkeeping.util.FormatUtils;

// TODO: Auto-generated Javadoc
/**
 * JavaFX port of the original Swing {@code SettingsPanel}.
 */
public class SettingsPanelFX extends BorderPane
{
	
	/** The service. */
	private final SettingsService service;
	
	/** The primary stage. */
	private final Stage primaryStage;
	
	/** The on settings saved. */
	private final Runnable onSettingsSaved;
	
	/** The org name field. */
	private TextField orgNameField;
	
	/** The fiscal start field. */
	private TextField fiscalStartField;
	
	/** The currency box. */
	private ComboBox<String> currencyBox;
	
	/** The income account box. */
	private ComboBox<String> incomeAccountBox;
	
	/** The expense account box. */
	private ComboBox<String> expenseAccountBox;
	
	/** Donation edit posting policy selector. */
	private ComboBox<String> donationEditPolicyBox;
	
	/** The theme combo. */
	private ComboBox<String> themeCombo;
	
	/** The language combo. */
	private ComboBox<Locale> languageCombo;
	/** Pending-row text color preference combo. */
	private ComboBox<String> pendingRowTextColorCombo;
	/** Text field for customizing the currency format pattern. */
	private TextField currencyFormatField;
	
	/** The autosave enabled check. */
	private CheckBox autosaveEnabledCheck;
	
	/** The autosave interval spinner. */
	private Spinner<Integer> autosaveIntervalSpinner;
	
	/** The default directory field. */
	private TextField defaultDirectoryField;
	
	/** The last file field. */
	private TextField lastFileField;
	
	/** The default report period combo. */
	private ComboBox<ReportPeriodPreset> defaultReportPeriodCombo;
	
	/** The year to date option check. */
	private CheckBox yearToDateOptionCheck;
	
	/** The full year option check. */
	private CheckBox fullYearOptionCheck;
	
	/** The last month option check. */
	private CheckBox lastMonthOptionCheck;
	
	/**
	 * Constructs a new {@code SettingsPanelFX}.
	 *
	 * @param primaryStage reference to the main stage so theme changes can be applied
	 * @param service      settings service for persistence
	 */
	public SettingsPanelFX(Stage primaryStage, SettingsService service)
	{
		this(primaryStage, service, null);
		
	}
	
	/**
	 * Convenience constructor including a callback invoked after successful save.
	 *
	 * @param primaryStage the primary stage
	 * @param service the service
	 * @param onSettingsSaved the on settings saved
	 */
	public SettingsPanelFX(Stage primaryStage, SettingsService service,
		Runnable onSettingsSaved)
	{
		this.primaryStage = primaryStage;
		this.service = service;
		this.onSettingsSaved = onSettingsSaved;
		
		if (this.service != null && Database.isInitialized())
		{
			
			try
			{
				this.service.loadSettings(null);
				
				SettingsModel current = this.service.getSettings();
				FormatUtils.configureLocale(
					current.getLanguage() != null ?
						Locale.forLanguageTag(current.getLanguage()) :
						Locale.getDefault(),
					current.getDefaultCurrency());
				FormatUtils.setCurrencyFormat(current.getCurrencyFormat());
				
				if (this.primaryStage != null &&
					this.primaryStage.getScene() != null)
				{
					ThemeManager.applyTheme(this.primaryStage.getScene(),
						current.getTheme());
					
					if (current.getOrganizationName() != null &&
						!current.getOrganizationName().isBlank())
					{
						this.primaryStage
							.setTitle(current.getOrganizationName() +
								" - Nonprofit Bookkeeping");
					}
					else
					{
						this.primaryStage.setTitle("Nonprofit Bookkeeping");
					}
					
				}
				
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			
		}
		
		setPadding(new Insets(10));
		TabPane tabs = new TabPane();
		
		tabs.getTabs().addAll(companyInfoTab(),
			applicationTab(),
			uiPrefsTab());
		
		setCenter(tabs);
		
		Button saveBtn = new Button("Save Settings");
		saveBtn.setOnAction(e -> {
			
			if (this.service != null && Database.isInitialized())
			{
				collectFieldValues();
				
				try
				{
					this.service.saveSettings(null);
					
					if (this.primaryStage != null &&
						this.primaryStage.getScene() != null)
					{
						ThemeManager.applyTheme(this.primaryStage.getScene(),
							this.service.getSettings().getTheme());
						FormatUtils.setCurrencyFormat(
							this.service.getSettings().getCurrencyFormat());
						String org =
							this.service.getSettings().getOrganizationName();
						
						if (org != null && !org.isBlank())
						{
							this.primaryStage
								.setTitle(org + " - Nonprofit Bookkeeping");
						}
						else
						{
							this.primaryStage.setTitle("Nonprofit Bookkeeping");
						}
						
					}
					
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
			
		});
		setBottom(saveBtn);
		BorderPane.setMargin(saveBtn, new Insets(10));
		
	}
	
	/* ───────────────────────── Tab builders ───────────────────────── */
	
	/**
	 * Constructor SettingsPanelFX.
	 *
	 * @param stage the stage
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
		this.orgNameField
			.setTooltip(new Tooltip("Displayed in window titles and reports."));
		this.fiscalStartField = new TextField();
		this.fiscalStartField.setPromptText("MM-DD");
		this.fiscalStartField
			.setTooltip(new Tooltip("Fiscal year start in MM-DD format."));
		this.currencyBox = new ComboBox<>();
		this.currencyBox.getItems().addAll("USD", "EUR", "GBP");
		this.currencyBox.setTooltip(
			new Tooltip("Currency symbol applied to formatted amounts."));
		
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
		orgLabel.setTooltip(this.orgNameField.getTooltip());
		grid.add(orgLabel, 0, 0);
		grid.add(this.orgNameField, 1, 0);
		Label fiscalLabel = new Label("Fiscal Year Start:");
		fiscalLabel.setTooltip(this.fiscalStartField.getTooltip());
		grid.add(fiscalLabel, 0, 1);
		grid.add(this.fiscalStartField, 1, 1);
		Label currencyLabel = new Label("Default Currency:");
		currencyLabel.setTooltip(this.currencyBox.getTooltip());
		grid.add(currencyLabel, 0, 2);
		grid.add(this.currencyBox, 1, 2);
		
		TitledPane wrapper = titled("Company Information", grid);
		return new Tab("Company Info", wrapper);
		
	}
	
	
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
		this.donationEditPolicyBox = new ComboBox<>();
		this.donationEditPolicyBox.getItems().addAll("UPDATE_IN_PLACE",
			"REVERSE_AND_REPOST");
		
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
			if (m.getDonationEditPostingPolicy() != null &&
				!m.getDonationEditPostingPolicy().isBlank())
			{
				this.donationEditPolicyBox
					.setValue(m.getDonationEditPostingPolicy());
			}
		}
		if (this.donationEditPolicyBox.getValue() == null)
		{
			this.donationEditPolicyBox.setValue("UPDATE_IN_PLACE");
		}
		
		grid.add(new Label("Default Income Account:"), 0, 0);
		grid.add(this.incomeAccountBox, 1, 0);
		grid.add(new Label("Default Expense Account:"), 0, 1);
		grid.add(this.expenseAccountBox, 1, 1);
		grid.add(new Label("Donation Edit Posting:"), 0, 2);
		grid.add(this.donationEditPolicyBox, 1, 2);
		
		TitledPane wrapper = titled("Accounting Settings", grid);
		return new Tab("Accounting", wrapper);
		
	}
	
	/**
	 * Builds the "Application" tab containing autosave and reporting preferences.
	 *
	 * @return the tab
	 */
	private Tab applicationTab()
	{
		GridPane grid = grid(5, 2);
		
		this.autosaveEnabledCheck = new CheckBox("Enable background autosave");
		this.autosaveEnabledCheck
			.setTooltip(new Tooltip(
				"Automatically persist the open company at regular intervals."));
		this.autosaveIntervalSpinner = new Spinner<>(1, 240, 5);
		this.autosaveIntervalSpinner.setEditable(true);
		this.autosaveIntervalSpinner
			.setTooltip(new Tooltip("Minutes between automatic saves."));
		HBox autosaveControls = new HBox(10, this.autosaveEnabledCheck,
			new Label("Interval (minutes):"), this.autosaveIntervalSpinner);
		
		this.defaultDirectoryField = new TextField();
		this.defaultDirectoryField.setEditable(false);
		this.defaultDirectoryField
			.setTooltip(new Tooltip(
				"Used as the starting directory when creating companies."));
		Button chooseDir = new Button("Browse...");
		chooseDir.setTooltip(new Tooltip("Select a default directory."));
		chooseDir.setOnAction(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select Default Directory");
			
			if (this.defaultDirectoryField.getText() != null &&
				!this.defaultDirectoryField.getText().isBlank())
			{
				File current = new File(this.defaultDirectoryField.getText());
				
				if (current.exists())
				{
					chooser.setInitialDirectory(current.isDirectory() ?
						current : current.getParentFile());
				}
				
			}
			
			File selected = chooser.showDialog(this.primaryStage);
			
			if (selected != null)
			{
				this.defaultDirectoryField.setText(selected.getAbsolutePath());
			}
			
		});
		HBox dirBox = new HBox(8, this.defaultDirectoryField, chooseDir);
		HBox.setHgrow(this.defaultDirectoryField, Priority.ALWAYS);
		
		this.lastFileField = new TextField();
		this.lastFileField.setEditable(false);
		this.lastFileField.setTooltip(
			new Tooltip("Remembers the previously opened company file."));
		Button chooseFile = new Button("Browse...");
		chooseFile.setTooltip(new Tooltip("Select a recent company file."));
		chooseFile.setOnAction(e -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select Company File");
			
			if (this.lastFileField.getText() != null &&
				!this.lastFileField.getText().isBlank())
			{
				File current = new File(this.lastFileField.getText());
				
				if (current.exists())
				{
					chooser.setInitialDirectory(current.getParentFile());
					chooser.setInitialFileName(current.getName());
				}
				
			}
			
			File selected = chooser.showOpenDialog(this.primaryStage);
			
			if (selected != null)
			{
				this.lastFileField.setText(selected.getAbsolutePath());
			}
			
		});
		HBox fileBox = new HBox(8, this.lastFileField, chooseFile);
		HBox.setHgrow(this.lastFileField, Priority.ALWAYS);
		
		this.defaultReportPeriodCombo = new ComboBox<>();
		this.defaultReportPeriodCombo.getItems()
			.setAll(ReportPeriodPreset.values());
		this.defaultReportPeriodCombo
			.setTooltip(new Tooltip(
				"Default range applied when opening account details and reports."));
		this.defaultReportPeriodCombo.setConverter(new StringConverter<>()
		{
			@Override
			public String toString(ReportPeriodPreset preset)
			{
				
				if (preset == null)
				{
					return "";
				}
				
				return switch(preset)
				{
					case YEAR_TO_DATE -> "Year to Date";
					case FULL_YEAR -> "Full Fiscal Year";
					case LAST_MONTH -> "Last Month";
				};
				
			}
			
			@Override
			public ReportPeriodPreset fromString(String string)
			{
				return ReportPeriodPreset.fromString(string,
					ReportPeriodPreset.YEAR_TO_DATE);
				
			}
			
		});
		
		this.yearToDateOptionCheck = new CheckBox("Offer Year to Date");
		this.fullYearOptionCheck = new CheckBox("Offer Full Year");
		this.lastMonthOptionCheck = new CheckBox("Offer Last Month");
		VBox reportOptions =
			new VBox(6, this.yearToDateOptionCheck, this.fullYearOptionCheck,
				this.lastMonthOptionCheck);
		
		if (this.service != null)
		{
			SettingsModel m = this.service.getSettings();
			this.autosaveEnabledCheck.setSelected(m.isAutosaveEnabled());
			this.autosaveIntervalSpinner.getValueFactory()
				.setValue(m.getAutosaveIntervalMinutes());
			this.defaultDirectoryField.setText(m.getDefaultCompanyDirectory());
			this.lastFileField.setText(m.getLastUsedCompanyFile());
			this.defaultReportPeriodCombo.setValue(ReportPeriodPreset
				.fromString(m.getDefaultReportPeriod(),
					ReportPeriodPreset.YEAR_TO_DATE));
			this.yearToDateOptionCheck
				.setSelected(m.isEnableYearToDateOption());
			this.fullYearOptionCheck.setSelected(m.isEnableFullYearOption());
			this.lastMonthOptionCheck.setSelected(m.isEnableLastMonthOption());
		}
		else
		{
			this.autosaveEnabledCheck.setSelected(true);
			this.autosaveIntervalSpinner.getValueFactory().setValue(5);
			this.defaultReportPeriodCombo
				.setValue(ReportPeriodPreset.YEAR_TO_DATE);
			this.yearToDateOptionCheck.setSelected(true);
			this.fullYearOptionCheck.setSelected(true);
			this.lastMonthOptionCheck.setSelected(true);
		}
		
		grid.add(new Label("Autosave:"), 0, 0);
		grid.add(autosaveControls, 1, 0);
		grid.add(new Label("Default Directory:"), 0, 1);
		grid.add(dirBox, 1, 1);
		grid.add(new Label("Last Used File:"), 0, 2);
		grid.add(fileBox, 1, 2);
		grid.add(new Label("Default Report Period:"), 0, 3);
		grid.add(this.defaultReportPeriodCombo, 1, 3);
		grid.add(new Label("Enable Report Filters:"), 0, 4);
		grid.add(reportOptions, 1, 4);
		
		TitledPane wrapper = titled("Application Settings", grid);
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
	
	/**
	 * Creates the database backup.
	 */
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
				alert("Failed to save the current company before backup: " +
					e.getMessage());
				return;
			}
			
		}
		
		FileChooser fc = new FileChooser();
		fc.setTitle("Export Database Script");
		fc.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("SQL Script", "*.sql"));
		File out = fc.showSaveDialog(this.primaryStage);
		
		if (out == null)
		{
			return;
		}
		
		String target =
			out.toPath().toAbsolutePath().toString().replace("'", "''");
		
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
	
	/**
	 * Restore database backup.
	 */
	private void restoreDatabaseBackup()
	{
		
		if (!Database.isInitialized())
		{
			alert("Initialize the H2 database before importing a backup.");
			return;
		}
		
		FileChooser fc = new FileChooser();
		fc.setTitle("Import Database Script");
		fc.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("SQL Script", "*.sql"));
		File in = fc.showOpenDialog(this.primaryStage);
		
		if (in == null)
		{
			return;
		}
		
		String source =
			in.toPath().toAbsolutePath().toString().replace("'", "''");
		
		try (Connection connection = Database.get().getConnection();
			Statement statement = connection.createStatement())
		{
			statement.execute("RUNSCRIPT FROM '" + source + "'");
			CurrentCompany.close();
			alert("Database restored from " + in.getName() +
				". Reopen a company to continue working.");
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
		this.languageCombo.getItems().addAll(
			Locale.forLanguageTag("en-US"),
			Locale.forLanguageTag("es-ES"),
			Locale.forLanguageTag("fr-FR"));
		this.languageCombo.setConverter(new StringConverter<>()
		{
			@Override
			public String toString(Locale locale)
			{
				
				if (locale == null)
				{
					return "";
				}
				
				return locale.getDisplayLanguage(locale) + " (" +
					locale.getCountry() + ")";
				
			}
			
			@Override
			public Locale fromString(String string)
			{
				return null;
				
			}
			
		});
		this.languageCombo.setValue(Locale.forLanguageTag("en-US"));
		
		if (this.service != null)
		{
			String lang = this.service.getSettings().getLanguage();
			
			if (lang != null && !lang.isBlank())
			{
				this.languageCombo.setValue(Locale.forLanguageTag(lang));
			}
			
		}
		
		grid.add(this.languageCombo, 1, 1);
		
		grid.add(new Label("Pending Row Text:"), 0, 2);
		this.pendingRowTextColorCombo = new ComboBox<>();
		this.pendingRowTextColorCombo.getItems().addAll("Black", "System");
		this.pendingRowTextColorCombo.setValue("Black");
		
		if (this.service != null)
		{
			String pendingRowTextColor =
				this.service.getSettings().getPendingRowTextColor();
			if (pendingRowTextColor != null && !pendingRowTextColor.isBlank())
			{
				this.pendingRowTextColorCombo.setValue(pendingRowTextColor);
			}
		}
		
		grid.add(this.pendingRowTextColorCombo, 1, 2);
		
		grid.add(new Label("Currency Format:"), 0, 3);
		this.currencyFormatField = new TextField();
		
		if (this.service != null)
		{
			String fmt = this.service.getSettings().getCurrencyFormat();
			if (fmt != null)
				this.currencyFormatField.setText(fmt);
			else
				this.currencyFormatField
					.setText(FormatUtils.getCurrencyFormat());
		}
		else
		{
			this.currencyFormatField.setText(FormatUtils.getCurrencyFormat());
		}
		
		grid.add(this.currencyFormatField, 1, 3);
		
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
	@SuppressWarnings("unused")
	private static GridPane grid(int rows, int cols)
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
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK)
			.showAndWait();
		
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
		if (this.incomeAccountBox != null &&
			this.incomeAccountBox.getValue() != null)
			m.setDefaultIncomeAccount(this.incomeAccountBox.getValue());
		if (this.expenseAccountBox != null &&
			this.expenseAccountBox.getValue() != null)
			m.setDefaultExpenseAccount(this.expenseAccountBox.getValue());
		if (this.donationEditPolicyBox != null &&
			this.donationEditPolicyBox.getValue() != null)
		{
			m.setDonationEditPostingPolicy(this.donationEditPolicyBox.getValue());
		}
		
		if (this.themeCombo != null && this.themeCombo.getValue() != null)
			m.setTheme(this.themeCombo.getValue());

		if (this.pendingRowTextColorCombo != null &&
			this.pendingRowTextColorCombo.getValue() != null)
		{
			m.setPendingRowTextColor(this.pendingRowTextColorCombo.getValue());
		}
			
		if (this.languageCombo != null && this.languageCombo.getValue() != null)
		{
			Locale locale = this.languageCombo.getValue();
			m.setLanguage(locale.toLanguageTag());
			FormatUtils.configureLocale(locale,
				this.currencyBox != null ? this.currencyBox.getValue() : null);
		}
		
		if (this.currencyFormatField != null &&
			!this.currencyFormatField.getText().isEmpty())
			m.setCurrencyFormat(this.currencyFormatField.getText());
		
		if (this.autosaveEnabledCheck != null)
			m.setAutosaveEnabled(this.autosaveEnabledCheck.isSelected());
		if (this.autosaveIntervalSpinner != null &&
			this.autosaveIntervalSpinner.getValue() != null)
			m.setAutosaveIntervalMinutes(
				this.autosaveIntervalSpinner.getValue());
		if (this.defaultDirectoryField != null)
			m.setDefaultCompanyDirectory(this.defaultDirectoryField.getText());
		if (this.lastFileField != null)
			m.setLastUsedCompanyFile(this.lastFileField.getText());
		if (this.defaultReportPeriodCombo != null &&
			this.defaultReportPeriodCombo.getValue() != null)
			m.setDefaultReportPeriod(
				this.defaultReportPeriodCombo.getValue().name());
		if (this.yearToDateOptionCheck != null)
			m.setEnableYearToDateOption(
				this.yearToDateOptionCheck.isSelected());
		if (this.fullYearOptionCheck != null)
			m.setEnableFullYearOption(this.fullYearOptionCheck.isSelected());
		if (this.lastMonthOptionCheck != null)
			m.setEnableLastMonthOption(this.lastMonthOptionCheck.isSelected());
		
	}
	
}
