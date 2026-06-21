package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.persistence.DonationRecordRepository;
import nonprofitbookkeeping.persistence.DonorRepository;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service-backed donor receipt workflow queries and status changes.
 */
public class DonorReceiptWorkflowService
{
    private final DonorRepository donorRepository;
    private final DonationRecordRepository donationRepository;
    private final JournalRepository journalRepository;

    public DonorReceiptWorkflowService()
    {
        this(new DonorRepository(), new DonationRecordRepository(), new JournalRepository());
    }

    DonorReceiptWorkflowService(DonorRepository donorRepository,
        DonationRecordRepository donationRepository, JournalRepository journalRepository)
    {
        this.donorRepository = Objects.requireNonNull(donorRepository, "donorRepository");
        this.donationRepository = Objects.requireNonNull(donationRepository, "donationRepository");
        this.journalRepository = Objects.requireNonNull(journalRepository, "journalRepository");
    }

    public DonorDetail detailForDonor(String donorExternalId) throws SQLException
    {
        Optional<DonorContact> donor = this.donorRepository.findByExternalId(donorExternalId);
        List<DonationRecord> donations = postedDonations(
            this.donationRepository.listByDonorExternalId(donorExternalId));
        return new DonorDetail(donorExternalId, donor.orElse(null), donations,
            annualTotals(donations));
    }

    public List<DonationRecord> donationsNeedingReceipts() throws SQLException
    {
        return postedDonations(this.donationRepository.listAll()).stream()
            .filter(d -> d.isReceiptRequired() && d.getReceiptSentAt() == null)
            .toList();
    }

    public DonationRecord updateReceiptRequired(String donationId, boolean required)
        throws SQLException
    {
        DonationRecord donation = this.donationRepository.findByDonationId(donationId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown donation: " + donationId));
        donation.setReceiptRequired(required);
        if (!required)
        {
            donation.setReceiptSentAt(null);
        }
        this.donationRepository.upsert(donation);
        return donation;
    }

    public DonationRecord markReceiptSent(String donationId, LocalDateTime sentAt)
        throws SQLException
    {
        DonationRecord donation = this.donationRepository.findByDonationId(donationId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown donation: " + donationId));
        donation.setReceiptRequired(true);
        donation.setReceiptSentAt(sentAt == null ? LocalDateTime.now() : sentAt);
        this.donationRepository.upsert(donation);
        return donation;
    }

    private List<DonationRecord> postedDonations(List<DonationRecord> candidates)
        throws SQLException
    {
        List<DonationRecord> posted = new ArrayList<>();
        for (DonationRecord donation : candidates)
        {
            if (hasPostedDonationTransaction(donation))
            {
                posted.add(donation);
            }
        }
        return posted;
    }

    private boolean hasPostedDonationTransaction(DonationRecord donation)
        throws SQLException
    {
        return donation.getJournalTxnId() != null &&
            this.journalRepository.findTransactionById(donation.getJournalTxnId()).isPresent();
    }

    private static Map<Year, BigDecimal> annualTotals(List<DonationRecord> donations)
    {
        return donations.stream()
            .filter(d -> d.getDonationDate() != null)
            .collect(Collectors.groupingBy(d -> Year.of(d.getDonationDate().getYear()),
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO,
                    d -> d.getAmount() == null ? BigDecimal.ZERO : d.getAmount(),
                    BigDecimal::add)));
    }

    public record DonorDetail(String donorExternalId, DonorContact donor,
        List<DonationRecord> donationHistory, Map<Year, BigDecimal> annualTotals)
    {
    }
}
