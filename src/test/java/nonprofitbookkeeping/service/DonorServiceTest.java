package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.persistence.DonationRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DonorServiceTest
{
    @TempDir
    Path tempDir;

    @AfterEach
    void closeDatabase()
    {
        Database.close();
    }

    @Test
    void createsEditsListsAndDeactivatesDonors() throws Exception
    {
        openDatabase();
        DonorService service = new DonorService();

        service.addDonor(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100"));
        assertEquals(List.of(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100")),
            service.getAllDonors());

        assertTrue(service.editDonor("donor-1",
            new DonorContact("donor-1", "Pat Updated", "updated@example.org", "555-0101")));
        assertEquals("Pat Updated", service.getAllDonors().get(0).getName());
        assertEquals("updated@example.org", service.getAllDonors().get(0).getEmail());

        assertTrue(service.removeDonor("donor-1"));
        assertTrue(service.getAllDonors().isEmpty());
        assertFalse(service.removeDonor("missing"));
    }

    @Test
    void deactivationKeepsDonationHistoryLinkedToDonorExternalId() throws Exception
    {
        openDatabase();
        DonorService service = new DonorService();
        DonationRecordRepository donations = new DonationRecordRepository();
        service.addDonor(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100"));
        DonationRecord donation = new DonationRecord();
        donation.setDonationId("gift-1");
        donation.setDonorExternalId("donor-1");
        donation.setDonationDate(LocalDate.of(2026, 6, 1));
        donation.setAmount(new BigDecimal("25.00"));
        donation.setMemo("Pledge");
        donation.setCashAccountNumber("1000");
        donation.setRevenueAccountNumber("4000");
        donations.upsert(donation);

        service.removeDonor("donor-1");

        assertTrue(service.getAllDonors().isEmpty());
        assertEquals(1, donations.listByDonorExternalId("donor-1").size());
        assertEquals("gift-1", donations.listByDonorExternalId("donor-1").get(0).getDonationId());
    }

    @Test
    void saveDonorsDeactivatesMissingRowsWithoutBreakingDonationHistory() throws Exception
    {
        openDatabase();
        DonorService service = new DonorService();
        DonationRecordRepository donations = new DonationRecordRepository();
        service.addDonor(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100"));
        DonationRecord donation = new DonationRecord();
        donation.setDonationId("gift-1");
        donation.setDonorExternalId("donor-1");
        donation.setDonationDate(LocalDate.of(2026, 6, 1));
        donation.setAmount(new BigDecimal("25.00"));
        donation.setMemo("Pledge");
        donation.setCashAccountNumber("1000");
        donation.setRevenueAccountNumber("4000");
        donations.upsert(donation);

        service.removeDonor("donor-1");
        service.saveDonors(null);

        assertTrue(service.getAllDonors().isEmpty());
        assertEquals("donor-1", donations.listByDonorExternalId("donor-1").get(0).getDonorExternalId());
    }

    private void openDatabase() throws Exception
    {
        Database.init(tempDir.resolve("donors"));
        Database.get().ensureSchema();
    }
}
