package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.Journal;
import nonprofitbookkeeping.model.Ledger;

import java.util.List;
import java.util.Optional;

/**
 * High level facade for database persistence.
 * <p>
 * The service exposes simple methods for saving and loading pieces of the
 * domain model without forcing callers to interact with {@link EntityManager}
 * or individual repositories directly.
 * </p>
 */
public class DatabaseService {

    private final EntityManager entityManager;
    private final AccountingTransactionRepository transactionRepository;
    private final DonorRepository donorRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final ScaRecordRepository scaRecordRepository;
    private final CompanyRepository companyRepository;

    public DatabaseService() {
        this.entityManager = EntityManagerProvider.getEntityManager();
        this.transactionRepository = new AccountingTransactionRepository(entityManager);
        this.donorRepository = new DonorRepository(entityManager);
        this.inventoryRepository = new InventoryRepository(entityManager);
        this.saleRecordRepository = new SaleRecordRepository(entityManager);
        this.scaRecordRepository = new ScaRecordRepository(entityManager);
        this.companyRepository = new CompanyRepository(entityManager);
    }

    /** Persist core parts of the company to the database. */
    public void saveCompany(Company company) {
        if (company == null) {
            return;
        }
        Ledger ledger = company.getLedger();
        if (ledger != null) {
            List<AccountingTransaction> txs = ledger.getTransactions();
            if (txs != null) {
                txs.forEach(transactionRepository::save);
            }
        }
        // additional components like donors, inventory or sales could be saved
        // here when available from the Company model.
    }

    /**
     * Loads a company instance from the database.
     * Currently this reconstructs a {@link Company} with its {@link Ledger}
     * transactions populated from the database.
     */
    public Optional<Company> loadCompany(long companyId) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        companyOpt.ifPresent(company -> {
            Journal journal = company.getLedger().getJournal();
            List<AccountingTransaction> txs = transactionRepository.findAll();
            if (txs != null) {
                txs.forEach(journal::addTransaction);
            }
        });
        return companyOpt;
    }

    /** Create a new company and return its id. */
    public long create(Company company) {
        long id = companyRepository.create(company);
        saveCompany(company);
        return id;
    }

    /** Find a company by id. */
    public Optional<Company> findById(long companyId) {
        return companyRepository.findById(companyId);
    }

    /** Delete a company by id. */
    public boolean delete(long companyId) {
        return companyRepository.delete(companyId);
    }

    public AccountingTransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public DonorRepository getDonorRepository() {
        return donorRepository;
    }

    public InventoryRepository getInventoryRepository() {
        return inventoryRepository;
    }

    public SaleRecordRepository getSaleRecordRepository() {
        return saleRecordRepository;
    }

    public ScaRecordRepository getScaRecordRepository() {
        return scaRecordRepository;
    }
}
