
package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.Journal;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.persistence.entity.CompanyEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * High level facade for database persistence.
 * <p>
 * The service exposes simple methods for saving and loading pieces of the
 * domain model without forcing callers to interact with {@link EntityManager}
 * or individual repositories directly.
 * </p>
 */
public class DatabaseService
{
	private static final Logger LOG =
		LoggerFactory.getLogger(DatabaseService.class);
	
	private final EntityManager entityManager;
	private final LedgerRepository ledgerRepository;
	private final AccountingTransactionRepository transactionRepository;
	private final DonorRepository donorRepository;
	private final InventoryRepository inventoryRepository;
	private final SaleRecordRepository saleRecordRepository;
	private final ScaRecordRepository scaRecordRepository;
	private final CompanyRepository companyRepository;
	private final DatabaseBackupService backupService;
	
	public DatabaseService()
	{
		this.entityManager = DatabaseManager.getEntityManager();
		this.ledgerRepository = new LedgerRepository(this.entityManager);
		this.transactionRepository =
			new AccountingTransactionRepository(this.entityManager);
		this.donorRepository = new DonorRepository(this.entityManager);
		this.inventoryRepository = new InventoryRepository(this.entityManager);
		this.saleRecordRepository =
			new SaleRecordRepository(this.entityManager);
		this.scaRecordRepository = new ScaRecordRepository(this.entityManager);
		this.companyRepository = new CompanyRepository(this.entityManager);
		this.backupService = new DatabaseBackupService();
		
	}
	
	/** Persist the company and its core components to the database. */
	public void saveCompany(Company company)
	{
		
		if (company == null)
		{
			LOG.warn("saveCompany called with null company");
			return;
		}
		
		LOG.debug("Persisting company '{}' (id: {})",
			company.getName(), company.getId());
		// Store the serialized company so the chart of accounts and other
		// top level data are available when reloading.
		this.companyRepository.saveOrUpdate(company);
		
		Ledger ledger = company.getLedger();
		
		if (ledger != null)
		{
			this.ledgerRepository.save(ledger);
		}
		// additional components like donors, inventory or sales could be saved
		// here when available from the Company model.
		
	}
	
	/**
	 * Loads a company instance from the database.
	 * Currently this reconstructs a {@link Company} with its {@link Ledger}
	 * transactions populated from the database.
	 */
	public Optional<Company> loadCompany(long companyId)
	{
		LOG.debug("Loading company with id {}", companyId);
		Optional<Company> companyOpt =
			this.companyRepository.findById(companyId);
		companyOpt.ifPresent(company -> {
			Journal journal = company.getLedger().getJournal();
			List<AccountingTransaction> txs =
				this.transactionRepository.findAll();
			
			if (txs != null)
			{
				txs.forEach(journal::addTransaction);
			}
			
			LOG.debug("Loaded company '{}' with {} transactions",
				company.getName(),
				journal.getJournalTransactions().size());
		});
		return companyOpt;
		
	}
	
	/**
	 * Convenience method to load the first company in the database. This is
	 * used by legacy parts of the application that assume a single company
	 * instance.
	 *
	 * @return the loaded {@link Company} or {@code null} if none exist
	 */
        public Company loadCompany()
        {
                return this.companyRepository.findFirstId()
                        .flatMap(this::loadCompany)
                        .orElse(null);

        }

	
	/** Create or update a company and return its id. */
	public long create(Company company)
	{
		saveCompany(company);
		return company.getId();
		
	}
	
	/** Find a company by id. */
	public Optional<Company> findById(long companyId)
	{
		return this.companyRepository.findById(companyId);
		
	}
	
	/** Delete a company by id. */
        public boolean delete(long companyId)
        {
                return this.companyRepository.delete(companyId);

        }

        /**
         * Count stored companies.
         */
        public long countCompanies()
        {
                return this.companyRepository.count();

        }
	
	/**
	 * Count stored companies.
	 */
	public long countCompanies()
	{
		return this.companyRepository.count();
		
	}
	
	public AccountingTransactionRepository getTransactionRepository()
	{
		return this.transactionRepository;
		
	}
	
	public LedgerRepository getLedgerRepository()
	{
		return this.ledgerRepository;
		
	}
	
	public DonorRepository getDonorRepository()
	{
		return this.donorRepository;
		
	}
	
	public InventoryRepository getInventoryRepository()
	{
		return this.inventoryRepository;
		
	}
	
	/**
	 * Create a SQL backup of the database at the specified path.
	 *
	 * @param filePath destination for the SQL script
	 * @throws SQLException if the backup fails
	 */
	public void backupDatabase(String filePath) throws SQLException
	{
		this.backupService.backupTo(filePath);
		
	}
	
	/**
	 * Restore the database from a previously created SQL backup.
	 *
	 * @param filePath source of the SQL script
	 * @throws SQLException if the restore fails
	 */
	public void restoreDatabase(String filePath) throws SQLException
	{
		this.backupService.restoreFrom(filePath);
		
	}
	
        /**
         * List all companies present in the database as domain objects.
         */
        public java.util.List<Company> listCompanies()
        {
                return this.companyRepository.findAll()
                        .stream()
                        .map(this.companyRepository::toModel)
                        .collect(Collectors.toList());

        }

        /**
         * List raw {@link nonprofitbookkeeping.persistence.entity.CompanyEntity} rows.
         * Used by maintenance utilities that need direct access to the underlying
         * persistence representation.
         */
        public java.util.List<nonprofitbookkeeping.persistence.entity.CompanyEntity> listCompanyEntities()
        {
                return this.companyRepository.findAll();

        }

	
}
