package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.persistence.DonationRecordRepository;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.io.IOException;
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
	/** Edit policy for posted donations. */
	public enum DonationEditPostingPolicy
	{
		UPDATE_IN_PLACE,
		REVERSE_AND_REPOST
	}

	private static final String LINK_ORIGINAL = "ORIGINAL";
	private static final String LINK_REVERSAL = "REVERSAL";
	private static final String LINK_ADJUSTMENT = "ADJUSTMENT";

	private final DonationRecordRepository donationRecordRepository;
	private final JournalRepository journalRepository;
	private final PostingFacade postingFacade;
	private final DonationEditPostingPolicy editPostingPolicy;

	public DonationPostingService()
	{
		this(new DonationRecordRepository(), new JournalRepository(),
			new DefaultPostingFacade(),
			resolvePolicyFromSettings());
	}

	DonationPostingService(DonationRecordRepository donationRecordRepository,
		JournalRepository journalRepository)
	{
		this(donationRecordRepository, journalRepository,
			new DefaultPostingFacade(journalRepository,
				new NoOpPostingDatePolicyValidator(),
				new NoOpAccountFundRestrictionValidator(),
				new NoOpPostingLockValidator()),
				DonationEditPostingPolicy.UPDATE_IN_PLACE);
	}

	DonationPostingService(DonationRecordRepository donationRecordRepository,
		JournalRepository journalRepository,
		DonationEditPostingPolicy editPostingPolicy)
	{
		this(donationRecordRepository, journalRepository,
			new DefaultPostingFacade(journalRepository,
				new NoOpPostingDatePolicyValidator(),
				new NoOpAccountFundRestrictionValidator(),
				new NoOpPostingLockValidator()),
			editPostingPolicy);
	}

	DonationPostingService(DonationRecordRepository donationRecordRepository,
		JournalRepository journalRepository,
		PostingFacade postingFacade,
		DonationEditPostingPolicy editPostingPolicy)
	{
		this.donationRecordRepository = donationRecordRepository;
		this.journalRepository = journalRepository;
		this.postingFacade = postingFacade;
		this.editPostingPolicy = editPostingPolicy == null ?
			DonationEditPostingPolicy.UPDATE_IN_PLACE : editPostingPolicy;
	}

	/**
	 * Posts and upserts a donation.
	 */
	public DonationRecord postDonation(DonationRecord donation) throws SQLException
	{
		requireValid(donation);
		Optional<DonationRecord> existing = this.donationRecordRepository
			.findByDonationId(donation.getDonationId());
		if (shouldReverseAndRepost(existing, donation))
		{
			return reverseAndRepost(existing.orElseThrow(), donation);
		}
		int journalTxnId = resolveJournalTxnId(donation, existing);
		AccountingTransaction txn = toTransaction(donation, journalTxnId,
			LINK_ORIGINAL);
		this.postingFacade.post(toCommand(txn, donation, LINK_ORIGINAL));
		donation.setJournalTxnId(journalTxnId);
		this.donationRecordRepository.upsert(donation);
		upsertDonationJournalLink(donation.getDonationId(), journalTxnId,
			LINK_ORIGINAL);
		return donation;
	}

	public Optional<DonationRecord> findDonationByJournalTxnId(int journalTxnId)
		throws SQLException
	{
		return this.donationRecordRepository.findByJournalTxnId(journalTxnId);
	}

	private DonationRecord reverseAndRepost(DonationRecord existing,
		DonationRecord updated) throws SQLException
	{
		int reversalTxnId = nextJournalTxnId();
		AccountingTransaction reversalTxn = toReverseTransaction(existing, reversalTxnId);
		this.postingFacade.post(toCommand(reversalTxn, existing, LINK_REVERSAL));
		upsertDonationJournalLink(existing.getDonationId(), reversalTxnId,
			LINK_REVERSAL);

		int adjustedTxnId = nextJournalTxnId();
		AccountingTransaction adjustedTxn = toTransaction(updated,
			adjustedTxnId, LINK_ADJUSTMENT);
		this.postingFacade.post(toCommand(adjustedTxn, updated, LINK_ADJUSTMENT));
		updated.setJournalTxnId(adjustedTxnId);
		this.donationRecordRepository.upsert(updated);
		upsertDonationJournalLink(updated.getDonationId(), adjustedTxnId,
			LINK_ADJUSTMENT);
		return updated;
	}

	private PostingCommand toCommand(AccountingTransaction txn,
		DonationRecord donation, String linkRole)
	{
		return new PostingCommand(txn, "DONATION", donation.getDonationId(),
			linkRole, "DONATION:" + donation.getDonationId());
	}

	private boolean shouldReverseAndRepost(Optional<DonationRecord> existing,
		DonationRecord updated)
	{
		if (this.editPostingPolicy !=
			DonationEditPostingPolicy.REVERSE_AND_REPOST)
		{
			return false;
		}
		if (existing.isEmpty() || existing.get().getJournalTxnId() == null)
		{
			return false;
		}
		return donationChanged(existing.get(), updated);
	}

	private static boolean donationChanged(DonationRecord a, DonationRecord b)
	{
		if (a == null || b == null)
		{
			return true;
		}
		return !Objects.equals(a.getDonationDate(), b.getDonationDate()) ||
			!Objects.equals(a.getAmount(), b.getAmount()) ||
			!Objects.equals(a.getMemo(), b.getMemo()) ||
			!Objects.equals(a.getCashAccountNumber(), b.getCashAccountNumber()) ||
			!Objects.equals(a.getRevenueAccountNumber(),
				b.getRevenueAccountNumber()) ||
			!Objects.equals(a.getFundNumber(), b.getFundNumber()) ||
			!Objects.equals(a.getDonorExternalId(), b.getDonorExternalId());
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
		if (donation.getAmount() == null ||
			donation.getAmount().compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("amount must be > 0");
		}
	}

	private static boolean isBlank(String s)
	{
		return s == null || s.isBlank();
	}

	private int resolveJournalTxnId(DonationRecord donation,
		Optional<DonationRecord> existing) throws SQLException
	{
		if (donation.getJournalTxnId() != null)
		{
			return donation.getJournalTxnId();
		}
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

	private AccountingTransaction toReverseTransaction(DonationRecord donation,
		int journalTxnId)
	{
		AccountingTransaction txn = toTransaction(donation, journalTxnId,
			LINK_REVERSAL);
		LinkedHashSet<AccountingEntry> reversed = new LinkedHashSet<>();
		AccountingEntry creditCash = new AccountingEntry(donation.getAmount(),
			donation.getCashAccountNumber(), AccountSide.CREDIT);
		creditCash.setFundNumber(donation.getFundNumber());
		reversed.add(creditCash);
		AccountingEntry debitRevenue = new AccountingEntry(donation.getAmount(),
			donation.getRevenueAccountNumber(), AccountSide.DEBIT);
		debitRevenue.setFundNumber(donation.getFundNumber());
		reversed.add(debitRevenue);
		txn.setEntries(reversed);
		txn.setMemo("Reversal of donation " + donation.getDonationId());
		return txn;
	}

	private AccountingTransaction toTransaction(DonationRecord donation,
		int journalTxnId, String linkRole)
	{
		AccountingTransaction txn = new AccountingTransaction();
		txn.setId(journalTxnId);
		LocalDate date = donation.getDonationDate() == null ? LocalDate.now() :
			donation.getDonationDate();
		txn.setDate(date.toString());
		txn.setBookingDateTimestamp(System.currentTimeMillis());
		txn.setMemo(buildMemo(donation));
		txn.setToFrom(donation.getDonorExternalId());
		txn.setInfo(Map.of(
			"module", "DONATION",
			"domain_record_id", donation.getDonationId(),
			"link_role", linkRole,
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

	private static DonationEditPostingPolicy resolvePolicyFromSettings()
	{
		SettingsService settingsService = new SettingsService();
		try
		{
			settingsService.loadSettings(null);
			SettingsModel settings = settingsService.getSettings();
			String value = settings == null ? null :
				settings.getDonationEditPostingPolicy();
			if ("REVERSE_AND_REPOST".equalsIgnoreCase(value))
			{
				return DonationEditPostingPolicy.REVERSE_AND_REPOST;
			}
		}
		catch (IOException ignored)
		{
			// Fall back to legacy behavior.
		}
		return DonationEditPostingPolicy.UPDATE_IN_PLACE;
	}

	private static void upsertDonationJournalLink(String donationId,
		int journalTxnId, String linkRole) throws SQLException
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
