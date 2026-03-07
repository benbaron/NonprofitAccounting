
package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;
import static org.testfx.matcher.control.TableViewMatchers.containsRow;

public class JournalPanelFXTest extends JavaFXTestBase
{
	
	private JournalPanelFX panel;
	private Company testCompany;
	
	@Mock private Ledger mockLedger;
	@Mock private Journal mockJournal;
	
	private ObservableList<AccountingTransaction> journalTransactions;
	
	@Start
	@Override public
		void start(Stage stage) throws Exception
	{
		MockitoAnnotations.openMocks(this);
		
		this.testCompany = new Company();
		CompanyProfileModel profile = new CompanyProfileModel();
		profile.setCompanyName("Test Journal Co");
		this.testCompany.setCompanyProfile(profile);
		this.testCompany.setLedger(this.mockLedger);
		
		when(this.mockLedger.getJournal()).thenReturn(this.mockJournal);
		
		this.journalTransactions = FXCollections.observableArrayList();
		AccountingTransaction tx1 = new AccountingTransaction();
		tx1.setId(1);
		tx1.setDate("2024-01-15");
		tx1.setMemo("Donation A");
		tx1.setBookingDateTimestamp(Timestamp.from(Instant.now().minusSeconds(3600)));
		
		
		AccountingTransaction tx2 = new AccountingTransaction();
		tx2.setId(2);
		tx2.setDate("2024-01-20");
		tx2.setMemo("Supplies B");
		tx2.setBookingDateTimestamp(Timestamp.from(Instant.now()));
		
		
		this.journalTransactions.addAll(tx1, tx2);
		
		when(this.mockJournal.getJournalTransactions()).thenReturn(this.journalTransactions);
		// Mock add, update, delete operations
		doAnswer(invocation -> {
			AccountingTransaction tx = invocation.getArgument(0);
			this.journalTransactions.add(tx);
			return null;
		}).when(this.mockJournal).addTransaction(any(AccountingTransaction.class));
		
		doAnswer(invocation -> {
			AccountingTransaction txToUpdate = invocation.getArgument(0);
			Optional<AccountingTransaction> existingOpt = this.journalTransactions.stream()
				.filter(t -> t.getId() == txToUpdate.getId() ||
					t.getBookingDateTimestamp().equals(txToUpdate.getBookingDateTimestamp()))
				.findFirst();
			
			if (existingOpt.isPresent())
			{
				int idx = this.journalTransactions.indexOf(existingOpt.get());
				this.journalTransactions.set(idx, txToUpdate);
			}
			else
			{
				this.journalTransactions.add(txToUpdate); // Or throw error if update target not
															// found
			}
			
			return null;
		}).when(this.mockJournal).updateTransaction(any(AccountingTransaction.class));
		
		doAnswer(invocation -> {
			Timestamp ts = invocation.getArgument(0);
			this.journalTransactions.removeIf(tx -> tx.getBookingDateTimestamp().equals(ts));
			return null;
		}).when(this.mockJournal).deleteTransaction(any(Timestamp.class));
		
		
		// Set the company AFTER mocks are configured
		CurrentCompany.forceCompanyLoad(this.testCompany);
		
		this.panel = new JournalPanelFX(); // Panel will call refresh and use CurrentCompany
		Scene scene = new Scene(this.panel, 800, 600);
		stage.setScene(scene);
		stage.show();
	}
	
	@AfterEach public void tearDown()
	{
		CurrentCompany.close();
	}
	
	
	@Test public void testInitialDisplay_ShowsTransactions()
	{
		TableView<AccountingTransaction> table = lookup(".table-view").queryTableView();
		assertNotNull(table);
		verifyThat(table, hasNumRows(2));
		// Check for one of the rows. PropertyValueFactory is used, so direct object
		// comparison works.
		verifyThat(table, containsRow(this.journalTransactions.get(0)));
		verifyThat(table, containsRow(this.journalTransactions.get(1)));
	}
	
