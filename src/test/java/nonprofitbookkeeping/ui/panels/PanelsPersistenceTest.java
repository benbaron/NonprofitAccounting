package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.model.SaleRecord;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.service.SalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PanelsPersistenceTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetDatabase() throws Exception {
        TestDatabase.reset(this.tempDir);
    }

    @Test
    void donorsPersistenceFlow() throws Exception {
        DonorService service = new DonorService();
        service.loadDonors(null);

        DonorContact donor = new DonorContact(UUID.randomUUID().toString(), "Alice", "a@example.com", "555-0101");
        service.addDonor(donor);
        service.saveDonors(null);
        service.loadDonors(null);
        assertEquals(1, service.getAllDonors().size());

        DonorContact updated = new DonorContact(donor.getId(), "Alice Smith", "alice@example.com", "555-0102");
        service.editDonor("Alice", updated);
        service.saveDonors(null);
        service.loadDonors(null);
        assertEquals("Alice Smith", service.getAllDonors().get(0).getName());

        service.removeDonor("Alice Smith");
        service.saveDonors(null);
        service.loadDonors(null);
        assertTrue(service.getAllDonors().isEmpty());
    }

    @Test
    void fundsPersistenceFlow() throws Exception {
        FundAccountingService service = new FundAccountingService();
        service.loadFunds(null);

        Fund general = new Fund("General");
        general.setBalance(new BigDecimal("100.00"));
        Fund reserve = new Fund("Reserve");
        reserve.setBalance(new BigDecimal("50.00"));
        service.addFund(general);
        service.addFund(reserve);
        service.saveFunds(null);
        service.loadFunds(null);
        assertEquals(2, service.listFunds().size());

        service.transferFunds("General", "Reserve", new BigDecimal("25.00"));
        service.saveFunds(null);
        service.loadFunds(null);
        Map<String, BigDecimal> balances = service.getFundBalances();
        assertEquals(0, balances.get("General").compareTo(new BigDecimal("75.00")));
        assertEquals(0, balances.get("Reserve").compareTo(new BigDecimal("75.00")));

        service.removeFund("Reserve");
        service.saveFunds(null);
        service.loadFunds(null);
        assertEquals(1, service.listFunds().size());
    }

    @Test
    void inventoryPersistenceFlow() throws Exception {
        InventoryService service = new InventoryService();
        service.loadItems(null);

        String id = UUID.randomUUID().toString();
        InventoryItem item = new InventoryItem(id, "Laptop", new BigDecimal("1200.00"),
                LocalDate.now().toString(), 5).withAccumDep(BigDecimal.ZERO);
        service.addItem(item);
        service.saveItems(null);
        service.loadItems(null);
        assertEquals(1, service.listItems().size());

        InventoryItem updated = new InventoryItem(id, "Laptop Pro", new BigDecimal("1500.00"),
                LocalDate.now().toString(), 5).withAccumDep(BigDecimal.ZERO);
        service.updateItem(updated);
        service.saveItems(null);
        service.loadItems(null);
        assertEquals("Laptop Pro", service.listItems().get(0).getName());

        service.deleteItem(id);
        service.saveItems(null);
        service.loadItems(null);
        assertTrue(service.listItems().isEmpty());
    }

    @Test
    void salesPersistenceFlow() throws Exception {
        SalesService service = new SalesService();
        service.loadSales(null);

        String id = UUID.randomUUID().toString();
        SaleRecord sale = new SaleRecord(id, LocalDate.now().toString(), "Book", 2,
                new BigDecimal("10.00"), new BigDecimal("3.00"));
        service.addSale(sale);
        service.saveSales(null);
        service.loadSales(null);
        assertEquals(1, service.listSales().size());
        assertEquals("Book", service.listSales().get(0).getItem());

        SaleRecord updated = new SaleRecord(id, LocalDate.now().plusDays(1).toString(), "Book Deluxe", 2,
                new BigDecimal("12.00"), new BigDecimal("4.00"));
        service.clear();
        service.addSale(updated);
        service.saveSales(null);
        service.loadSales(null);
        assertEquals("Book Deluxe", service.listSales().get(0).getItem());

        service.removeSale(id);
        service.saveSales(null);
        service.loadSales(null);
        assertTrue(service.listSales().isEmpty());
    }
}
