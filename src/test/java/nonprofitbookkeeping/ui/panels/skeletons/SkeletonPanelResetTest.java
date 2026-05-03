
package nonprofitbookkeeping.ui.panels.skeletons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.reports.jasper.runtime.ReportMetadata;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


/**
 * The Class SkeletonPanelResetTest.
 */
public class SkeletonPanelResetTest extends JavaFXTestBase
{
	
	/** The original user home. */
	private static String originalUserHome;
	
	/** The temp user home dir. */
	private static Path tempUserHomeDir;
	
	/** The dashboard panel. */
	private SkeletonDashboardPanel dashboardPanel;
	
	/** The journal panel. */
	private SkeletonJournalPanel journalPanel;
	
	/** The coa panel. */
	private SkeletonCoaPanel coaPanel;
	
	/** The reports panel. */
	private SkeletonReportsPanel reportsPanel;
	
	/**
	 * Override @see nonprofitbookkeeping.ui.JavaFXTestBase#start(javafx.stage.Stage) 
	 */
	@Start
	public void start(Stage stage)
	{
		this.dashboardPanel = new SkeletonDashboardPanel();
		this.journalPanel = new SkeletonJournalPanel();
		this.coaPanel = new SkeletonCoaPanel();
		this.reportsPanel = new SkeletonReportsPanel();
		VBox root =
			new VBox(this.dashboardPanel, this.journalPanel, this.coaPanel,
				this.reportsPanel);
		Scene scene = new Scene(root, 900, 700);
		stage.setScene(scene);
		stage.show();
		
	}
	
