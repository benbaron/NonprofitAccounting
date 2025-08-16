package nonprofitbookkeeping.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Donor;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.model.SaleRecord;
import nonprofitbookkeeping.repository.AccountingTransactionRepository;
import nonprofitbookkeeping.repository.DonorRepository;
import nonprofitbookkeeping.repository.InventoryRepository;
import nonprofitbookkeeping.repository.SaleRecordRepository;

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

    public JsonToDatabaseMigration() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("nonprofitPU");
        EntityManager em = emf.createEntityManager();
        this.donorRepository = new DonorRepository(em);
        this.inventoryRepository = new InventoryRepository(em);
        this.saleRepository = new SaleRecordRepository(em);
        this.transactionRepository = new AccountingTransactionRepository(em);
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
}

