
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
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
	
	private TextField orgNameField;
	private TextField fiscalStartField;
	private ComboBox<String> currencyBox;
	private TableView<UserRow> userTable;
	private ComboBox<String> incomeAccountBox;
	private ComboBox<String> expenseAccountBox;
       private ComboBox<String> themeCombo;
       private ComboBox<String> languageCombo;
       /** Text field for customizing the currency format pattern. */
       private TextField currencyFormatField;
	
	/**
	 * Constructs a new {@code SettingsPanelFX}.
	 *
	* @param primaryStage reference to the main stage so theme changes can be applied
	 * @param service      settings service for persistence
	 * @param companyDir   directory of the current company
	 */
        public SettingsPanelFX(Stage primaryStage, SettingsService service)
        {
                this.primaryStage = primaryStage;
                this.service = service;

                if (this.service != null && Database.isInitialized())
                {

                        try
                        {
                                this.service.loadSettings(null);

                               if (this.primaryStage != null && this.primaryStage.getScene() != null)
                               {
                                       ThemeManager.applyTheme(this.primaryStage.getScene(),
                                               this.service.getSettings().getTheme());
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
		
		tabs.getTabs().addAll(companyInfoTab(), usersTab(), accountingTab(), backupTab(),
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

                                       if (this.primaryStage != null && this.primaryStage.getScene() != null)
                                       {
                                               ThemeManager.applyTheme(this.primaryStage.getScene(),
                                                       this.service.getSettings().getTheme());
                                               FormatUtils.setCurrencyFormat(this.service.getSettings().getCurrencyFormat());
                                       }
					
					alert("Settings saved");
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
                this(stage, new SettingsService());
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
		
		grid.add(new Label("Organization Name:"), 0, 0);
		grid.add(this.orgNameField, 1, 0);
		grid.add(new Label("Fiscal Year Start:"), 0, 1);
		grid.add(this.fiscalStartField, 1, 1);
		grid.add(new Label("Default Currency:"), 0, 2);
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
	@SuppressWarnings(
	{ "unchecked", "deprecation" }) private Tab usersTab()
	{
		this.userTable = new TableView<>();
		TableColumn<UserRow, String> userCol = new TableColumn<>("Username");
		userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
		TableColumn<UserRow, String> roleCol = new TableColumn<>("Role");
		roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
		this.userTable.getColumns().addAll(userCol, roleCol);
		this.userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		if (this.service != null)
		{
			
			for (SettingsModel.User u : this.service.getSettings().getUsers())
			{
				this.userTable.getItems().add(new UserRow(u.getUsername(), u.getRole()));
			}
			
		}
		
		TitledPane wrapper = titled("User Management", this.userTable);
		return new Tab("Users", wrapper);
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
		
		grid.add(new Label("Default Income Account:"), 0, 0);
		grid.add(this.incomeAccountBox, 1, 0);
		grid.add(new Label("Default Expense Account:"), 0, 1);
		grid.add(this.expenseAccountBox, 1, 1);
		
		TitledPane wrapper = titled("Accounting Settings", grid);
		return new Tab("Accounting", wrapper);
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
		this.languageCombo.getItems().addAll("English", "Spanish", "French");
		this.languageCombo.setValue("English");
		
		if (this.service != null)
		{
			String lang = this.service.getSettings().getLanguage();
			if (lang != null)
				this.languageCombo.setValue(lang);
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
                        m.setLanguage(this.languageCombo.getValue());
               if (this.currencyFormatField != null && !this.currencyFormatField.getText().isEmpty())
                       m.setCurrencyFormat(this.currencyFormatField.getText());
		
		// update user list from table
		if (this.userTable != null)
		{
			java.util.List<SettingsModel.User> list = new java.util.ArrayList<>();
			
			for (UserRow row : this.userTable.getItems())
			{
				list.add(new SettingsModel.User(row.getUsername(), row.getRole()));
			}
			
			m.setUsers(list);
		}
		
	}
	
	/* ───────────────────────── Table model ───────────────────────── */
	/**
	 * Represents a single row in the user management table within the "Users" tab.
	 * This class holds username and role information for display.
	 */
	public static class UserRow
	{
		/** The username of the user. */
		private final String username;
		/** The role assigned to the user (e.g., "Administrator", "Viewer"). */
		private final String role;
		
		/**
		 * Constructs a new {@code UserRow}.
		 *
		 * @param u The username for this user row.
		 * @param r The role for this user row.
		 */
		public UserRow(String u, String r)
		{
			this.username = u;
			this.role = r;
		}
		
		/**
		 * Gets the username.
		 * @return The username string.
		 */
		public String getUsername()
		{
			return this.username;
		}
		
		/**
		 * Gets the role.
		 * @return The role string.
		 */
		public String getRole()
		{
			return this.role;
		}
		
	}
	
}
