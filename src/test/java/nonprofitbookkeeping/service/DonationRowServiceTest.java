package nonprofitbookkeeping.service;

import nonprofitbookkeeping.reports.datasource.scareports.DonationRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DonationRowServiceTest {

    private DonationRowService service;
    private File tempDir;

    @BeforeEach
    void setUp() throws Exception {
        this.service = new DonationRowService();
        this.tempDir = Files.createTempDirectory("donationRows").toFile();
    }

    @Test
    void testAddAndList() {
        DonationRow row = new DonationRow();
        row.setOrganizationName("Org");
        service.addRow(row);
        List<DonationRow> list = service.listRows();
        assertEquals(1, list.size());
        assertEquals("Org", list.get(0).getOrganizationName());
    }

    @Test
    void testSaveAndLoad() throws Exception {
        DonationRow row = new DonationRow("Org", "123", "Reason", "1", "2024-01-01", BigDecimal.ONE);
        service.addRow(row);
        service.saveRows(tempDir);

        DonationRowService service2 = new DonationRowService();
        service2.loadRows(tempDir);
        List<DonationRow> list = service2.listRows();
        assertEquals(1, list.size());
        assertEquals("Org", list.get(0).getOrganizationName());
    }
}