	/**
	 * Redirect user home.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@BeforeAll
	static void redirectUserHome() throws IOException
	{
		originalUserHome = System.getProperty("user.home");
		tempUserHomeDir = Files.createTempDirectory("npbk-panel-reset-home");
		System.setProperty("user.home", tempUserHomeDir.toString());
		
	}
	
	/**
	 * Reset company.
	 */
	@AfterEach
	public void resetCompany()
	{
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(null));
		WaitForAsyncUtils.waitForFxEvents();
		
	}
	
	/**
	 * Restore user home.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@AfterAll
	static void restoreUserHome() throws IOException
	{
		
		if (originalUserHome != null)
		{
			System.setProperty("user.home", originalUserHome);
		}
		
		if (tempUserHomeDir != null)
		{
			
			try (var paths = Files.walk(tempUserHomeDir))
			{
				paths.sorted(Comparator.reverseOrder())
					.forEach(path ->
					{
						
						try
						{
							Files.deleteIfExists(path);
						}
						catch (IOException ignored)
						{
							// Best-effort cleanup.
						}
						
					});
			}
			
		}
		
	}
	
	/**
	 * Dashboard clears when company closes.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void dashboardClearsWhenCompanyCloses() throws Exception
	{
		Company company = buildSampleCompany();
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(company));
		WaitForAsyncUtils.waitForFxEvents();
		
		TableView<?> table = getDashboardTable();
		assertTrue(fromFx(() -> !table.getItems().isEmpty()));
		
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(null));
		WaitForAsyncUtils.waitForFxEvents();
		
		assertTrue(fromFx(() -> table.getItems().isEmpty()));
		Label placeholder = (Label) fromFx(table::getPlaceholder);
		assertEquals("No company open.", placeholder.getText());
		Label nameLabel = getDashboardCompanyLabel();
		assertEquals("No company loaded", fromFx(nameLabel::getText));
		
	}
	
	/**
	 * Journal clears when company closes.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void journalClearsWhenCompanyCloses() throws Exception
	{
		Company company = buildSampleCompany();
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(company));
		WaitForAsyncUtils.waitForFxEvents();
		
		TableView<?> table = getJournalTable();
		assertTrue(fromFx(() -> !table.getItems().isEmpty()));
		
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(null));
		WaitForAsyncUtils.waitForFxEvents();
		
		assertTrue(fromFx(() -> table.getItems().isEmpty()));
		Label placeholder = (Label) fromFx(table::getPlaceholder);
		assertEquals("No journal entries found or company not open.",
			placeholder.getText());
		
	}
	
	/**
	 * Coa clears when company closes.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void coaClearsWhenCompanyCloses() throws Exception
	{
		Company company = buildSampleCompany();
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(company));
		WaitForAsyncUtils.waitForFxEvents();
		
		TreeItem<?> rootNode = getCoaRootNode();
		assertTrue(fromFx(() -> !rootNode.getChildren().isEmpty()));
		
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(null));
		WaitForAsyncUtils.waitForFxEvents();
		
		assertEquals(0, fromFx(() -> rootNode.getChildren().size()));
		TreeTableView<?> treeTable = getCoaTreeTable();
		Label placeholder = (Label) fromFx(treeTable::getPlaceholder);
		assertEquals("No company open.", placeholder.getText());
		
	}
	
	/**
	 * Reports clear when company closes.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void reportsClearWhenCompanyCloses() throws Exception
	{
		Company company = buildSampleCompany();
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(company));
		WaitForAsyncUtils.waitForFxEvents();
		
		ObservableList<ReportMetadata> dataList = getReportsDataList();
		Platform.runLater(() -> dataList.add(new ReportMetadata("Balance Sheet",
			"2024-03-15T10:15:30Z",
			"/tmp/balance-sheet.pdf")));
		WaitForAsyncUtils.waitForFxEvents();
		
		assertFalse(fromFx(dataList::isEmpty));
		
		Platform.runLater(() -> CurrentCompany.forceCompanyLoad(null));
		WaitForAsyncUtils.waitForFxEvents();
		
		assertTrue(fromFx(dataList::isEmpty));
		TableView<?> table = getReportsTable();
		Label placeholder = (Label) fromFx(table::getPlaceholder);
		assertEquals("No company open.", placeholder.getText());
		
	}
	
	/**
	 * Builds the sample company.
	 *
	 * @return the company
	 */
	private Company buildSampleCompany()
	{
		Company company = new Company();
		company.getCompanyProfile().setCompanyName("Sample Org");
		
		Account cash = new Account("100", "Cash", AccountType.ASSET,
			new BigDecimal("100.00"));
		Account revenue =
			new Account("400", "Revenue", AccountType.INCOME, BigDecimal.ZERO);
		Account expense = new Account("500", "Supplies", AccountType.EXPENSE,
			BigDecimal.ZERO);
		company.getChartOfAccounts().addAccount(cash);
		company.getChartOfAccounts().addAccount(revenue);
		company.getChartOfAccounts().addAccount(expense);
		
		AccountingTransaction tx1 = createTransaction(1L,
			new AccountingEntry(new BigDecimal("50.00"), "100",
				AccountSide.DEBIT, "Cash"),
			new AccountingEntry(new BigDecimal("50.00"), "400",
				AccountSide.CREDIT, "Revenue"));
		tx1.setDate("2024-03-10");
		tx1.setDescription("Membership dues");
		
		AccountingTransaction tx2 = createTransaction(2L,
			new AccountingEntry(new BigDecimal("20.00"), "500",
				AccountSide.DEBIT, "Supplies"),
			new AccountingEntry(new BigDecimal("20.00"), "100",
				AccountSide.CREDIT, "Cash"));
		tx2.setDate("2024-03-12");
		tx2.setDescription("Office supplies");
		
		company.getLedger().getJournal().addTransaction(tx1);
		company.getLedger().getJournal().addTransaction(tx2);
		return company;
		
	}
	
	/**
	 * Creates the transaction.
	 *
	 * @param timestamp the timestamp
	 * @param debit the debit
	 * @param credit the credit
	 * @return the accounting transaction
	 */
	private static AccountingTransaction createTransaction(long timestamp,
		AccountingEntry debit, AccountingEntry credit)
	{
		Set<AccountingEntry> entries = new LinkedHashSet<>();
		entries.add(debit);
		entries.add(credit);
		return new AccountingTransaction(new Account(), entries, null,
			timestamp);
		
	}
	
	/**
	 * Gets the dashboard table.
	 *
	 * @return the dashboard table
	 * @throws Exception the exception
	 */
	private TableView<?> getDashboardTable() throws Exception
	{
		Field field = SkeletonDashboardPanel.class
			.getDeclaredField("recentTransactionsTable");
		field.setAccessible(true);
		return (TableView<?>) field.get(this.dashboardPanel);
		
	}
	
	/**
	 * Gets the dashboard company label.
	 *
	 * @return the dashboard company label
	 * @throws Exception the exception
	 */
	private Label getDashboardCompanyLabel() throws Exception
	{
		Field field =
			SkeletonDashboardPanel.class.getDeclaredField("companyNameLabel");
		field.setAccessible(true);
		return (Label) field.get(this.dashboardPanel);
		
	}
	
	/**
	 * Gets the journal table.
	 *
	 * @return the journal table
	 * @throws Exception the exception
	 */
	private TableView<?> getJournalTable() throws Exception
	{
		Field field =
			SkeletonJournalPanel.class.getDeclaredField("journalDisplayTable");
		field.setAccessible(true);
		return (TableView<?>) field.get(this.journalPanel);
		
	}
	
	/**
	 * Gets the coa root node.
	 *
	 * @return the coa root node
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	private TreeItem<?> getCoaRootNode() throws Exception
	{
		Field field =
			SkeletonCoaPanel.class.getDeclaredField("rootAccountsNode");
		field.setAccessible(true);
		return (TreeItem<Account>) field.get(this.coaPanel);
		
	}
	
	/**
	 * Gets the coa tree table.
	 *
	 * @return the coa tree table
	 * @throws Exception the exception
	 */
	private TreeTableView<?> getCoaTreeTable() throws Exception
	{
		Field field = SkeletonCoaPanel.class.getDeclaredField("coaTreeTable");
		field.setAccessible(true);
		return (TreeTableView<?>) field.get(this.coaPanel);
		
	}
	
	/**
	 * Gets the reports data list.
	 *
	 * @return the reports data list
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	private ObservableList<ReportMetadata> getReportsDataList() throws Exception
	{
		Field field = SkeletonReportsPanel.class
			.getDeclaredField("generatedReportsDataList");
		field.setAccessible(true);
		return (ObservableList<ReportMetadata>) field.get(this.reportsPanel);
		
	}
	
	/**
	 * Gets the reports table.
	 *
	 * @return the reports table
	 * @throws Exception the exception
	 */
	private TableView<?> getReportsTable() throws Exception
	{
		Field field = SkeletonReportsPanel.class
			.getDeclaredField("generatedReportsTable");
		field.setAccessible(true);
		return (TableView<?>) field.get(this.reportsPanel);
		
	}
	
	/**
	 * From fx.
	 *
	 * @param <T> the generic type
	 * @param callable the callable
	 * @return the t
	 * @throws Exception the exception
	 */
	private static <T> T fromFx(java.util.concurrent.Callable<T> callable)
		throws Exception
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		Platform.runLater(() -> {
			
			try
			{
				future.complete(callable.call());
			}
			catch (Exception ex)
			{
				future.completeExceptionally(ex);
			}
			
		});
		return future.get(5, TimeUnit.SECONDS);
		
	}
	
}
