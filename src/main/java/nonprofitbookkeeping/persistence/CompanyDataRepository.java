package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Persists and loads the core {@link Company} aggregates using normalized tables instead of the legacy blob store.
 */
public class CompanyDataRepository {

    private final AccountRepository accountRepository = new AccountRepository();
    private final JournalRepository journalRepository = new JournalRepository();
    private final CompanyProfileRepository profileRepository = new CompanyProfileRepository();

    public void persist(Company company) throws SQLException {
        if (company == null) {
            return;
        }

        List<AccountingTransaction> transactions = company.getLedger() == null
                || company.getLedger().getJournal() == null
                ? Collections.emptyList()
                : company.getLedger().getJournal().getJournalTransactions();

        List<Account> accounts = company.getChartOfAccounts() == null
                ? Collections.emptyList()
                : company.getChartOfAccounts().getAccounts();
        accountRepository.replaceAll(ensureAccountsForTransactions(accounts, transactions));

        journalRepository.replaceAll(transactions);

        CompanyProfileModel profile = company.getCompanyProfileModel();
        profileRepository.save(profile);
    }

    private List<Account> ensureAccountsForTransactions(List<Account> accounts,
            List<AccountingTransaction> transactions) {
        Map<String, Account> byNumber = new LinkedHashMap<>();

        if (accounts != null) {
            for (Account account : accounts) {
                if (account == null) {
                    continue;
                }

                String number = safeAccountNumber(account);
                if (number == null || number.isBlank()) {
                    continue;
                }

                byNumber.putIfAbsent(number, account);
            }
        }

        if (transactions != null) {
            for (AccountingTransaction transaction : transactions) {
                if (transaction == null || transaction.getEntries() == null) {
                    continue;
                }

                for (AccountingEntry entry : transaction.getEntries()) {
                    if (entry == null) {
                        continue;
                    }

                    String accountNumber = entry.getAccountNumber();
                    if (accountNumber == null || accountNumber.isBlank()
                            || byNumber.containsKey(accountNumber)) {
                        continue;
                    }

                    Account placeholder = new Account();
                    placeholder.setAccountNumber(accountNumber);
                    String accountName = entry.getAccountName();
                    if (accountName == null || accountName.isBlank()) {
                        accountName = accountNumber;
                    }
                    placeholder.setName(accountName);
                    if (entry.getAccountSide() != null) {
                        placeholder.setIncreaseSide(entry.getAccountSide());
                    }
                    byNumber.put(accountNumber, placeholder);
                }
            }
        }

        return new ArrayList<>(byNumber.values());
    }

    private String safeAccountNumber(Account account) {
        try {
            return account.getAccountNumber();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public Company load() throws SQLException {
        Company company = new Company();
        company.getChartOfAccounts().replaceAllAccounts(accountRepository.listAll());
        company.getLedger().getJournal().replaceAllTransactions(journalRepository.listTransactions());
        profileRepository.load().ifPresent(company::setCompanyProfileModel);
        return company;
    }

    private List<Account> ensureAccountsForTransactions(List<Account> accounts,
            List<AccountingTransaction> transactions)
    {
        List<Account> normalizedAccounts = new ArrayList<>();
        Set<String> seenAccountNumbers = new LinkedHashSet<>();

        if (accounts != null)
        {
            for (Account account : accounts)
            {
                if (account == null)
                {
                    continue;
                }

                String accountNumber;
                try
                {
                    accountNumber = account.getAccountNumber();
                }
                catch (NullPointerException ex)
                {
                    continue;
                }

                if (accountNumber == null || accountNumber.trim().isEmpty())
                {
                    continue;
                }

                if (seenAccountNumbers.add(accountNumber))
                {
                    normalizedAccounts.add(account);
                }
            }
        }

        if (transactions != null)
        {
            for (AccountingTransaction transaction : transactions)
            {
                if (transaction == null || transaction.getEntries() == null)
                {
                    continue;
                }

                for (AccountingEntry entry : transaction.getEntries())
                {
                    if (entry == null)
                    {
                        continue;
                    }

                    String accountNumber = entry.getAccountNumber();
                    if (accountNumber == null || accountNumber.trim().isEmpty())
                    {
                        continue;
                    }

                    if (seenAccountNumbers.contains(accountNumber))
                    {
                        continue;
                    }

                    Account placeholder = new Account();
                    placeholder.setAccountNumber(accountNumber);

                    if (entry.getAccountName() != null && !entry.getAccountName().isBlank())
                    {
                        placeholder.setName(entry.getAccountName());
                    }
                    else
                    {
                        placeholder.setName("Imported account " + accountNumber);
                    }

                    if (entry.getAccountSide() != null)
                    {
                        placeholder.setIncreaseSide(entry.getAccountSide());
                    }

                    placeholder.setOpeningBalance(BigDecimal.ZERO);
                    normalizedAccounts.add(placeholder);
                    seenAccountNumbers.add(accountNumber);
                }
            }
        }

        return normalizedAccounts;
    }
}
