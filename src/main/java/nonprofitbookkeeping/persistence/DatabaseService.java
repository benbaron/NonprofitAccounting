package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.Company;
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
    private final LedgerRepository ledgerRepository;
    private final AccountingTransactionRepository transactionRepository;
    private final DonorRepository donorRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final ScaRecordRepository scaRecordRepository;
    private final CompanyRepository companyRepository;

    public DatabaseService() {
        this.entityManager = EntityManagerProvider.getEntityManager();
        this.ledgerRepository = new LedgerRepository(entityManager);
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
            ledgerRepository.save(ledger);
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

    public LedgerRepository getLedgerRepository() {
        return ledgerRepository;
    }

    public DonorRepository getDonorRepository() {
        return donorRepository;
    }

    public InventoryRepository getInventoryRepository() {
        return inventoryRepository;

    }

    /**
     * Create a SQL backup of the database at the specified path.
     *
     * @param filePath destination for the SQL script
     * @throws SQLException if the backup fails
     */
    public void backupDatabase(String filePath) throws SQLException {
        backupService.backupTo(filePath);
    }

    /**
     * Restore the database from a previously created SQL backup.
     *
     * @param filePath source of the SQL script
     * @throws SQLException if the restore fails
     */
    public void restoreDatabase(String filePath) throws SQLException {
        backupService.restoreFrom(filePath);
    }
}
