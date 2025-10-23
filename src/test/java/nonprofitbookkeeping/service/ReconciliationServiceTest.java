package nonprofitbookkeeping.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.core.AccountingTransactionBuilder;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

class ReconciliationServiceTest
{
        private ReconciliationService service;
        private Company company;
        private Account checking;
        private Account offset;
        private Account card;
        private Account expense;
        private AccountingTransaction deposit;
        private AccountingTransaction cardCharge;

        @BeforeEach void setUp()
        {
                ReconciliationService.reset();
                this.service = new ReconciliationService();

                this.company = new Company();
                this.checking = new Account("100", "Checking", AccountType.BANK, BigDecimal.ZERO);
                this.offset = new Account("200", "Equity", AccountType.EQUITY, BigDecimal.ZERO);
                this.card = new Account("300", "Corporate Card", AccountType.CREDITCARD, BigDecimal.ZERO);
                this.expense = new Account("400", "Supplies", AccountType.EXPENSE, BigDecimal.ZERO);

                this.company.getChartOfAccounts().addAccount(this.checking);
                this.company.getChartOfAccounts().addAccount(this.offset);
                this.company.getChartOfAccounts().addAccount(this.card);
                this.company.getChartOfAccounts().addAccount(this.expense);

                this.deposit = AccountingTransactionBuilder.create()
                        .debit(new BigDecimal("150.00"), this.checking.getAccountNumber())
                        .credit(new BigDecimal("150.00"), this.offset.getAccountNumber())
                        .build();
                this.deposit.setDate("2024-01-10");
                this.deposit.setDescription("Donation receipt");
                this.deposit.setBookingDateTimestamp(1L);
                this.company.getLedger().getJournal().addTransaction(this.deposit);

                this.cardCharge = AccountingTransactionBuilder.create()
                        .debit(new BigDecimal("45.00"), this.expense.getAccountNumber())
                        .credit(new BigDecimal("45.00"), this.card.getAccountNumber())
                        .build();
                this.cardCharge.setDate("2024-01-12");
                this.cardCharge.setDescription("Office supplies");
                this.cardCharge.setBookingDateTimestamp(2L);
                this.company.getLedger().getJournal().addTransaction(this.cardCharge);

                CurrentCompany.forceCompanyLoad(this.company);
                // Warm the reconciliation cache with ledger data.
                ReconciliationService.listReconcilableAccounts();
        }

        @AfterEach void tearDown()
        {
                CurrentCompany.forceCompanyLoad(null);
                ReconciliationService.reset();
        }

        @Test void listReconcilableAccountsReturnsDisplayNames()
        {
                List<String> accounts = ReconciliationService.listReconcilableAccounts();
                assertEquals(2, accounts.size());
                assertTrue(accounts.contains("Checking"));
                assertTrue(accounts.contains("Corporate Card"));
        }

        @Test void getUnreconciledReturnsTransactionsForDisplayName()
        {
                List<AccountingTransaction> checkingTx = ReconciliationService.getUnreconciled("Checking");
                assertEquals(1, checkingTx.size());
                assertSame(this.deposit, checkingTx.get(0));
        }

        @Test void getUnreconciledAcceptsAccountNumber()
        {
                List<AccountingTransaction> checkingTx = ReconciliationService
                        .getUnreconciled(this.checking.getAccountNumber());
                assertEquals(1, checkingTx.size());
                assertSame(this.deposit, checkingTx.get(0));
        }

        @Test void getUnreconciledEntriesFiltersByDate()
        {
                List<String[]> rows = ReconciliationService.getUnreconciledEntries("Checking", "2024-01-01", "2024-01-31");
                assertEquals(1, rows.size());
                assertArrayEquals(new String[] { "1", "2024-01-10", "$150.00", "Donation receipt" }, rows.get(0));

                List<String[]> none = ReconciliationService.getUnreconciledEntries("Checking", "2024-02-01", "2024-02-28");
                assertTrue(none.isEmpty());
        }

        @Test void reconcileEntryRemovesTransaction()
        {
                assertTrue(ReconciliationService.reconcileEntry(1L));
                assertTrue(ReconciliationService.getUnreconciled("Checking").isEmpty());
                assertFalse(ReconciliationService.reconcileEntry(99L));
        }

        @Test void reconcileRemovesOnlySpecifiedTransactions()
        {
                this.service.reconcile("Checking", "2024-01-31", BigDecimal.ZERO, List.of(1L));
                assertTrue(ReconciliationService.getUnreconciled("Checking").isEmpty());

                List<AccountingTransaction> cardTx = ReconciliationService.getUnreconciled("Corporate Card");
                assertEquals(1, cardTx.size());
                assertSame(this.cardCharge, cardTx.get(0));
        }

        @Test void addTransactionToReconcileAddsNewTransactions()
        {
                AccountingTransaction manual = AccountingTransactionBuilder.create()
                        .debit(new BigDecimal("25.00"), this.checking.getAccountNumber())
                        .credit(new BigDecimal("25.00"), this.offset.getAccountNumber())
                        .build();
                manual.setBookingDateTimestamp(3L);
                manual.setDate("2024-02-01");
                manual.setDescription("Manual adjustment");

                this.service.addTransactionToReconcile(manual);

                List<AccountingTransaction> checkingTx = ReconciliationService.getUnreconciled("Checking");
                assertEquals(2, checkingTx.size());
                assertTrue(checkingTx.contains(manual));
        }
}
