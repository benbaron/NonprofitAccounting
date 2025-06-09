package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.service.AccountService;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.panels.AccountsPanelFX.AccountRow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;


public class AccountsPanelFXTest extends JavaFXTestBase {

    private AccountsPanelFX panel;
    private TableView<AccountRow> table;

    private MockedStatic<AccountService> mockedAccountService;
    private List<Account> mockAccountsList;

    @Mock
    private AccountService mockServiceInstance; // For constructor if it were used

    @Start
    @Override
    @SuppressWarnings("unchecked")
    public void start(Stage stage) throws Exception {
        MockitoAnnotations.openMocks(this);

        // Prepare mock data for AccountService.getAllAccounts()
        mockAccountsList = new ArrayList<>();
        Account acc1 = new Account("101", "Cash", AccountType.ASSET, new BigDecimal("1000.00"));
        acc1.setCurrency("USD");
        Account acc2 = new Account("201", "Accounts Payable", AccountType.LIABILITY, new BigDecimal("500.00"));
        acc2.setCurrency("USD");
        mockAccountsList.addAll(Arrays.asList(acc1, acc2));

        // Mock the static AccountService.getAllAccounts()
        mockedAccountService = Mockito.mockStatic(AccountService.class);
        mockedAccountService.when(AccountService::getAllAccounts).thenReturn(mockAccountsList);

        // Pass the unused mockServiceInstance to satisfy constructor, though it's not used by panel
        panel = new AccountsPanelFX(mockServiceInstance);
        Scene scene = new Scene(panel, 800, 600);
        stage.setScene(scene);
        stage.show();

        table = lookup(".table-view").queryTableView();
    }

    @AfterEach
    public void tearDownStaticMock() {
        if (mockedAccountService != null) {
            mockedAccountService.close();
        }
    }

    @Test
    public void testInitialDisplay_TablePopulatedFromService() {
        assertNotNull(table);
        // AccountService.getAllAccounts() is called in panel's constructor via refresh()
        mockedAccountService.verify(AccountService::getAllAccounts, times(1));

        verifyThat(table, hasNumRows(2));

        // Verify row content (requires AccountRow to have equals or check specific properties)
        List<AccountRow> rows = table.getItems();
        assertTrue(rows.stream().anyMatch(r -> "101".equals(r.getCode()) && "Cash".equals(r.getName())));
        assertTrue(rows.stream().anyMatch(r -> "201".equals(r.getCode()) && "Accounts Payable".equals(r.getName())));

        // Verify buttons are present
        assertNotNull(lookup("Add Account").queryButton());
        assertNotNull(lookup("Edit Account").queryButton());
        assertNotNull(lookup("Delete Account").queryButton());
    }

    @Test
    public void testAddAccountButton_AddsEmptyRowToTable() {
        int initialRowCount = table.getItems().size();

        clickOn("Add Account");
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(table, hasNumRows(initialRowCount + 1));

        AccountRow newRow = table.getItems().get(initialRowCount); // The newly added row
        assertNotNull(newRow);
        assertEquals("", newRow.getCode(), "Newly added row should have empty code by default.");
        assertEquals("", newRow.getName(), "Newly added row should have empty name by default.");
        assertEquals("USD", newRow.getCurrency(), "Newly added row should have default currency."); // As per AccountRow constructor
        assertEquals(BigDecimal.ZERO, newRow.getOpening(), "Newly added row should have zero opening balance.");
    }

    @Test
    public void testDeleteAccountButton_RemovesSelectedRow() {
        // Ensure there's a row to select and delete
        assumeTrue(table.getItems().size() > 0, "Table must have rows to test delete.");

        Platform.runLater(() -> table.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        int initialRowCount = table.getItems().size();
        AccountRow rowToDelete = table.getSelectionModel().getSelectedItem();
        assertNotNull(rowToDelete);

        clickOn("Delete Account");
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(table, hasNumRows(initialRowCount - 1));
        assertFalse(table.getItems().stream().anyMatch(r -> r.getCode().equals(rowToDelete.getCode())),
                    "Deleted row should no longer be in the table.");
    }

    @Test
    public void testDeleteAccountButton_NoSelection_ShowsAlert() {
        Platform.runLater(() -> table.getSelectionModel().clearSelection());
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Delete Account");
        WaitForAsyncUtils.waitForFxEvents();

        DialogPane alertPane = getTopModalDialogPane();
        assertNotNull(alertPane, "Alert dialog should be shown when deleting with no selection.");
  //      assertEquals(AlertType.INFORMATION, (alertPane.getScene().getWindow()).getAlertType());
        assertTrue(alertPane.getContentText().contains("Please select an account to delete."));

        // Close the alert
        Button okButton = (Button) alertPane.lookupButton(ButtonType.OK);
        clickOn(okButton);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testEditAccountButton_NoSelection_ShowsAlert() {
        Platform.runLater(() -> table.getSelectionModel().clearSelection());
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Edit Account");
        WaitForAsyncUtils.waitForFxEvents();

        DialogPane alertPane = getTopModalDialogPane();
        assertNotNull(alertPane, "Alert dialog should be shown when editing with no selection.");
//        assertEquals(AlertType.INFORMATION, (alertPane.getScene().getWindow()).getAlertType());
        assertTrue(alertPane.getContentText().contains("Please select an account to edit."));

        Button okButton = (Button) alertPane.lookupButton(ButtonType.OK);
        clickOn(okButton);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testEditAccountButton_WithSelection_AttemptsToEdit() {
        // This test can only verify that table.edit() is called.
        // Actual editing depends on TableCell implementations.
        // The current AccountsPanelFX uses PropertyValueFactory, implying non-editable cells by default
        // unless specific columns are made editable with custom cell factories.
        assumeTrue(table.getItems().size() > 0, "Table must have rows to test edit.");

        TableView<AccountRow> spiedTable = spy(table);
        // Replace the table in the panel with the spy.
        // This assumes the panel structure is a BorderPane with the TitledPane at the CENTER.
        // If the structure is different, this might need adjustment.
        // A more robust way would be if AccountsPanelFX had a getter for its table,
        // or if the table was passed in (dependency injection).
        Node originalCenter = panel.getCenter();
        if (originalCenter instanceof TitledPane) {
            ((TitledPane) originalCenter).setContent(spiedTable);
        } else {
            // Fallback or fail if structure is not as expected
            panel.setCenter(spiedTable);
        }


        Platform.runLater(() -> spiedTable.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        int selectedIndex = spiedTable.getSelectionModel().getSelectedIndex();

        clickOn("Edit Account");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that table.edit() was called on the selected row and first column
        verify(spiedTable, times(1)).edit(eq(selectedIndex), eq(spiedTable.getColumns().get(0)));
         // Restore original content if necessary to avoid impacting other tests, though each test gets a new panel.
        if (originalCenter instanceof TitledPane) {
           ((TitledPane) originalCenter).setContent(table);
        } else {
            panel.setCenter(table);
        }
    }

    private DialogPane getTopModalDialogPane() {
        // Helper to find the topmost modal dialog/alert
        // This might need adjustment based on how alerts are structured in the application theme
        Optional<Node> dialogPaneOpt = lookup( (Node n) -> n instanceof DialogPane && n.getScene() != null && n.getScene().getWindow() instanceof Stage && ((Stage)n.getScene().getWindow()).isShowing() ).tryQuery();
        return (DialogPane) dialogPaneOpt.orElse(null);
    }

}
