package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.DonationRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DonationPostingServiceTest
{
	@TempDir
	Path tempDir;

	@Test
	void postDonation_persistsJournalAndBidirectionalLinkage() throws Exception
	{
		Path dbPath = this.tempDir.resolve("donation-posting");
		Database.init(dbPath);
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		Database.get().ensureSchema();
		seedAccounts();
		seedDonor();

		DonationPostingService service = new DonationPostingService(
			new nonprofitbookkeeping.persistence.DonationRecordRepository(),
			new nonprofitbookkeeping.persistence.JournalRepository(),
			DonationPostingService.DonationEditPostingPolicy.UPDATE_IN_PLACE);
		DonationRecord donation = new DonationRecord();
		donation.setDonationId("don-001");
		donation.setDonorExternalId("d-001");
		donation.setDonationDate(LocalDate.of(2026, 4, 22));
		donation.setAmount(new BigDecimal("250.00"));
		donation.setMemo("Spring campaign gift");
		donation.setCashAccountNumber("1000");
		donation.setRevenueAccountNumber("4000");
		donation.setFundNumber("FUND-1");

		DonationRecord posted = service.postDonation(donation);

		assertTrue(posted.getJournalTxnId() != null);
		assertEquals("don-001", service.findDonationByJournalTxnId(
			posted.getJournalTxnId()).orElseThrow().getDonationId());
		assertEquals("DONATION", readTxnInfo(posted.getJournalTxnId(), "module"));
		assertEquals("don-001", readTxnInfo(posted.getJournalTxnId(), "domain_record_id"));
		assertEquals(2, readJournalEntryCount(posted.getJournalTxnId()));
		assertEquals(1, readDonationLinkCount("don-001", posted.getJournalTxnId()));
	}

	@Test
	void editDonation_reverseAndRepost_policyCreatesReversalAndAdjustment()
		throws Exception
	{
		Path dbPath = this.tempDir.resolve("donation-reverse-policy");
		Database.init(dbPath);
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		Database.get().ensureSchema();
		seedAccounts();
		seedDonor();

		DonationPostingService service = new DonationPostingService(
			new nonprofitbookkeeping.persistence.DonationRecordRepository(),
			new nonprofitbookkeeping.persistence.JournalRepository(),
			DonationPostingService.DonationEditPostingPolicy.REVERSE_AND_REPOST);

		DonationRecord first = new DonationRecord();
		first.setDonationId("don-002");
		first.setDonorExternalId("d-001");
		first.setDonationDate(LocalDate.of(2026, 4, 20));
		first.setAmount(new BigDecimal("120.00"));
		first.setMemo("Initial gift");
		first.setCashAccountNumber("1000");
		first.setRevenueAccountNumber("4000");
		first.setFundNumber("FUND-1");
		DonationRecord original = service.postDonation(first);

		DonationRecord edited = new DonationRecord();
		edited.setDonationId("don-002");
		edited.setDonorExternalId("d-001");
		edited.setDonationDate(LocalDate.of(2026, 4, 21));
		edited.setAmount(new BigDecimal("175.00"));
		edited.setMemo("Adjusted gift");
		edited.setCashAccountNumber("1000");
		edited.setRevenueAccountNumber("4000");
		edited.setFundNumber("FUND-1");
		DonationRecord adjusted = service.postDonation(edited);

		assertTrue(adjusted.getJournalTxnId() != null);
		assertTrue(adjusted.getJournalTxnId() > original.getJournalTxnId());
		assertEquals(3, readDonationLinksByRole("don-002", "ORIGINAL")
			+ readDonationLinksByRole("don-002", "REVERSAL")
			+ readDonationLinksByRole("don-002", "ADJUSTMENT"));
		assertEquals(1, readDonationLinksByRole("don-002", "ORIGINAL"));
		assertEquals(1, readDonationLinksByRole("don-002", "REVERSAL"));
		assertEquals(1, readDonationLinksByRole("don-002", "ADJUSTMENT"));
		assertEquals("ADJUSTMENT",
			readTxnInfo(adjusted.getJournalTxnId(), "link_role"));
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

	private void seedDonor() throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "MERGE INTO donor(external_id, name, email, phone) KEY(external_id) VALUES (?,?,?,?)"))
		{
			ps.setString(1, "d-001");
			ps.setString(2, "Donor One");
			ps.setString(3, "donor1@example.org");
			ps.setString(4, "555-111-0001");
			ps.executeUpdate();
		}
	}

	private String readTxnInfo(int txnId, String key) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT v FROM transaction_info WHERE txn_id = ? AND k = ?"))
		{
			ps.setInt(1, txnId);
			ps.setString(2, key);
			try (ResultSet rs = ps.executeQuery())
			{
				assertTrue(rs.next());
				return rs.getString(1);
			}
		}
	}

	private int readJournalEntryCount(int txnId) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT COUNT(*) FROM journal_entry WHERE txn_id = ?"))
		{
			ps.setInt(1, txnId);
			try (ResultSet rs = ps.executeQuery())
			{
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private int readDonationLinkCount(String donationId, int journalTxnId)
		throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT COUNT(*) FROM donation_journal_link WHERE donation_id = ? AND journal_txn_id = ?"))
		{
			ps.setString(1, donationId);
			ps.setInt(2, journalTxnId);
			try (ResultSet rs = ps.executeQuery())
			{
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private int readDonationLinksByRole(String donationId, String linkRole)
		throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT COUNT(*) FROM donation_journal_link WHERE donation_id = ? AND link_role = ?"))
		{
			ps.setString(1, donationId);
			ps.setString(2, linkRole);
			try (ResultSet rs = ps.executeQuery())
			{
				rs.next();
				return rs.getInt(1);
			}
		}
	}
}
