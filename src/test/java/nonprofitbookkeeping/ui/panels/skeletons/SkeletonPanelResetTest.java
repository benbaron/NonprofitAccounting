package nonprofitbookkeeping.ui.panels.skeletons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

public class SkeletonPanelResetTest extends JavaFXTestBase
{
        private SkeletonDashboardPanel dashboardPanel;
        private SkeletonJournalPanel journalPanel;

        @Start
        public void start(Stage stage)
        {
                this.dashboardPanel = new SkeletonDashboardPanel();
                this.journalPanel = new SkeletonJournalPanel();
                VBox root = new VBox(this.dashboardPanel, this.journalPanel);
                Scene scene = new Scene(root, 900, 700);
                stage.setScene(scene);
                stage.show();
        }

        @AfterEach
        public void resetCompany()
        {
                Platform.runLater(() -> CurrentCompany.forceCompanyLoad(null));
                WaitForAsyncUtils.waitForFxEvents();
        }

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
                assertEquals("No journal entries found or company not open.", placeholder.getText());
        }

        private Company buildSampleCompany()
        {
                Company company = new Company();
                company.getCompanyProfile().setCompanyName("Sample Org");

                Account cash = new Account("100", "Cash", AccountType.ASSET, new BigDecimal("100.00"));
                Account revenue = new Account("400", "Revenue", AccountType.INCOME, BigDecimal.ZERO);
                Account expense = new Account("500", "Supplies", AccountType.EXPENSE, BigDecimal.ZERO);
                company.getChartOfAccounts().addAccount(cash);
                company.getChartOfAccounts().addAccount(revenue);
                company.getChartOfAccounts().addAccount(expense);

                AccountingTransaction tx1 = createTransaction(1L,
                        new AccountingEntry(new BigDecimal("50.00"), "100", AccountSide.DEBIT, "Cash"),
                        new AccountingEntry(new BigDecimal("50.00"), "400", AccountSide.CREDIT, "Revenue"));
                tx1.setDate("2024-03-10");
                tx1.setDescription("Membership dues");

                AccountingTransaction tx2 = createTransaction(2L,
                        new AccountingEntry(new BigDecimal("20.00"), "500", AccountSide.DEBIT, "Supplies"),
                        new AccountingEntry(new BigDecimal("20.00"), "100", AccountSide.CREDIT, "Cash"));
                tx2.setDate("2024-03-12");
                tx2.setDescription("Office supplies");

                company.getLedger().getJournal().addTransaction(tx1);
                company.getLedger().getJournal().addTransaction(tx2);
                return company;
        }

        private static AccountingTransaction createTransaction(long timestamp, AccountingEntry debit, AccountingEntry credit)
        {
                Set<AccountingEntry> entries = new LinkedHashSet<>();
                entries.add(debit);
                entries.add(credit);
                return new AccountingTransaction(new Account(), entries, null, timestamp);
        }

        @SuppressWarnings("unchecked")
        private TableView<?> getDashboardTable() throws Exception
        {
                Field field = SkeletonDashboardPanel.class.getDeclaredField("recentTransactionsTable");
                field.setAccessible(true);
                return (TableView<?>) field.get(this.dashboardPanel);
        }

        private Label getDashboardCompanyLabel() throws Exception
        {
                Field field = SkeletonDashboardPanel.class.getDeclaredField("companyNameLabel");
                field.setAccessible(true);
                return (Label) field.get(this.dashboardPanel);
        }

        @SuppressWarnings("unchecked")
        private TableView<?> getJournalTable() throws Exception
        {
                Field field = SkeletonJournalPanel.class.getDeclaredField("journalDisplayTable");
                field.setAccessible(true);
                return (TableView<?>) field.get(this.journalPanel);
        }

        private static <T> T fromFx(java.util.concurrent.Callable<T> callable) throws Exception
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
