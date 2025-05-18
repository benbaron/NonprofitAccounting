/**
 * nonprofit-scaledger-ribbon.zip_expanded SettingsPanelFX.java SettingsPanelFX
 */

package nonprofitbookkeeping.ui.panels;


import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * JavaFX port of the original Swing {@code SettingsPanel}.
 */
public class SettingsPanelFX extends BorderPane
{
	/**
	 * 
	 * Constructor SettingsPanelFX
	 * @param primaryStage 
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
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "deprecation" }) private static Tab usersTab()
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
	 * 
	 * @return
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
	 * 
	 * @return
	 */
	private static Tab backupTab()
	{
		HBox box = new HBox(10);
		Button backupBtn = new Button("Create Backup");
		Button restoreBtn = new Button("Restore Backup");
		backupBtn.setOnAction(e -> alert("Backup process would run here."));
		restoreBtn.setOnAction(e -> alert("Restore process would run here."));
		box.getChildren().addAll(backupBtn, restoreBtn);
		box.setPadding(new Insets(10));
		
		TitledPane wrapper = titled("Backup & Restore", box);
		return new Tab("Backup", wrapper);
	}
	
	/**
	 * 
	 * @return
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
	 * 
	 * @param title
	 * @param content
	 * @return
	 */
	private static TitledPane titled(String title, javafx.scene.Node content)
	{
		TitledPane tp = new TitledPane(title, content);
		tp.setCollapsible(false);
		return tp;
	}
	
	/**
	 * 
	 * @param msg
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
	/* ───────────────────────── Table model ───────────────────────── */
	public static class UserRow
	{
		private final String username;
		private final String role;
		
		public UserRow(String u, String r)
		{
			this.username = u;
			this.role = r;
		}
		
		public String getUsername()
		{
			return this.username;
		}
		
		public String getRole()
		{
			return this.role;
		}
		
	}
	
}
