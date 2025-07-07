
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import java.io.IOException;

/**
 * JavaFX port of the original Swing {@code SettingsPanel}.
 */
public class SettingsPanelFX extends BorderPane
{
	/**
	 * Constructs a new {@code SettingsPanelFX}.
	 * Initializes the panel with a {@link TabPane} containing various settings categories:
	 * Company Info, Users, Accounting, Backup, and UI Preferences.
	 * 
	 * @param primaryStage The primary stage of the application. This parameter is currently not
	 *                     used within this constructor or its helper methods.
	 */
	public SettingsPanelFX(Stage primaryStage)
	{
		setPadding(new Insets(10));
		TabPane tabs = new TabPane();
		
		tabs.getTabs().addAll(
			companyInfoTab(),
			usersTab(),
			accountingTab(),
			backupTab(),
			uiPrefsTab());
		
		setCenter(tabs);
	}
	
	/* ───────────────────────── Tab builders ───────────────────────── */
	
	/**
	 * Builds and returns the "Company Info" tab for the settings panel.
	 * This tab contains fields for editing basic organization information such as
	 * name, fiscal year start, and default currency.
	 *
	 * @return A {@link Tab} configured with company information settings.
	 */
	private static Tab companyInfoTab()
	{
		GridPane grid = grid(3, 2);
		grid.add(new Label("Organization Name:"), 0, 0);
		grid.add(new TextField("My Nonprofit"), 1, 0);
		grid.add(new Label("Fiscal Year Start:"), 0, 1);
		grid.add(new TextField("2025-01-01"), 1, 1);
		grid.add(new Label("Default Currency:"), 0, 2);
		grid.add(new ComboBox<String>()
		{
			{
				getItems().addAll("USD", "EUR", "GBP");
				setValue("USD");
			}
			
		}, 1, 2);
		
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
	{ "unchecked", "deprecation" }) private static Tab usersTab()
	{
		TableView<UserRow> table = new TableView<>();
		TableColumn<UserRow, String> userCol = new TableColumn<>("Username");
		userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
		TableColumn<UserRow, String> roleCol = new TableColumn<>("Role");
		roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
		table.getColumns().addAll(userCol, roleCol);
		table.getItems().addAll(
			new UserRow("admin", "Administrator"),
			new UserRow("user1", "Viewer"));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		TitledPane wrapper = titled("User Management", table);
		return new Tab("Users", wrapper);
	}
	
	/**
	 * Builds and returns the "Accounting" tab for the settings panel.
	 * This tab includes settings related to default accounts (income, expense)
	 * and options like auto-numbering for vouchers.
	 * 
	 * @return A {@link Tab} configured with accounting-related settings.
	 */
	private static Tab accountingTab()
	{
		GridPane grid = grid(3, 2);
		grid.add(new Label("Default Income Account:"), 0, 0);
		grid.add(new TextField("Donations"), 1, 0);
		grid.add(new Label("Default Expense Account:"), 0, 1);
		grid.add(new TextField("Office Supplies"), 1, 1);
		grid.add(new Label("Auto-Number Vouchers:"), 0, 2);
		grid.add(new CheckBox("Enabled")
		{
			{
				setSelected(true);
			}
			
		}, 1, 2);
		
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
    private static Tab backupTab()
    {
            HBox box = new HBox(10);
            Button backupBtn = new Button("Create Backup");
            Button restoreBtn = new Button("Restore Backup");

            backupBtn.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Save Backup");
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("NPBK files", "*.npbk"));
                    File out = fc.showSaveDialog(null);
                    if (out != null)
                    {
                            try
                            {
                                    CurrentCompany.setCurrentFile(out);
                                    CurrentCompany.persist();
                                    alert("Backup saved to " + out.getAbsolutePath());
                            }
                            catch (IOException | ActionCancelledException | NoFileCreatedException ex)
                            {
                                    alert("Backup failed: " + ex.getMessage());
                            }
                    }
            });

            restoreBtn.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Open Backup");
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("NPBK files", "*.npbk"));
                    File f = fc.showOpenDialog(null);
                    if (f != null)
                    {
                            try
                            {
                                    CurrentCompany.loadFromPersistent(f);
                                    CurrentCompany.markCompanyOpen();
                                    alert("Backup restored from " + f.getName());
                            }
                            catch (IOException | ActionCancelledException | NoFileCreatedException ex)
                            {
                                    alert("Restore failed: " + ex.getMessage());
                            }
                    }
            });

            box.getChildren().addAll(backupBtn, restoreBtn);
            box.setPadding(new Insets(10));

            TitledPane wrapper = titled("Backup & Restore", box);
            return new Tab("Backup", wrapper);
    }
	
	/**
	 * Builds and returns the "UI Preferences" tab for the settings panel.
	 * This tab includes options for selecting UI theme and language.
	 * 
	 * @return A {@link Tab} configured with UI preference settings.
	 */
	private static Tab uiPrefsTab()
	{
		GridPane grid = grid(2, 2);
		grid.add(new Label("Theme:"), 0, 0);
		grid.add(new ComboBox<String>()
		{
			{
				getItems().addAll("Light", "Dark", "System");
				setValue("System");
			}
			
		}, 1, 0);
		grid.add(new Label("Language:"), 0, 1);
		grid.add(new ComboBox<String>()
		{
			{
				getItems().addAll("English", "Spanish", "French");
				setValue("English");
			}
			
		}, 1, 1);
		
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
