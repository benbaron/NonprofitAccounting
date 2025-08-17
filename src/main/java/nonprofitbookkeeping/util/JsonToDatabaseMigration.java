package nonprofitbookkeeping.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.Donor;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.model.SaleRecord;

import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.model.scaledger.LedgerContainer;
import nonprofitbookkeeping.model.scaledger.LedgerEntry;
import nonprofitbookkeeping.persistence.dao.LedgerEntryDao;
import nonprofitbookkeeping.persistence.dao.ReportConfigurationDao;
import nonprofitbookkeeping.persistence.entity.LedgerEntryEntity;
import nonprofitbookkeeping.persistence.entity.SupplementalRecordEntity;

import nonprofitbookkeeping.repository.AccountingTransactionRepository;
import nonprofitbookkeeping.repository.DonorRepository;
import nonprofitbookkeeping.repository.InventoryRepository;
import nonprofitbookkeeping.repository.SaleRecordRepository;
import nonprofitbookkeeping.repository.SupplementalRecordRepository;


import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility that migrates existing JSON-based data into the database using the
 * configured JPA repositories. It relies on {@link JacksonDataStorer} for
 * consistent Jackson configuration when reading JSON files.
 */
public class JsonToDatabaseMigration {

    private final JacksonDataStorer dataStorer = new JacksonDataStorer();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DonorRepository donorRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRecordRepository saleRepository;
    private final AccountingTransactionRepository transactionRepository;
    private final LedgerEntryDao ledgerEntryDao;
    private final ReportConfigurationDao reportConfigDao;

    private final SupplementalRecordRepository supplementalRecordRepository;

    public JsonToDatabaseMigration(DatabaseService db) {
        this.donorRepository = db.getDonorRepository();
        this.inventoryRepository = db.getInventoryRepository();
        this.saleRepository = db.getSaleRecordRepository();
        this.transactionRepository = db.getTransactionRepository();
    }


    public JsonToDatabaseMigration() {

        EntityManager em = DatabaseManager.getEntityManager();
        this.donorRepository = new DonorRepository(em);
        this.inventoryRepository = new InventoryRepository(em);
        this.saleRepository = new SaleRecordRepository(em);
        this.transactionRepository = new AccountingTransactionRepository(em);
        this.ledgerEntryDao = new LedgerEntryDao(em);
        this.reportConfigDao = new ReportConfigurationDao(em);

        this.supplementalRecordRepository = new SupplementalRecordRepository(em);


    }

    /** Migrate donors from a JSON file. */
    public void migrateDonors(File donorsJson) throws IOException {
        List<Donor> donors = mapper.readValue(donorsJson,
                mapper.getTypeFactory().constructCollectionType(List.class, Donor.class));
        donors.forEach(donorRepository::save);
    }

    /** Migrate inventory items from a JSON file. */
    public void migrateInventory(File inventoryJson) throws IOException {
        List<InventoryItem> items = mapper.readValue(inventoryJson,
                mapper.getTypeFactory().constructCollectionType(List.class, InventoryItem.class));
        items.forEach(inventoryRepository::save);
    }

    /** Migrate sale records from a JSON file. */
    public void migrateSales(File salesJson) throws IOException {
        List<SaleRecord> sales = mapper.readValue(salesJson,
                mapper.getTypeFactory().constructCollectionType(List.class, SaleRecord.class));
        sales.forEach(saleRepository::save);
    }

    /** Migrate accounting transactions from a JSON file. */
    public void migrateTransactions(File transactionsJson) throws IOException {
        List<AccountingTransaction> txs = mapper.readValue(transactionsJson,
                mapper.getTypeFactory().constructCollectionType(List.class, AccountingTransaction.class));
        txs.forEach(transactionRepository::save);
    }

    /** Migrate ledger entries from a JSON file representing a {@link LedgerContainer}. */
    public void migrateLedger(File ledgerJson) throws IOException {
        LedgerContainer container = mapper.readValue(ledgerJson, LedgerContainer.class);
        saveLedgerEntries(container.getLedgerQ1());
        saveLedgerEntries(container.getLedgerQ2());
        saveLedgerEntries(container.getLedgerQ3());
        saveLedgerEntries(container.getLedgerQ4());
    }

    private void saveLedgerEntries(List<LedgerEntry> entries) {
        for (LedgerEntry le : entries) {
            LedgerEntryEntity entity = new LedgerEntryEntity();
            entity.setEntryDate(le.getEntryDate());
            entity.setCheckNumber(le.getCheckNumber());
            entity.setCleared(le.isCleared());
            entity.setToFrom(le.getToFrom());
            entity.setMemoString(le.getMemoString());
            entity.setBudgetTracking(le.getBudgetTracking());
            addSupplemental(entity, le.getAmount(), le.getAssetAccount(), le.getIncomeAccount(),
                    le.getExpenseAccount(), le.getFundName(), 1);
            addSupplemental(entity, le.amount2, le.assetAccount2, le.incomeAccount2,
                    le.expenseAccount2, le.fundName2, 2);
            addSupplemental(entity, le.amount3, le.assetAccount3, le.incomeAccount3,
                    le.expenseAccount3, le.fundName3, 3);
            addSupplemental(entity, le.amount4, le.assetAccount4, le.incomeAccount4,
                    le.expenseAccount4, le.fundName4, 4);
            ledgerEntryDao.save(entity);
        }
    }

    private void addSupplemental(LedgerEntryEntity entity, double amt, String asset,
                                 String income, String expense, String fund, int seq) {
        if (asset != null || income != null || expense != null || fund != null || amt != 0) {
            SupplementalRecordEntity sr = new SupplementalRecordEntity();
            sr.setAmount(amt);
            sr.setAssetAccount(asset);
            sr.setIncomeAccount(income);
            sr.setExpenseAccount(expense);
            sr.setFundName(fund);
            sr.setSequenceNumber(seq);
            sr.setLedgerEntry(entity);
            entity.getSupplementalRecords().add(sr);
        }
    }

    /** Migrate report configurations from a JSON file. */
    public void migrateReportConfigurations(File configJson) throws IOException {
        List<ReportConfiguration> configs = mapper.readValue(configJson,
                mapper.getTypeFactory().constructCollectionType(List.class, ReportConfiguration.class));
        configs.forEach(reportConfigDao::save);

    }
}


    /**
     * Convenience method that reads an entire company data archive (the format
     * produced by {@link JacksonDataStorer}) and persists its contents to the
     * database using the configured repositories.
     */
    public void migrateCompanyArchive(File companyZip) throws IOException {
        Company company = dataStorer.loadData(Company.class, companyZip);
        DatabaseService db = new DatabaseService();
        db.saveCompany(company);
    }
}
