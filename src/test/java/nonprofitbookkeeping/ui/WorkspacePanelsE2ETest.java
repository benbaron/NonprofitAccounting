package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
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
		runOnFxThread(() -> {
			DateRangeContext.set(DateRange.ALL);
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
	void endToEndDateRangeWriteReadFiltersLedgerRows() throws Exception
	{
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);

		int initialCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(2, initialCount);

		runOnFxThread(() -> {
			DateRangeContext.set(new DateRange(java.time.LocalDate.of(2026, 1, 1),
				java.time.LocalDate.of(2026, 1, 10)));
			return null;
		});

		int filteredCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(1, filteredCount,
			"Write date range to context and read filtered ledger rows (E2E)");

		runOnFxThread(() -> {
			DateRangeContext.set(DateRange.ALL);
			return null;
		});

		int resetCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(2, resetCount);
	}


	@Test
	void endToEndDateRangeOneSidedBoundsFilterLedgerRows() throws Exception
	{
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);

		runOnFxThread(() -> {
			DateRangeContext.set(new DateRange(java.time.LocalDate.of(2026, 1, 10), null));
			return null;
		});
		int startOnlyCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(1, startOnlyCount,
			"Start-only range should include rows on/after the start date");

		runOnFxThread(() -> {
			DateRangeContext.set(new DateRange(null, java.time.LocalDate.of(2026, 1, 10)));
			return null;
		});
		int endOnlyCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(1, endOnlyCount,
			"End-only range should include rows on/before the end date");
	}


	@Test
	void malformedDateRowDoesNotCrashAndRemainsVisible() throws Exception
	{
		LedgerRegisterPanel ledgerPanel = runOnFxThread(LedgerRegisterPanel::new);

		runOnFxThread(() -> {
			injectMalformedRow(ledgerPanel);
			DateRangeContext.set(new DateRange(java.time.LocalDate.of(2026, 1, 1),
				java.time.LocalDate.of(2026, 1, 31)));
			return null;
		});

		int filteredCount = runOnFxThread(() -> ledgerTable(ledgerPanel).getItems().size());
		assertEquals(3, filteredCount,
			"Malformed date rows should not crash filtering and remain visible");
	}

	@Test
	void transactionEditorHasExpectedSplitColumnsAndRows() throws Exception
	{
		TransactionEditorPanel panel = runOnFxThread(TransactionEditorPanel::new);
		TableView<?> table = runOnFxThread(() -> {
			Node root = panel.getCenter();
			assertNotNull(root);
			return (TableView<?>) ((javafx.scene.layout.VBox) root).getChildren().get(2);
		});

		assertEquals(7, runOnFxThread(() -> table.getColumns().size()));
		assertEquals(2, runOnFxThread(() -> table.getItems().size()));
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
		assertInstanceOf(TableView.class, panel.getCenter());
		return (TableView<?>) panel.getCenter();
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void injectMalformedRow(LedgerRegisterPanel panel) throws Exception
	{
		Field allRowsField = LedgerRegisterPanel.class.getDeclaredField("allRows");
		allRowsField.setAccessible(true);
		javafx.collections.ObservableList allRows =
			(javafx.collections.ObservableList) allRowsField.get(panel);

		Class<?> rowClass = Class.forName(
			"nonprofitbookkeeping.ui.LedgerRegisterPanel$Row");
		Class<?>[] sig = java.util.Arrays.stream(rowClass.getRecordComponents())
			.map(RecordComponent::getType)
			.toArray(Class[]::new);
		java.lang.reflect.Constructor<?> ctor = rowClass.getDeclaredConstructor(sig);
		ctor.setAccessible(true);
		Object badRow = ctor.newInstance("not-a-date", "Bad Payee", "Bad Memo",
			"Cash/Bank", "Posted");
		allRows.add(badRow);

		Field txnTableField = LedgerRegisterPanel.class.getDeclaredField("txnTable");
		txnTableField.setAccessible(true);
		TableView table = (TableView) txnTableField.get(panel);
		table.getItems().add(badRow);
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
}
