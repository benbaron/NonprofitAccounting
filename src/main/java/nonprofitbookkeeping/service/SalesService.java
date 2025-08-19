package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.SaleRecord;
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.SaleRecordRepository;

import java.util.List;

/**
 * Service layer for {@link SaleRecord} entities.
 */
public class SalesService {

    /** Return all sales. */
    public List<SaleRecord> listSales() {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            SaleRecordRepository repository = new SaleRecordRepository(em);
            return repository.findAll();
        }
    }

    /** Add a sale record. */
    public void addSale(SaleRecord sale) {
        if (sale != null) {
            try (EntityManager em = DatabaseManager.getEntityManager()) {
                SaleRecordRepository repository = new SaleRecordRepository(em);
                repository.save(sale);
            }
        }
    }

    /** Remove a sale by id. */
    public boolean removeSale(String id) {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            SaleRecordRepository repository = new SaleRecordRepository(em);
            return repository.delete(id);
        }
    }

    /** Delete all sale records. */
    public void clear() {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            SaleRecordRepository repository = new SaleRecordRepository(em);
            for (SaleRecord s : repository.findAll()) {
                repository.delete(s.getId());
            }
        }
    }

    /** Compatibility stub: data is persisted automatically. */
    public void saveSales(java.io.File companyDirectory) {
        // no-op
    }

    /** Compatibility stub: loading from JSON is unnecessary. */
    public void loadSales(java.io.File companyDirectory) {
        // no-op
    }
}

