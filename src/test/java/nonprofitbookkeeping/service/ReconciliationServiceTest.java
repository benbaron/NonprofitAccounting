package nonprofitbookkeeping.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.core.AccountingTransactionBuilder;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;

class ReconciliationServiceTest
{
        private static final String BANK_ACCOUNT = "1000";
        private static final String OFFSET_ACCOUNT = "4000";

        private Ledger ledger;
        private ReconciliationService service;
        private AccountingTransaction januaryDeposit;
        private AccountingTransaction februaryPayment;

        @BeforeEach
        void setUp()
        {
                new CurrentCompany();
                Company company = CurrentCompany.getCompany();
                CurrentCompany.markCompanyOpen();

                ChartOfAccounts chart = company.getChartOfAccounts();
                Account bank = new Account(BANK_ACCOUNT, "Checking", AccountSide.DEBIT);
                bank.setAccountType(AccountType.CHECKING);
                chart.addAccount(bank);

                Account income = new Account(OFFSET_ACCOUNT, "Contributions", AccountSide.CREDIT);
                income.setAccountType(AccountType.INCOME);
                chart.addAccount(income);

                this.ledger = company.getLedger();
                this.service = new ReconciliationService();

                this.januaryDeposit = createTransaction(new BigDecimal("125.00"), 1L, "2024-01-05", "Initial deposit");
                this.februaryPayment = createTransaction(new BigDecimal("80.00"), 2L, "2024-02-02", "Sponsor payment");
        }

        @Test
        void getUnreconciled_returnsTransactionsForReconcilableAccount()
        {
                List<AccountingTransaction> unreconciled = ReconciliationService.getUnreconciled(BANK_ACCOUNT);
                assertThat(unreconciled)
                        .extracting(AccountingTransaction::getMemo)
                        .containsExactly("Initial deposit", "Sponsor payment");
        }

        @Test
        void listReconcilableAccounts_includesBankAccount()
        {
                List<String> accounts = ReconciliationService.listReconcilableAccounts();
                assertThat(accounts).containsExactly(BANK_ACCOUNT);
        }

        @Test
        void reconcile_marksTransactionsClearedAndRemovesFromList()
        {
                this.service.reconcile(BANK_ACCOUNT, "2024-02-28", new BigDecimal("205.00"), List.of(1L));

                List<AccountingTransaction> remaining = ReconciliationService.getUnreconciled(BANK_ACCOUNT);
                assertThat(remaining)
                        .extracting(AccountingTransaction::getBookingDateTimestamp)
                        .containsExactly(2L);
                assertThat(this.januaryDeposit.getClearBank()).isEqualTo("2024-02-28");
        }

        @Test
        void addTransactionToReconcile_includesManualTransaction()
        {
                AccountingTransaction manual = createTransaction(new BigDecimal("30.00"), 3L, "2024-03-10", "Manual entry");
                this.ledger.getJournal().deleteTransaction(3L); // manual transaction should not live in ledger yet

                this.service.addTransactionToReconcile(manual);

                List<AccountingTransaction> unreconciled = ReconciliationService.getUnreconciled(BANK_ACCOUNT);
                assertThat(unreconciled)
                        .extracting(AccountingTransaction::getBookingDateTimestamp)
                        .contains(3L);
        }

        @Test
        void reconcileEntry_marksSingleTransactionCleared()
        {
                boolean cleared = ReconciliationService.reconcileEntry(2L);
                assertThat(cleared).isTrue();

                List<AccountingTransaction> remaining = ReconciliationService.getUnreconciled(BANK_ACCOUNT);
                assertThat(remaining)
                        .extracting(AccountingTransaction::getBookingDateTimestamp)
                        .containsExactly(1L);
                assertThat(this.februaryPayment.getClearBank()).isEqualTo("CLEARED");
        }

        @Test
        void getUnreconciledEntries_filtersByDateRange()
        {
                List<String[]> rows = ReconciliationService.getUnreconciledEntries(BANK_ACCOUNT, "2024-02-01", "2024-02-28");
                assertThat(rows).hasSize(1);
                assertThat(rows.get(0)[0]).isEqualTo("2024-02-02");
                assertThat(rows.get(0)[3]).isEqualTo("UNRECONCILED");
        }

        private AccountingTransaction createTransaction(BigDecimal amount, long id, String date, String memo)
        {
                AccountingTransactionBuilder builder = AccountingTransactionBuilder.create();
                builder.debit(amount, BANK_ACCOUNT);
                builder.credit(amount, OFFSET_ACCOUNT);
                AccountingTransaction tx = builder.build();
                tx.setBookingDateTimestamp(id);
                tx.setDate(date);
                tx.setMemo(memo);
                this.ledger.getJournal().addTransaction(tx);
                return tx;
        }
}
