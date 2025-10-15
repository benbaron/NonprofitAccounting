package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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

        List<Account> accounts = company.getChartOfAccounts() == null
                ? Collections.emptyList()
                : company.getChartOfAccounts().getAccounts();
        accountRepository.replaceAll(accounts);

        List<AccountingTransaction> transactions = company.getLedger() == null
                || company.getLedger().getJournal() == null
                ? Collections.emptyList()
                : company.getLedger().getJournal().getJournalTransactions();
        journalRepository.replaceAll(transactions);

        CompanyProfileModel profile = company.getCompanyProfileModel();
        profileRepository.save(profile);
    }

    public Company load() throws SQLException {
        Company company = new Company();
        company.getChartOfAccounts().replaceAllAccounts(accountRepository.listAll());
        company.getLedger().getJournal().replaceAllTransactions(journalRepository.listTransactions());
        profileRepository.load().ifPresent(company::setCompanyProfileModel);
        return company;
    }
}
