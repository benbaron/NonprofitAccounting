package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.SaleRecord;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SalesServiceTest {

    @Test
    void persistAndRetrieve() {
        SalesService service = new SalesService();
        service.addSale(new SaleRecord("1", "2024-01-01", "Item", 2, BigDecimal.TEN, BigDecimal.ONE));

        List<SaleRecord> sales = service.listSales();
        assertEquals(1, sales.size());
        assertEquals("1", sales.get(0).getId());
    }
}

