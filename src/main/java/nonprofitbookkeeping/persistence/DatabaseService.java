package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.Ledger;

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

    public DatabaseService() {
        this.entityManager = EntityManagerProvider.getEntityManager();
        this.ledgerRepository = new LedgerRepository(entityManager);
        this.transactionRepository = new AccountingTransactionRepository(entityManager);
        this.donorRepository = new DonorRepository(entityManager);
        this.inventoryRepository = new InventoryRepository(entityManager);
        this.saleRecordRepository = new SaleRecordRepository(entityManager);
        this.scaRecordRepository = new ScaRecordRepository(entityManager);
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
    public Company loadCompany() {
        Company company = new Company();
        Ledger ledger = ledgerRepository.findFirst();
        if (ledger != null) {
            company.setLedger(ledger);
        }
        return company;
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

    public SaleRecordRepository getSaleRecordRepository() {
        return saleRecordRepository;
    }

    public ScaRecordRepository getScaRecordRepository() {
        return scaRecordRepository;
    }
}
