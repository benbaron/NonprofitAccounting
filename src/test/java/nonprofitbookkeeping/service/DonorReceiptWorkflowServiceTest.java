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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DonorReceiptWorkflowServiceTest
{
    @TempDir
    Path tempDir;

    @AfterEach
    void closeDatabase()
    {
        Database.close();
    }

    @Test
    void donationQueryOnlyReturnsRecordsLinkedToPostedJournalTransactions() throws Exception
    {
        openDatabase();
        seedAccounts();
        DonorService donors = new DonorService();
        donors.addDonor(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100"));
        DonationRecord posted = donation("gift-posted", "donor-1", "25.00", LocalDate.of(2026, 1, 5));
        new DonationPostingService().postDonation(posted);
        DonationRecord metadataOnly = donation("gift-metadata", "donor-1", "50.00", LocalDate.of(2026, 1, 6));
        new DonationRecordRepository().upsert(metadataOnly);

        DonorReceiptWorkflowService.DonorDetail detail = new DonorReceiptWorkflowService()
            .detailForDonor("donor-1");

        assertEquals(1, detail.donationHistory().size());
        assertEquals("gift-posted", detail.donationHistory().get(0).getDonationId());
    }

    @Test
    void receiptStatusUpdatesPersistOnDonationRecord() throws Exception
    {
        openDatabase();
        seedAccounts();
        new DonorService().addDonor(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100"));
        DonationRecord posted = new DonationPostingService()
            .postDonation(donation("gift-1", "donor-1", "25.00", LocalDate.of(2026, 1, 5)));
        DonorReceiptWorkflowService service = new DonorReceiptWorkflowService();

        service.updateReceiptRequired(posted.getDonationId(), false);
        DonationRecord noReceipt = new DonationRecordRepository()
            .findByDonationId(posted.getDonationId()).orElseThrow();
        assertFalse(noReceipt.isReceiptRequired());

        LocalDateTime sentAt = LocalDateTime.of(2026, 2, 1, 9, 30);
        service.markReceiptSent(posted.getDonationId(), sentAt);
        DonationRecord sent = new DonationRecordRepository()
            .findByDonationId(posted.getDonationId()).orElseThrow();
        assertTrue(sent.isReceiptRequired());
        assertEquals(sentAt, sent.getReceiptSentAt());
    }

    @Test
    void annualDonorSummaryTotalsPostedDonationsByYear() throws Exception
    {
        openDatabase();
        seedAccounts();
        new DonorService().addDonor(new DonorContact("donor-1", "Pat Donor", "pat@example.org", "555-0100"));
        DonationPostingService posting = new DonationPostingService();
        posting.postDonation(donation("gift-2025", "donor-1", "15.50", LocalDate.of(2025, 12, 31)));
        posting.postDonation(donation("gift-2026-a", "donor-1", "20.00", LocalDate.of(2026, 1, 5)));
        posting.postDonation(donation("gift-2026-b", "donor-1", "30.25", LocalDate.of(2026, 6, 5)));

        DonorReceiptWorkflowService.DonorDetail detail = new DonorReceiptWorkflowService()
            .detailForDonor("donor-1");

        assertEquals(new BigDecimal("15.50"), detail.annualTotals().get(Year.of(2025)));
        assertEquals(new BigDecimal("50.25"), detail.annualTotals().get(Year.of(2026)));
    }

    private void openDatabase() throws Exception
    {
        Database.init(this.tempDir.resolve("donor-receipts"));
        Database.get().ensureSchema();
    }

    private static DonationRecord donation(String id, String donorId, String amount, LocalDate date)
    {
        DonationRecord donation = new DonationRecord();
        donation.setDonationId(id);
        donation.setDonorExternalId(donorId);
        donation.setDonationDate(date);
        donation.setAmount(new BigDecimal(amount));
        donation.setMemo("Donation " + id);
        donation.setCashAccountNumber("1000");
        donation.setRevenueAccountNumber("4000");
        return donation;
    }

    private void seedAccounts() throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "MERGE INTO account(account_number, name, account_type, increase_side) KEY(account_number) VALUES (?,?,?,?)"))
        {
            ps.setString(1, "1000");
            ps.setString(2, "Cash");
            ps.setString(3, "ASSET");
            ps.setString(4, "DEBIT");
            ps.addBatch();
            ps.setString(1, "4000");
            ps.setString(2, "Donations Revenue");
            ps.setString(3, "INCOME");
            ps.setString(4, "CREDIT");
            ps.addBatch();
            ps.executeBatch();
        }
    }
}