	@Test public void testNewButton_OpensDialog_AndAddsTransactionOnSimulatedSave()
	{
		clickOn("New");
		WaitForAsyncUtils.waitForFxEvents();
		
                DialogPane dialogPane = getTopModalDialogPane();
                assertNotNull(dialogPane, "GeneralJournalEntryPanelFX dialog not found");
		// assertEquals("New Transaction",
		// dialogPane.getScene().getWindow().getTitle());
		
                // Simulate the JournalEntryWorkspaceFX saving a new transaction
                // This is a simplification. A real test might need to interact with
                // the workspace fields and then trigger its internal save mechanism
                // that calls the consumer. For this test, we'll assume the consumer
                // provided by JournalPanelFX to the workspace
		// is called with a new transaction.
		
		AccountingTransaction newTx = new AccountingTransaction();
		newTx.setId(3); // New ID
		newTx.setDate("2024-01-25");
		newTx.setMemo("New Donation C");
		newTx.setBookingDateTimestamp(Timestamp.from(Instant.now().plusSeconds(3600)));
		
		
                // The JournalPanelFX passes a Consumer to JournalEntryWorkspaceFX.
                // We can't easily "click save" inside the workspace from *this*
		// test.
		// So, we'll verify the mockJournal's addTransaction was called by virtue of the
		// dialog setup.
		// To simulate the dialog closing and refresh happening, we'll manually close
		// it.
		// And then check if addTransaction on mockJournal was called.
		
		// This part is tricky without deeper interaction or a way to get the consumer.
		// Let's assume the dialog is closed and the panel's refresh is triggered.
                // The actual add happens in the consumer passed to the workspace.
		// We'll verify by checking the table size after closing.
		
		// This is where direct testing of the consumer would be better.
		// For now, we assume that if the dialog was correctly configured, a save would
		// work.
		// We will manually add to the list to simulate the callback being successful
		// and then close the dialog, then verify table.
		Platform.runLater(() -> {
                        // This simulates the callback that JournalPanelFX provides to
                        // the workspace
			this.mockJournal.addTransaction(newTx);
			this.panel.refresh(); // Manually call refresh as the dialog's internal save would
		});
		WaitForAsyncUtils.waitForFxEvents();
		
		
		// Close the dialog
		Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
		clickOn(closeButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		verifyThat(".table-view", hasNumRows(3));
		verifyThat(".table-view", containsRow(newTx));
	}
	
	@Test public void testEditButton_OpensDialog_AndUpdatesTransactionOnSimulatedSave()
	{
		TableView<AccountingTransaction> table = lookup(".table-view").queryTableView();
		Platform.runLater(() -> table.getSelectionModel().select(0)); // Select first transaction
																		// (tx1)
		WaitForAsyncUtils.waitForFxEvents();
		
		AccountingTransaction selectedTx = table.getSelectionModel().getSelectedItem();
		assertNotNull(selectedTx);
		assertEquals("Donation A", selectedTx.getMemo());
		
		clickOn("Edit");
		WaitForAsyncUtils.waitForFxEvents();
		
                DialogPane dialogPane = getTopModalDialogPane();
                assertNotNull(dialogPane, "GeneralJournalEntryPanelFX dialog for edit not found");
		// assertEquals("Edit Transaction",
		// (dialogPane.getScene().getWindow()).getTitle());
		
		// Simulate NewTransactionPanelFX saving changes
		AccountingTransaction editedTx = new AccountingTransaction();
		editedTx.setId((int) selectedTx.getId()); // Keep ID
		editedTx.setBookingDateTimestamp(selectedTx.getBookingDateTimestamp()); // Keep original
																				// timestamp for
																				// update
		editedTx.setDate(selectedTx.getDate());
		editedTx.setMemo("Updated Donation A Memo"); // Changed memo
		
		Platform.runLater(() -> {
			// Simulate the callback for saving an edit
			this.mockJournal.updateTransaction(editedTx);
			this.panel.refresh();
		});
		WaitForAsyncUtils.waitForFxEvents();
		
		Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
		clickOn(closeButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		verifyThat(".table-view", hasNumRows(2)); // Still 2 rows
		ArgumentCaptor<AccountingTransaction> captor =
			ArgumentCaptor.forClass(AccountingTransaction.class);
		// We verify against the mockJournal because the table items are new instances
		// after refresh
		verify(this.mockJournal, atLeastOnce()).updateTransaction(captor.capture());
		
		// Check the captured transaction that was passed to updateTransaction
		Optional<AccountingTransaction> updatedInMock = captor.getAllValues().stream()
			.filter(tx -> tx.getId() == editedTx.getId())
			.findFirst();
		assertTrue(updatedInMock.isPresent());
		assertEquals("Updated Donation A Memo", updatedInMock.get().getMemo());
		
		// Also check table content for the updated memo
		assertTrue(table.getItems().stream().anyMatch(tx -> tx.getId() == editedTx.getId() &&
			"Updated Donation A Memo".equals(tx.getMemo())));
	}
	
        @Test public void testDeleteButton_RemovesTransaction()
        {
                TableView<AccountingTransaction> table = lookup(".table-view").queryTableView();
                Platform.runLater(() -> table.getSelectionModel().selectIndices(0, 1));
                WaitForAsyncUtils.waitForFxEvents();

                AccountingTransaction first = table.getItems().get(0);
                AccountingTransaction second = table.getItems().get(1);

                clickOn("Delete");
                WaitForAsyncUtils.waitForFxEvents();

                verify(this.mockJournal, times(1))
                        .deleteTransaction(eq(first.getBookingDateTimestamp()));
                verify(this.mockJournal, times(1))
                        .deleteTransaction(eq(second.getBookingDateTimestamp()));
                verifyThat(table, hasNumRows(0));
        }
	
	private DialogPane getTopModalDialogPane()
	{
		List<Stage> stages = listTargetWindows().stream()
			.filter(w -> w instanceof Stage)
			.map(w -> (Stage) w)
			.filter(s -> s.getModality() == javafx.stage.Modality.APPLICATION_MODAL ||
				s.getModality() == javafx.stage.Modality.WINDOW_MODAL)
			.filter(Stage::isShowing)
			.collect(Collectors.toList());
		if (stages.isEmpty())
			return null;
		Stage currentDialogStage = stages.get(stages.size() - 1);
		if (currentDialogStage.getScene() == null ||
			currentDialogStage.getScene().getRoot() == null)
			return null;
			
		if (currentDialogStage.getScene().getRoot() instanceof DialogPane)
		{
			return (DialogPane) currentDialogStage.getScene().getRoot();
		}
		
		// Fallback for cases where DialogPane might be nested, e.g. in a custom dialog
		// wrapper
		return from(currentDialogStage.getScene().getRoot()).lookup(".dialog-pane").query();
	}
	
}
