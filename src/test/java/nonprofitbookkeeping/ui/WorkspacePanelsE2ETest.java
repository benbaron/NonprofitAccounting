package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import nonprofitbookkeeping.ui.adapters.OrgAppPanelNodeAdapter;
import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.records.BankingItemRecord;
import nonprofitbookkeeping.model.supplemental.ReceivablesLine;
import nonprofitbookkeeping.persistence.CompanyDataRepository;
import nonprofitbookkeeping.service.BankingItemRecordService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspacePanelsE2ETest
{
	@TempDir Path tempDir;

	static
	{
		System.setProperty("javafx.platform", "Monocle");
		System.setProperty("glass.platform", "Monocle");
		System.setProperty("monocle.platform", "Headless");
		System.setProperty("prism.order", "sw");
		System.setProperty("prism.text", "t2k");
		System.setProperty("prism.es2", "false");
		System.setProperty("java.awt.headless", "true");
	}

	@BeforeAll
	static void initFxToolkit() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(1);
		try
		{
			Platform.startup(latch::countDown);
		}
		catch (IllegalStateException alreadyStarted)
		{
			latch.countDown();
		}
		assertTrue(latch.await(5, TimeUnit.SECONDS),
			"JavaFX platform failed to start");
	}

	@BeforeEach
	void resetDateRangeContext() throws Exception
	{
		TestDatabase.reset(this.tempDir);
		runOnFxThread(() -> {
			DateRangeContext.set(DateRange.ALL);
			LedgerSelectionContext.setSelectedTransaction(null);
			return null;
		});
	}

	@Test
	void workspaceSubTabsArePresent() throws Exception
	{
		assertEquals(2,
			runOnFxThread(() -> getTabPane(new BudgetPanel()).getTabs().size()));
		assertEquals(2,
			runOnFxThread(() -> getTabPane(new LedgerPanel()).getTabs().size()));
		assertEquals(2,
			runOnFxThread(() -> getTabPane(new AssetsPanel()).getTabs().size()));
	}

	@Test
	void mainViewCanSelectNewWorkspaceTabs() throws Exception
	{
		MainApplicationView view = runOnFxThread(MainApplicationView::new);

		String budgetTab = runOnFxThread(() -> {
			view.showPanel(MainApplicationView.PanelType.BUDGET);
			return selectedTabText(view);
		});
		assertEquals("Budget", budgetTab);

		String ledgerTab = runOnFxThread(() -> {
			view.showPanel(MainApplicationView.PanelType.LEDGER);
			return selectedTabText(view);
		});
		assertEquals("Ledger", ledgerTab);

		String assetsTab = runOnFxThread(() -> {
			view.showPanel(MainApplicationView.PanelType.ASSETS);
			return selectedTabText(view);
		});
		assertEquals("Assets", assetsTab);
	}

	@Test
	void transactionEditorCanInitializeWhenNoCompanyIsActive() throws Exception
	{
		runOnFxThread(() -> {
			CurrentCompany.forceCompanyLoad(null);
			TransactionEditorPanel panel = new TransactionEditorPanel();
			assertNotNull(panel.root());
			return null;
		});
	}

	@Test
	void endToEndDateRangeWriteReadFiltersLedgerRows() throws Exception
	{
		seedLedgerTransactions();
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);

		int initialCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(4, initialCount);

		runOnFxThread(() -> {
			DateRangeContext.set(new DateRange(java.time.LocalDate.of(2026, 1, 1),
				java.time.LocalDate.of(2026, 1, 10)));
			return null;
		});

		int filteredCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(2, filteredCount,
			"Write date range to context and read filtered ledger rows (E2E)");

		runOnFxThread(() -> {
			DateRangeContext.set(DateRange.ALL);
			return null;
		});

		int resetCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(4, resetCount);
	}

	@Test
	void ledgerStatusShowsTransactionAndRowCounts() throws Exception
	{
		seedLedgerTransactions();
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);
		String text = runOnFxThread(() -> {
			Field statusField = LedgerRegisterPanel.class.getDeclaredField("status");
			statusField.setAccessible(true);
			return ((Label) statusField.get(ledgerPanel)).getText();
		});
		assertTrue(text.contains("2 transaction(s)"));
		assertTrue(text.contains("4 row(s)"));
	}

	@Test
	void ledgerSubrecordsKeepDetailInBackingRows() throws Exception
	{
		seedLedgerTransactionsWithSupplementalDetails();
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);
		String summary = runOnFxThread(() -> {
			Object firstRow = ledgerTable(ledgerPanel).getItems().get(0);
			java.lang.reflect.Method method = firstRow.getClass().getDeclaredMethod("subrecordSummary");
			method.setAccessible(true);
			return (String) method.invoke(firstRow);
		});

		assertTrue(summary.contains("RECEIVABLE#INV-2026-001"), summary);
		assertTrue(summary.contains("Donor pledge"), summary);
		assertTrue(summary.contains("125.5"), summary);
	}

	@Test
	void endToEndDateRangeOneSidedBoundsFilterLedgerRows() throws Exception
	{
		seedLedgerTransactions();
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);

		runOnFxThread(() -> {
			DateRangeContext.set(new DateRange(java.time.LocalDate.of(2026, 1, 10), null));
			return null;
		});
		int startOnlyCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(2, startOnlyCount,
			"Start-only range should include rows on/after the start date");

		runOnFxThread(() -> {
			DateRangeContext.set(new DateRange(null, java.time.LocalDate.of(2026, 1, 10)));
			return null;
		});
		int endOnlyCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(2, endOnlyCount,
			"End-only range should include rows on/before the end date");
	}

	@Test
	void transactionEditorLoadsLiveTransactionFromRegisterSelection() throws Exception
	{
		seedLedgerTransactions();
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);
		TransactionEditorPanel editorPanel = runOnFxThread(TransactionEditorPanel::new);

		runOnFxThread(() -> {
			TableView<?> table = ledgerTable(ledgerPanel);
			table.getSelectionModel().select(0);
			invokeOpenSelected(ledgerPanel);
			return null;
		});

		TableView<?> editorTable = runOnFxThread(() -> {
			Node root = editorPanel.root();
			return (TableView<?>) ((javafx.scene.layout.VBox) ((BorderPane) root).getCenter())
				.getChildren().get(2);
		});
		assertEquals(2, runOnFxThread(() -> editorTable.getItems().size()));
	}

	@Test
	void ledgerOpenSelectionNavigatesToEditorSubtab() throws Exception
	{
		seedLedgerTransactions();
		LedgerPanel panel = runOnFxThread(LedgerPanel::new);
		TabPane tabs = runOnFxThread(() -> (TabPane) panel.getCenter());
		LedgerRegisterPanel registerPanel = runOnFxThread(() ->
			(LedgerRegisterPanel) ((OrgAppPanelNodeAdapter) tabs.getTabs().get(0).getContent()).getAppPanel());

		runOnFxThread(() -> {
			TableView<?> table = ledgerTable(registerPanel);
			table.getSelectionModel().select(0);
			invokeOpenSelected(registerPanel);
			return null;
		});

		assertEquals("Transaction Editor", runOnFxThread(() -> tabs.getSelectionModel().getSelectedItem().getText()));
	}

	@Test
	void transactionEditorSavePersistsHeaderChanges() throws Exception
	{
		seedLedgerTransactions();
		TransactionEditorPanel editorPanel = runOnFxThread(TransactionEditorPanel::new);
		AccountingTransaction transaction = new CompanyDataRepository().load().getLedger().getTransactions().get(0);

		runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(transaction);
			return null;
		});

		runOnFxThread(() -> {
			Field memoField = TransactionEditorPanel.class.getDeclaredField("memo");
			memoField.setAccessible(true);
			TextField memo = (TextField) memoField.get(editorPanel);
			memo.setText("Updated memo");
			editorPanel.onSave();
			return null;
		});

		AccountingTransaction persisted = new CompanyDataRepository().load().getLedger().getTransactions().stream()
			.filter(tx -> tx.getId() == transaction.getId())
			.findFirst()
			.orElseThrow();
		assertEquals("Updated memo", persisted.getMemo());
	}

	@Test
	void transactionEditorHasExpectedSplitColumnsAndRows() throws Exception
	{
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		TableView<?> table = runOnFxThread(() -> {
			Node root = panel.root();
			assertNotNull(root);
			return (TableView<?>) ((javafx.scene.layout.VBox) ((BorderPane) root).getCenter())
				.getChildren().get(2);
		});

		assertEquals(7, runOnFxThread(() -> table.getColumns().size()));
		assertEquals(0, runOnFxThread(() -> table.getItems().size()));
	}

	@Test
	void transactionEditorRefreshStatusUsesIdLabelWhenTimestampFallbackMatches() throws Exception
	{
		seedLedgerTransactions();
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		AccountingTransaction persisted = new CompanyDataRepository().load().getLedger()
			.getTransactions().get(0);
		AccountingTransaction selected = new AccountingTransaction();
		selected.setId(0);
		selected.setBookingDateTimestamp(persisted.getBookingDateTimestamp());
		selected.setDate("2026-01-05");

		String statusText = runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(selected);
			invokeRefreshFromSelection(panel);
			Field statusField = TransactionEditorPanel.class.getDeclaredField("status");
			statusField.setAccessible(true);
			return ((Label) statusField.get(panel)).getText();
		});

		assertTrue(statusText.contains("Refreshed transaction #"),
			"Expected ID label after timestamp fallback match, got: " + statusText);
	}

	@Test
	void transactionEditorRefreshStatusFallsBackToDateLabelWhenUnidentified() throws Exception
	{
		seedLedgerTransactions();
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		AccountingTransaction selected = new AccountingTransaction();
		selected.setId(0);
		selected.setBookingDateTimestamp(999999999999L);
		selected.setDate("2026-03-07");

		String statusText = runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(selected);
			invokeRefreshFromSelection(panel);
			Field statusField = TransactionEditorPanel.class.getDeclaredField("status");
			statusField.setAccessible(true);
			return ((Label) statusField.get(panel)).getText();
		});

		assertTrue(statusText.contains("dated 2026-03-07"),
			"Expected date label for unmatched ID-less transaction, got: " + statusText);
	}

	@Test
	void transactionEditorPersistsStableSplitIdsAndReloadsAssociatedMetadata() throws Exception
	{
		seedLedgerTransactions();
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		AccountingTransaction transaction = new CompanyDataRepository().load().getLedger()
			.getTransactions().get(0);

		String firstSplitId = runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(transaction);
			TableView<?> table = splitTable(panel);
			Object first = table.getItems().get(0);
			first.getClass().getDeclaredMethod("setMerchant", String.class)
				.invoke(first, "Merchant A");
			String splitId = (String) first.getClass().getDeclaredMethod("getSplitId")
				.invoke(first);
			panel.onSave();
			return splitId;
		});

		AccountingTransaction persisted = new CompanyDataRepository().load().getLedger()
			.getTransactions().stream()
			.filter(tx -> tx.getId() == transaction.getId())
			.findFirst()
			.orElseThrow();
		String encodedSplitIds = persisted.getInfo().get("ledger.split.ids");
		assertNotNull(encodedSplitIds);
		assertTrue(encodedSplitIds.contains(firstSplitId),
			"Persisted split IDs should contain the edited split id");

		TransactionEditorPanel reloadedPanel = runOnFxThread(TransactionEditorPanel::new);
		runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(persisted);
			invokeRefreshFromSelection(reloadedPanel);
			Object first = splitTable(reloadedPanel).getItems().get(0);
			String reloadedSplitId = (String) first.getClass().getDeclaredMethod("getSplitId")
				.invoke(first);
			String merchant = (String) first.getClass().getDeclaredMethod("getMerchant")
				.invoke(first);
			assertEquals(firstSplitId, reloadedSplitId);
			assertEquals("Merchant A", merchant);
			return null;
		});
	}

	@Test
	void transactionEditorSplitIdSetUpdatesWhenSplitRemovedAndAdded() throws Exception
	{
		seedLedgerTransactions();
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		AccountingTransaction transaction = new CompanyDataRepository().load().getLedger()
			.getTransactions().get(0);

		String[] ids = runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(transaction);
			TableView<?> table = splitTable(panel);
			Object first = table.getItems().get(0);
			Object second = table.getItems().get(1);
			String firstId = (String) first.getClass().getDeclaredMethod("getSplitId").invoke(first);
			String removedId = (String) second.getClass().getDeclaredMethod("getSplitId").invoke(second);
			((TableView<Object>) (TableView<?>) table).getItems().remove(1);

			Object added = first.getClass().getDeclaredConstructor().newInstance();
			String account = (String) first.getClass().getDeclaredMethod("getAccount").invoke(first);
			String fund = (String) first.getClass().getDeclaredMethod("getFund").invoke(first);
			added.getClass().getDeclaredMethod("setAccount", String.class).invoke(added, account);
			added.getClass().getDeclaredMethod("setFund", String.class).invoke(added, fund);
			added.getClass().getDeclaredMethod("setAmount", String.class).invoke(added, "10.00");
			added.getClass().getDeclaredMethod("setSide", String.class).invoke(added, "CREDIT");
			String addedId = (String) added.getClass().getDeclaredMethod("getSplitId").invoke(added);
			((TableView<Object>) (TableView<?>) table).getItems().add(added);
			panel.onSave();
			return new String[]{firstId, removedId, addedId};
		});

		AccountingTransaction persisted = new CompanyDataRepository().load().getLedger()
			.getTransactions().stream()
			.filter(tx -> tx.getId() == transaction.getId())
			.findFirst()
			.orElseThrow();
		String encoded = persisted.getInfo().get("ledger.split.ids");
		assertNotNull(encoded);
		assertTrue(encoded.contains(ids[0]), "Original retained split id should remain.");
		assertTrue(encoded.contains(ids[2]), "Newly added split id should be persisted.");
		assertTrue(!encoded.contains(ids[1]), "Removed split id should not remain persisted.");

		BankingItemRecordService service = new BankingItemRecordService();
		List<BankingItemRecord> current = service.listAll().stream()
			.filter(r -> String.valueOf(transaction.getId()).equals(r.transactionId()))
			.filter(r -> "LEDGER_EDITOR".equals(r.source()))
			.toList();
		assertEquals(2, current.size(), "Expected one banking-item record per remaining split.");
		assertTrue(current.stream().noneMatch(r -> r.bankingItemId().contains(ids[1])),
			"Removed split should not retain orphaned BankingItemRecord.");
		assertTrue(current.stream().anyMatch(r -> r.bankingItemId().contains(ids[2])),
			"Added split should have a BankingItemRecord.");
	}

	@Test
	void transactionEditorRepeatedSplitRewritesDoNotAccumulateOrphans() throws Exception
	{
		seedLedgerTransactions();
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		AccountingTransaction transaction = new CompanyDataRepository().load().getLedger()
			.getTransactions().get(0);

		runOnFxThread(() -> {
			LedgerSelectionContext.setSelectedTransaction(transaction);
			TableView<?> table = splitTable(panel);
			Object first = table.getItems().get(0);
			String account = (String) first.getClass().getDeclaredMethod("getAccount").invoke(first);
			String fund = (String) first.getClass().getDeclaredMethod("getFund").invoke(first);

			((TableView<Object>) (TableView<?>) table).getItems().remove(1);
			Object added1 = first.getClass().getDeclaredConstructor().newInstance();
			added1.getClass().getDeclaredMethod("setAccount", String.class).invoke(added1, account);
			added1.getClass().getDeclaredMethod("setFund", String.class).invoke(added1, fund);
			added1.getClass().getDeclaredMethod("setAmount", String.class).invoke(added1, "10.00");
			added1.getClass().getDeclaredMethod("setSide", String.class).invoke(added1, "CREDIT");
			((TableView<Object>) (TableView<?>) table).getItems().add(added1);
			panel.onSave();

			((TableView<Object>) (TableView<?>) table).getItems().remove(1);
			Object added2 = first.getClass().getDeclaredConstructor().newInstance();
			added2.getClass().getDeclaredMethod("setAccount", String.class).invoke(added2, account);
			added2.getClass().getDeclaredMethod("setFund", String.class).invoke(added2, fund);
			added2.getClass().getDeclaredMethod("setAmount", String.class).invoke(added2, "10.00");
			added2.getClass().getDeclaredMethod("setSide", String.class).invoke(added2, "CREDIT");
			((TableView<Object>) (TableView<?>) table).getItems().add(added2);
			panel.onSave();
			return null;
		});

		BankingItemRecordService service = new BankingItemRecordService();
		List<BankingItemRecord> current = service.listAll().stream()
			.filter(r -> String.valueOf(transaction.getId()).equals(r.transactionId()))
			.filter(r -> "LEDGER_EDITOR".equals(r.source()))
			.toList();
		assertEquals(2, current.size(),
			"Repeated rewrites should keep one banking-item record per live split only.");
	}

	@Test
	void budgetWorkspacePanelsProvideEntryAndReportTables() throws Exception
	{
		BudgetPanel panel = runOnFxThread(BudgetPanel::new);
		runOnFxThread(() -> {
			TabPane tabs = getTabPane(panel);
			BorderPane editorRoot = (BorderPane) tabs.getTabs().get(0).getContent();
			assertInstanceOf(TableView.class, editorRoot.getCenter());

			BorderPane reportRoot = (BorderPane) tabs.getTabs().get(1).getContent();
			assertInstanceOf(TreeTableView.class, reportRoot.getCenter());
			return null;
		});
	}

	@Test
	void budgetSaveAndReportArePlumbedThroughStore() throws Exception
	{
		FakeBudgetWorkspaceStore store = new FakeBudgetWorkspaceStore();
		runOnFxThread(() -> {
			BudgetEditorPanel editor = new BudgetEditorPanel(store);
			editor.onSave();
			assertEquals(1, store.savedRows.size());

			BudgetVsActualPanel report = new BudgetVsActualPanel(store);
			TreeTableView<?> tree = (TreeTableView<?>) ((BorderPane) report.root()).getCenter();
			assertEquals(1, tree.getRoot().getChildren().size());
			return null;
		});
	}

	@Test
	void dateRangeContextNullNormalizesToAll() throws Exception
	{
		runOnFxThread(() -> {
			DateRangeContext.set(null);
			return null;
		});
		DateRange current = runOnFxThread(DateRangeContext::get);
		assertEquals(DateRange.ALL, current);
	}

	private static TabPane getTabPane(javafx.scene.layout.BorderPane pane)
	{
		assertInstanceOf(TabPane.class, pane.getCenter());
		return (TabPane) pane.getCenter();
	}

	private static String selectedTabText(MainApplicationView view)
	{
		TabPane tabPane = (TabPane) view.getCenter();
		return tabPane.getSelectionModel().getSelectedItem().getText();
	}

	private static TableView<?> ledgerTable(LedgerRegisterPanel panel)
	{
		assertInstanceOf(TableView.class, ((BorderPane) panel.root()).getCenter());
		return (TableView<?>) ((BorderPane) panel.root()).getCenter();
	}

	private static void invokeOpenSelected(LedgerRegisterPanel panel) throws Exception
	{
		java.lang.reflect.Method openSelected = LedgerRegisterPanel.class.getDeclaredMethod("openSelected");
		openSelected.setAccessible(true);
		openSelected.invoke(panel);
	}

	private static void invokeRefreshFromSelection(TransactionEditorPanel panel) throws Exception
	{
		java.lang.reflect.Method refresh = TransactionEditorPanel.class
			.getDeclaredMethod("refreshFromSelection");
		refresh.setAccessible(true);
		refresh.invoke(panel);
	}

	private static TableView<?> splitTable(TransactionEditorPanel panel)
	{
		Node root = panel.root();
		return (TableView<?>) ((javafx.scene.layout.VBox) ((BorderPane) root).getCenter())
			.getChildren().get(2);
	}

	private void seedLedgerTransactions() throws SQLException
	{
		Company company = new Company();
		AccountingTransaction first = transaction(101, "2026-01-05", "Payee A", "Memo A", "Cash/Bank");
		AccountingTransaction second = transaction(102, "2026-01-12", "Payee B", "Memo B", "Cash/Bank");
		company.getLedger().getJournal().replaceAllTransactions(List.of(first, second));
		new CompanyDataRepository().persist(company);
	}

	private AccountingTransaction transaction(int id, String date, String payee, String memo, String bank)
	{
		AccountingTransaction transaction = new AccountingTransaction();
		transaction.setId(id);
		transaction.setBookingDateTimestamp(System.currentTimeMillis() + id);
		transaction.setDate(date);
		transaction.setToFrom(payee);
		transaction.setMemo(memo);
		transaction.setBank(bank);

		AccountingEntry debit = new AccountingEntry(new BigDecimal("10.00"), "1000", AccountSide.DEBIT, "Cash");
		debit.setFundNumber("GENERAL");
		AccountingEntry credit = new AccountingEntry(new BigDecimal("10.00"), "4000", AccountSide.CREDIT, "Revenue");
		credit.setFundNumber("GENERAL");
		transaction.setEntries(new LinkedHashSet<>(List.of(debit, credit)));
		return transaction;
	}

	private void seedLedgerTransactionsWithSupplementalDetails() throws SQLException
	{
		Company company = new Company();
		AccountingTransaction transaction = transaction(301, "2026-02-02", "Donor A", "Pledge", "Cash/Bank");
		ReceivablesLine line = new ReceivablesLine();
		line.setReference("INV-2026-001");
		line.setDescription("Donor pledge");
		line.setAmount(new BigDecimal("125.50"));
		transaction.setSupplementalLines(List.of(line));
		company.getLedger().getJournal().replaceAllTransactions(List.of(transaction));
		new CompanyDataRepository().persist(company);
	}

	private static <T> T runOnFxThread(Callable<T> task) throws Exception
	{
		if (Platform.isFxApplicationThread())
		{
			return task.call();
		}
		FutureTask<T> future = new FutureTask<>(task);
		Platform.runLater(future);
		try
		{
			return future.get(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw e;
		}
		catch (TimeoutException e)
		{
			throw new IllegalStateException("Timed out waiting for FX task", e);
		}
		catch (ExecutionException e)
		{
			Throwable cause = e.getCause();
			if (cause instanceof Exception)
			{
				throw (Exception) cause;
			}
			throw new RuntimeException(cause);
		}
	}

	private static final class FakeBudgetWorkspaceStore extends BudgetWorkspaceStore
	{
		private List<BudgetEditorPanel.BudgetRow> savedRows = new ArrayList<>();

		private FakeBudgetWorkspaceStore()
		{
			super(null);
		}

		@Override
		public List<BudgetEditorPanel.BudgetRow> loadEditorRows() throws SQLException
		{
			return List.of(new BudgetEditorPanel.BudgetRow("Program Supplies", "General", "2026-Q1", "3500.00"));
		}

		@Override
		public int saveEditorRows(List<BudgetEditorPanel.BudgetRow> rows) throws SQLException
		{
			savedRows = new ArrayList<>(rows);
			return rows.size();
		}

		@Override
		public List<BudgetVsActualPanel.GroupRow> loadBudgetVsActual() throws SQLException
		{
			return List.of(new BudgetVsActualPanel.GroupRow(
				"General",
				List.of(new BudgetVsActualPanel.AccountRow("Program Supplies",
					new BigDecimal("3500.00"), BigDecimal.ZERO))));
		}
	}
}
