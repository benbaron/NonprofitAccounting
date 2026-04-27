package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.persistence.DonationRecordRepository;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Phase-1 donation posting flow: write donation metadata + journal transaction link.
 */
public class DonationPostingService
{
	private final DonationRecordRepository donationRecordRepository;
	private final JournalRepository journalRepository;

	public DonationPostingService()
	{
		this(new DonationRecordRepository(), new JournalRepository());
	}

	DonationPostingService(DonationRecordRepository donationRecordRepository,
		JournalRepository journalRepository)
	{
		this.donationRecordRepository = donationRecordRepository;
		this.journalRepository = journalRepository;
	}

	/**
	 * Posts and upserts a donation. New donations allocate a journal transaction id;
	 * edited donations overwrite their existing linked transaction for phase-1 adoption.
	 */
	public DonationRecord postDonation(DonationRecord donation) throws SQLException
	{
		requireValid(donation);
		int journalTxnId = resolveJournalTxnId(donation);
		AccountingTransaction txn = toTransaction(donation, journalTxnId);
		this.journalRepository.upsertTransaction(txn);
		donation.setJournalTxnId(journalTxnId);
		this.donationRecordRepository.upsert(donation);
		upsertDonationJournalLink(donation.getDonationId(), journalTxnId, "ORIGINAL");
		return donation;
	}

	public Optional<DonationRecord> findDonationByJournalTxnId(int journalTxnId)
		throws SQLException
	{
		return this.donationRecordRepository.findByJournalTxnId(journalTxnId);
	}

	private void requireValid(DonationRecord donation)
	{
		Objects.requireNonNull(donation, "donation");
		if (isBlank(donation.getDonationId()))
		{
			throw new IllegalArgumentException("donationId is required");
		}
		if (isBlank(donation.getCashAccountNumber()))
		{
			throw new IllegalArgumentException("cashAccountNumber is required");
		}
		if (isBlank(donation.getRevenueAccountNumber()))
		{
			throw new IllegalArgumentException("revenueAccountNumber is required");
		}
		if (donation.getAmount() == null || donation.getAmount().compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("amount must be > 0");
		}
	}

	private static boolean isBlank(String s)
	{
		return s == null || s.isBlank();
	}

	private int resolveJournalTxnId(DonationRecord donation) throws SQLException
	{
		if (donation.getJournalTxnId() != null)
		{
			return donation.getJournalTxnId();
		}
		Optional<DonationRecord> existing =
			this.donationRecordRepository.findByDonationId(donation.getDonationId());
		if (existing.isPresent() && existing.get().getJournalTxnId() != null)
		{
			return existing.get().getJournalTxnId();
		}
		return nextJournalTxnId();
	}

	private static int nextJournalTxnId() throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT COALESCE(MAX(id), 0) + 1 FROM journal_transaction");
			 ResultSet rs = ps.executeQuery())
		{
			rs.next();
			return rs.getInt(1);
		}
	}

	private AccountingTransaction toTransaction(DonationRecord donation,
		int journalTxnId)
	{
		AccountingTransaction txn = new AccountingTransaction();
		txn.setId(journalTxnId);
		LocalDate date = donation.getDonationDate() == null ? LocalDate.now() : donation.getDonationDate();
		txn.setDate(date.toString());
		txn.setBookingDateTimestamp(System.currentTimeMillis());
		txn.setMemo(buildMemo(donation));
		txn.setToFrom(donation.getDonorExternalId());
		txn.setInfo(Map.of(
			"module", "DONATION",
			"domain_record_id", donation.getDonationId(),
			"link_role", "ORIGINAL",
			"idempotency_key", "DONATION:" + donation.getDonationId()));

		LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
		AccountingEntry debit = new AccountingEntry(donation.getAmount(),
			donation.getCashAccountNumber(), AccountSide.DEBIT);
		debit.setFundNumber(donation.getFundNumber());
		entries.add(debit);
		AccountingEntry credit = new AccountingEntry(donation.getAmount(),
			donation.getRevenueAccountNumber(), AccountSide.CREDIT);
		credit.setFundNumber(donation.getFundNumber());
		entries.add(credit);
		txn.setEntries(entries);
		return txn;
	}

	private String buildMemo(DonationRecord donation)
	{
		if (isBlank(donation.getMemo()))
		{
			return "Donation " + donation.getDonationId();
		}
		return donation.getMemo();
	}

	private static void upsertDonationJournalLink(String donationId, int journalTxnId,
		String linkRole) throws SQLException
	{
		String sql = """
			MERGE INTO donation_journal_link(donation_id, journal_txn_id, link_role, updated_at)
			KEY(donation_id, journal_txn_id, link_role)
			VALUES(?,?,?, CURRENT_TIMESTAMP)
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, donationId);
			ps.setInt(2, journalTxnId);
			ps.setString(3, linkRole);
			ps.executeUpdate();
		}
	}
}
