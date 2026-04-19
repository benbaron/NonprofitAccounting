package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.CompanyDataRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspacePanelsE2ETest
{
	@TempDir
	Path tempDir;

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
	void reset() throws Exception
	{
		TestDatabase.reset(this.tempDir);
		runOnFxThread(() -> {
			DateRangeContext.set(DateRange.ALL);
			return null;
		});
	}

	@Test
	void workspaceSubTabsReflectReadOnlyLedger() throws Exception
	{
		assertEquals(2,
			runOnFxThread(() -> getTabPane(new BudgetPanel()).getTabs().size()));
		assertEquals(1,
			runOnFxThread(() -> getTabPane(new LedgerPanel()).getTabs().size()));
		assertEquals(2,
			runOnFxThread(() -> getTabPane(new AssetsPanel()).getTabs().size()));
	}

	@Test
	void ledgerRegisterCanFilterRowsByDateRange() throws Exception
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
		assertEquals(2, filteredCount);
	}

	@Test
	void ledgerRegisterStatusShowsCounts() throws Exception
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

	private static void seedLedgerTransactions() throws Exception
	{
		AccountingTransaction jan05 = transaction(1001, "2026-01-05", "Vendor A", "Memo A");
		jan05.getEntries().add(entry("1000", "Operating", new BigDecimal("125.50")));
		jan05.getEntries().add(entry("2000", "Operating", new BigDecimal("125.50")));

		AccountingTransaction jan20 = transaction(1002, "2026-01-20", "Vendor B", "Memo B");
		jan20.getEntries().add(entry("3000", "Programs", new BigDecimal("80.00")));
		jan20.getEntries().add(entry("4000", "Programs", new BigDecimal("80.00")));

		Company company = new Company();
		company.getLedger().getJournal().replaceAllTransactions(java.util.List.of(jan05, jan20));
		new CompanyDataRepository().persist(company);
	}

	private static AccountingTransaction transaction(int id, String date, String payee,
		String memo)
	{
		AccountingTransaction txn = new AccountingTransaction();
		txn.setId(id);
		txn.setDate(date);
		txn.setToFrom(payee);
		txn.setMemo(memo);
		txn.setBank("Bank");
		return txn;
	}

	private static AccountingEntry entry(String account, String fund, BigDecimal amount)
	{
		AccountingEntry e = new AccountingEntry(amount, account, AccountSide.DEBIT);
		e.setAccountName(account);
		e.setFundNumber(fund);
		return e;
	}

	private static TableView<?> ledgerTable(LedgerRegisterPanel panel) throws Exception
	{
		Field tableField = LedgerRegisterPanel.class.getDeclaredField("txnTable");
		tableField.setAccessible(true);
		return (TableView<?>) tableField.get(panel);
	}

	private static TabPane getTabPane(BorderPane panel)
	{
		Node center = panel.getCenter();
		if (!(center instanceof TabPane tabPane))
		{
			throw new IllegalStateException("Expected TabPane center but got: " + center);
		}
		return tabPane;
	}

	private static <T> T runOnFxThread(Callable<T> action)
		throws InterruptedException, ExecutionException, TimeoutException
	{
		FutureTask<T> task = new FutureTask<>(action);
		Platform.runLater(task);
		return task.get(10, TimeUnit.SECONDS);
	}
}
