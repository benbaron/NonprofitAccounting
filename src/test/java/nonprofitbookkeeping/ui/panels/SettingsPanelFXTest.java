
package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.panels.SettingsPanelFX.UserRow; // Ensure UserRow is accessible

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;


public class SettingsPanelFXTest extends JavaFXTestBase
{
	
	private SettingsPanelFX panel;
	private TabPane tabPane;
	
	@Start
	@Override public
		void start(Stage stage) throws Exception
	{
		// The Stage parameter in SettingsPanelFX constructor is unused, so can pass
		// null or the current stage.
		this.panel = new SettingsPanelFX(stage);
		Scene scene = new Scene(this.panel, 800, 600);
		stage.setScene(scene);
		stage.show();
		
		this.tabPane = lookup(".tab-pane").queryAs(TabPane.class);
	}
	
	private void selectTab(String tabName)
	{
		Optional<Tab> tabOptional = this.tabPane.getTabs().stream()
			.filter(t -> tabName.equals(t.getText()))
			.findFirst();
		assertTrue(tabOptional.isPresent(), "Tab '" + tabName + "' not found.");
		Platform.runLater(() -> this.tabPane.getSelectionModel().select(tabOptional.get()));
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	@Test public void testCompanyInfoTab_InitialValuesAndInteraction()
	{
		selectTab("Company Info");
		
		// Verify initial values
		verifyThat(lookupTextFieldByLabel("Organization Name:"), hasTextInField("My Nonprofit"));
		verifyThat(lookupTextFieldByLabel("Fiscal Year Start:"), hasTextInField("2025-01-01"));
		verifyThat(lookupComboBoxByLabel("Default Currency:"), hasValue("USD"));
		
		// Interact (changes won't persist as there's no save logic in panel)
		lookupTextFieldByLabel("Organization Name:").setText("New Org Name");
		verifyThat(lookupTextFieldByLabel("Organization Name:"), hasTextInField("New Org Name"));
		
		Platform.runLater(() -> lookupComboBoxByLabel("Default Currency:").setValue("EUR"));
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat(lookupComboBoxByLabel("Default Currency:"), hasValue("EUR"));
	}
	
	/**
	 * @param string
	 * @return
	 */
        private Matcher hasValue(String string)
        {
                return new org.hamcrest.TypeSafeMatcher<ComboBox<?>>()
                {
                        @Override public void describeTo(org.hamcrest.Description description)
                        {
                                description.appendText("ComboBox value is ").appendValue(string);
                        }

                        @Override protected boolean matchesSafely(ComboBox<?> comboBox)
                        {
                                Object val = comboBox.getValue();
                                return val != null && val.toString().equals(string);
                        }
                };
        }
	
	/**
	 * @param string
	 * @return
	 */
        private Matcher hasTextInField(String string)
        {
                return new org.hamcrest.TypeSafeMatcher<TextField>()
                {
                        @Override public void describeTo(org.hamcrest.Description description)
                        {
                                description.appendText("TextField text is ").appendValue(string);
                        }

                        @Override protected boolean matchesSafely(TextField field)
                        {
                                return string.equals(field.getText());
                        }
                };
        }
	
	@Test
	public	void testUsersTab_TableDisplaysDemoData()
	{
		selectTab("Users");
		
		TableView<UserRow> usersTable = lookup(".table-view").queryTableView();
		assertNotNull(usersTable);
		verifyThat(usersTable, hasNumRows(2));
		
		// Verify content of UserRow - requires UserRow to have working equals or check
		// properties
		// For simplicity, checking if specific data exists.
		// PropertyValueFactory is used, so we can check properties of UserRow.
		List<UserRow> items = usersTable.getItems();
		assertTrue(items.stream()
			.anyMatch(r -> "admin".equals(r.getUsername()) && "Administrator".equals(r.getRole())));
		assertTrue(items.stream()
			.anyMatch(r -> "user1".equals(r.getUsername()) && "Viewer".equals(r.getRole())));
	}
	
	@Test public void testAccountingTab_InitialValuesAndInteraction()
	{
		selectTab("Accounting");
		
		verifyThat(lookupTextFieldByLabel("Default Income Account:"), hasTextInField("Donations"));
		verifyThat(lookupTextFieldByLabel("Default Expense Account:"),
			hasTextInField("Office Supplies"));
		
		CheckBox autoNumberCb = lookupCheckBoxByLabel("Auto-Number Vouchers:");
		assertTrue(autoNumberCb.isSelected());
		
		// Interact
		lookupTextFieldByLabel("Default Income Account:").setText("Grants");
		verifyThat(lookupTextFieldByLabel("Default Income Account:"), hasTextInField("Grants"));
		
		Platform.runLater(() -> autoNumberCb.setSelected(false));
		WaitForAsyncUtils.waitForFxEvents();
		assertFalse(autoNumberCb.isSelected());
	}
	
	@Test public void testBackupTab_ButtonsShowAlerts()
	{
		selectTab("Backup");
		
		clickOn("Create Backup");
		WaitForAsyncUtils.waitForFxEvents();
		DialogPane backupAlert = getTopModalDialogPane();
		assertNotNull(backupAlert, "Create Backup alert not shown.");
		assertTrue(backupAlert.getContentText().contains("Backup process would run here."));
		clickOn((Button) backupAlert.lookupButton(ButtonType.OK));
		WaitForAsyncUtils.waitForFxEvents();
		
		clickOn("Restore Backup");
		WaitForAsyncUtils.waitForFxEvents();
		DialogPane restoreAlert = getTopModalDialogPane();
		assertNotNull(restoreAlert, "Restore Backup alert not shown.");
		assertTrue(restoreAlert.getContentText().contains("Restore process would run here."));
		clickOn((Button) restoreAlert.lookupButton(ButtonType.OK));
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	@Test public void testUiPreferencesTab_InitialValuesAndInteraction()
	{
		selectTab("UI Preferences");
		
		verifyThat(lookupComboBoxByLabel("Theme:"), hasValue("System"));
		verifyThat(lookupComboBoxByLabel("Language:"), hasValue("English"));
		
		Platform.runLater(() -> lookupComboBoxByLabel("Theme:").setValue("Dark"));
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat(lookupComboBoxByLabel("Theme:"), hasValue("Dark"));
		
		Platform.runLater(() -> lookupComboBoxByLabel("Language:").setValue("Spanish"));
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat(lookupComboBoxByLabel("Language:"), hasValue("Spanish"));
	}
	
	// --- Helper methods for locating controls within Grids ---
	private TextField lookupTextFieldByLabel(String labelText)
	{
		Labeled label = lookup(labelText).queryLabeled();
		Node parent = label.getParent();
		
		if (parent instanceof GridPane)
		{
			GridPane grid = (GridPane) parent;
			Integer labelRowIndex = GridPane.getRowIndex(label);
			Integer labelColIndex = GridPane.getColumnIndex(label);
			// Assume TextField is in the next column of the same row
			Optional<Node> textFieldOpt = grid.getChildren().stream()
				.filter(node -> node instanceof TextField &&
					GridPane.getRowIndex(node) != null &&
					GridPane.getRowIndex(node).equals(labelRowIndex) &&
					GridPane.getColumnIndex(node) != null &&
					GridPane.getColumnIndex(node).equals(labelColIndex + 1))
				.findFirst();
			assertTrue(textFieldOpt.isPresent(),
				"TextField next to label '" + labelText + "' not found.");
			return (TextField) textFieldOpt.get();
		}
		
		fail("Label '" + labelText + "' is not in a GridPane or TextField not found next to it.");
		return null;
	}
	
	@SuppressWarnings("unchecked") private ComboBox<String> lookupComboBoxByLabel(String labelText)
	{
		Labeled label = lookup(labelText).queryLabeled();
		Node parent = label.getParent();
		
		if (parent instanceof GridPane)
		{
			GridPane grid = (GridPane) parent;
			Integer labelRowIndex = GridPane.getRowIndex(label);
			Integer labelColIndex = GridPane.getColumnIndex(label);
			Optional<Node> comboBoxOpt = grid.getChildren().stream()
				.filter(node -> node instanceof ComboBox &&
					GridPane.getRowIndex(node) != null &&
					GridPane.getRowIndex(node).equals(labelRowIndex) &&
					GridPane.getColumnIndex(node) != null &&
					GridPane.getColumnIndex(node).equals(labelColIndex + 1))
				.findFirst();
			assertTrue(comboBoxOpt.isPresent(),
				"ComboBox next to label '" + labelText + "' not found.");
			return (ComboBox<String>) comboBoxOpt.get();
		}
		
		fail("Label '" + labelText + "' is not in a GridPane or ComboBox not found next to it.");
		return null;
	}
	
	private CheckBox lookupCheckBoxByLabel(String labelText)
	{
		Labeled label = lookup(labelText).queryLabeled();
		Node parent = label.getParent();
		
		if (parent instanceof GridPane)
		{
			GridPane grid = (GridPane) parent;
			Integer labelRowIndex = GridPane.getRowIndex(label);
			Integer labelColIndex = GridPane.getColumnIndex(label);
			Optional<Node> checkBoxOpt = grid.getChildren().stream()
				.filter(node -> node instanceof CheckBox &&
					GridPane.getRowIndex(node) != null &&
					GridPane.getRowIndex(node).equals(labelRowIndex) &&
					GridPane.getColumnIndex(node) != null &&
					GridPane.getColumnIndex(node).equals(labelColIndex + 1))
				.findFirst();
			assertTrue(checkBoxOpt.isPresent(),
				"CheckBox next to label '" + labelText + "' not found.");
			return (CheckBox) checkBoxOpt.get();
		}
		
		fail("Label '" + labelText + "' is not in a GridPane or CheckBox not found next to it.");
		return null;
	}
	
	private DialogPane getTopModalDialogPane()
	{
		Optional<Node> dialogPaneOpt = lookup((Node n) -> n instanceof DialogPane &&
			n.getScene() != null && n.getScene().getWindow() instanceof Stage &&
			((Stage) n.getScene().getWindow()).isShowing()).tryQuery();
		return (DialogPane) dialogPaneOpt.orElse(null);
	}
	
}
