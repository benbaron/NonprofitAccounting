package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.AccountingTransactionBuilder;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.util.FormatUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ReconciliationServiceTest {

    private static final String CHECKING = "1000";
    private static final String SAVINGS = "2000";
    private static final String EXPENSE = "5000";

    private ReconciliationService service;
    private Company company;

    @BeforeEach
    void setUp() {
        this.company = new Company();
        this.company.getChartOfAccounts().addAccount(createAccount(CHECKING, "Checking", AccountType.CHECKING));
        this.company.getChartOfAccounts().addAccount(createAccount(SAVINGS, "Savings", AccountType.BANK));
        this.company.getChartOfAccounts().addAccount(createAccount(EXPENSE, "Office Supplies", AccountType.EXPENSE));

        CurrentCompany.forceCompanyLoad(this.company);
        this.service = new ReconciliationService();
    }

    @AfterEach
    void tearDown() {
        CurrentCompany.forceCompanyLoad(null);
    }

    @Test
    void getUnreconciledReturnsTransactionsForAccount() {
        AccountingTransaction check1 = addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("125.00"), 1001L,
                "2023-01-05", "Check #101");
        AccountingTransaction check2 = addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("75.00"), 1002L,
                "2023-01-10", "Check #102");
        addLedgerTransaction(SAVINGS, EXPENSE, new BigDecimal("20.00"), 2001L,
                "2023-01-12", "Savings Transfer");

        List<AccountingTransaction> checkingTx = ReconciliationService.getUnreconciled(CHECKING);
        assertEquals(2, checkingTx.size());
        List<Long> ids = checkingTx.stream()
                .map(AccountingTransaction::getBookingDateTimestamp)
                .collect(Collectors.toList());
        assertEquals(List.of(1001L, 1002L), ids);
        assertTrue(ReconciliationService.getUnreconciled("9999").isEmpty());
    }

    @Test
    void listReconcilableAccountsSortedByName() {
        addLedgerTransaction(SAVINGS, EXPENSE, new BigDecimal("40.00"), 2001L,
                "2023-02-01", "Transfer");
        addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("55.00"), 1001L,
                "2023-02-02", "Check #103");

        List<String> accounts = ReconciliationService.listReconcilableAccounts();
        assertEquals(List.of(CHECKING, SAVINGS), accounts);
    }

    @Test
    void reconcileRemovesTransactionsAndMarksClearBank() {
        AccountingTransaction check1 = addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("125.00"), 1001L,
                "2023-01-05", "Check #101");
        AccountingTransaction check2 = addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("75.00"), 1002L,
                "2023-01-10", "Check #102");

        this.service.reconcile(CHECKING, "2023-03-31", new BigDecimal("200.00"), List.of(1001L));

        assertEquals("2023-03-31", check1.getClearBank());
        assertEquals("", check2.getClearBank());
        List<Long> remaining = ReconciliationService.getUnreconciled(CHECKING).stream()
                .map(AccountingTransaction::getBookingDateTimestamp)
                .collect(Collectors.toList());
        assertEquals(List.of(1002L), remaining);
    }

    @Test
    void reconcileEntryMarksSingleTransaction() {
        AccountingTransaction check1 = addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("42.00"), 1001L,
                "2023-01-02", "Check #201");
        assertTrue(ReconciliationService.reconcileEntry(1001L));
        assertEquals("Cleared", check1.getClearBank());
        assertTrue(ReconciliationService.getUnreconciled(CHECKING).isEmpty());
    }

    @Test
    void getUnreconciledEntriesFiltersByDateRange() {
        addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("125.00"), 1001L,
                "2023-01-05", "Check #101");
        addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("75.50"), 1002L,
                "2023-02-10", "Check #102");

        List<String[]> january = ReconciliationService.getUnreconciledEntries(CHECKING,
                "2023-01-01", "2023-01-31");
        assertEquals(1, january.size());
        String[] row = january.get(0);
        assertEquals("1001", row[0]);
        assertEquals("2023-01-05", row[1]);
        assertEquals("Check #101", row[2]);
        assertEquals(FormatUtils.formatCurrency(new BigDecimal("125.00")), row[3]);
    }

    @Test
    void addTransactionToReconcileIndexesNewLedgerEntry() {
        addLedgerTransaction(CHECKING, EXPENSE, new BigDecimal("10.00"), 1001L,
                "2023-01-01", "Opening");

        AccountingTransaction newTx = createTransaction(CHECKING, EXPENSE, new BigDecimal("15.00"), 1002L,
                "2023-01-02", "Check #202");
        this.company.getLedger().getJournal().addTransaction(newTx);

        this.service.addTransactionToReconcile(newTx);

        List<Long> ids = ReconciliationService.getUnreconciled(CHECKING).stream()
                .map(AccountingTransaction::getBookingDateTimestamp)
                .collect(Collectors.toList());
        assertTrue(ids.contains(1002L));
    }

    private Account createAccount(String number, String name, AccountType type) {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setName(name);
        account.setAccountType(type);
        account.setIncreaseSide(AccountSide.DEBIT);
        return account;
    }

    private AccountingTransaction addLedgerTransaction(String debitAccount, String creditAccount,
            BigDecimal amount, long timestamp, String date, String memo) {
        AccountingTransaction tx = createTransaction(debitAccount, creditAccount, amount, timestamp, date, memo);
        this.company.getLedger().getJournal().addTransaction(tx);
        return tx;
    }

    private AccountingTransaction createTransaction(String debitAccount, String creditAccount,
            BigDecimal amount, long timestamp, String date, String memo) {
        AccountingTransaction tx = AccountingTransactionBuilder.create()
                .debit(amount, debitAccount)
                .credit(amount, creditAccount)
                .build();
        tx.setBookingDateTimestamp(timestamp);
        tx.setDate(date);
        tx.setMemo(memo);
        tx.setClearBank("");
        return tx;
    }
}
