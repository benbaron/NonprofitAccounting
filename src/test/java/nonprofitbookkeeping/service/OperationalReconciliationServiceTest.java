package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.BankingTransactionRecord;
import nonprofitbookkeeping.model.LedgerMatchRecord;
import nonprofitbookkeeping.persistence.BankingTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationalReconciliationServiceTest
{
	@TempDir
	Path tempDir;

	@Test
	void confirmVoidAndReconcile_updatesMatchAndLinkStatuses() throws Exception
	{
		Path dbPath = tempDir.resolve("operational-reconciliation-service");
		Database.init(dbPath);
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		Database.get().ensureSchema();
		seedBankId();

		BankingTransactionRecord btx = new BankingTransactionRecord();
		btx.setBankingRecordId("btx-ops-1");
		btx.setBankIdRecordId("bank-ops-1");
		btx.setTransactionDate(LocalDate.of(2026, 4, 1));
		btx.setAmount(new BigDecimal("200.00"));
		btx.setMatchStatus("UNMATCHED");
		BankingTransactionRepository.upsert(btx);

		OperationalReconciliationService service =
			new OperationalReconciliationService();

		LedgerMatchRecord link = new LedgerMatchRecord();
		link.setLedgerRecordId("lmr-1");
		link.setLedgerId("PRIMARY_LEDGER");
		link.setBankIdRecordId("bank-ops-1");
		link.setBankingRecordId("btx-ops-1");
		link.setMatchMethod("MANUAL");
		link.setReviewerUser("qa-user");
		link.setMatchGroupId("mg-1");

		service.confirmMatch(link);
		assertEquals("MATCH_CONFIRMED", readMatchStatus("btx-ops-1"));

		service.voidMatch("btx-ops-1", "lmr-1");
		assertEquals("UNMATCHED", readMatchStatus("btx-ops-1"));

		service.markReconciled("btx-ops-1");
		assertEquals("RECONCILED", readMatchStatus("btx-ops-1"));
	}

	@Test
	void reconcileFromBookingTimestamps_persistsStatementAndUpdatesRows()
		throws Exception
	{
		Path dbPath = tempDir.resolve("operational-reconciliation-booking");
		Database.init(dbPath);
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		Database.get().ensureSchema();
		seedBankId();
		seedJournalTxn(101, 555000111L);
		seedBankingTxn("btx-ops-2", "bank-ops-1", 101);

		OperationalReconciliationService service =
			new OperationalReconciliationService();
		int updated = service.reconcileFromBookingTimestamps("bank-ops-1",
			"2026-04-10", new BigDecimal("1200.00"), List.of(555000111L));

		assertEquals(1, updated);
		assertEquals("RECONCILED", readMatchStatus("btx-ops-2"));
		assertEquals("CLOSED", readStatementStatus("Ops Bank"));
		assertEquals("ops-account-1", readStatementAccountLabel("Ops Bank"));
	}

	@Test
	void postAdjustment_postsThroughFacade_and_linksBankTransaction() throws Exception
	{
		Path dbPath = tempDir.resolve("operational-reconciliation-adjustment");
		Database.init(dbPath);
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		Database.get().ensureSchema();
		seedBankId();
		seedAccounts();
		seedJournalTxn(101, 555000333L);
		seedBankingTxn("btx-ops-3", "bank-ops-1", 101);
		OperationalReconciliationService service = new OperationalReconciliationService();
		PostingCommand cmd = DepreciationPostingFactory.build("adj-btx-ops-3", new BigDecimal("12.34"), LocalDate.of(2026, 4, 15));
		PostingReference ref = service.postAdjustment("btx-ops-3", cmd);
		assertEquals("ADJUSTED", readMatchStatus("btx-ops-3"));
		assertEquals(ref.journalTxnId(), readJournalTxnId("btx-ops-3"));
		assertEquals(ref.canonicalTxnId(), readCanonicalTxnId("btx-ops-3"));
	}

	private void seedBankId() throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "INSERT INTO bank_id_record(bank_id_record_id, bank_id, bank_name, account_id, account_type) VALUES (?,?,?,?,?)"))
		{
			ps.setString(1, "bank-ops-1");
			ps.setString(2, "ext-bank-ops");
			ps.setString(3, "Ops Bank");
			ps.setString(4, "ops-account-1");
			ps.setString(5, "CHECKING");
			ps.executeUpdate();
		}
	}

	private String readMatchStatus(String bankingRecordId) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT match_status FROM banking_transaction_record WHERE banking_record_id = ?"))
		{
			ps.setString(1, bankingRecordId);
			try (var rs = ps.executeQuery())
			{
				assertEquals(true, rs.next());
				return rs.getString(1);
			}
		}
	}

	private void seedJournalTxn(int id, long bookingTimestamp) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "INSERT INTO journal_transaction(id, booking_ts, date_text, memo) VALUES (?,?,?,?)"))
		{
			ps.setInt(1, id);
			ps.setLong(2, bookingTimestamp);
			ps.setString(3, "2026-04-10");
			ps.setString(4, "match test");
			ps.executeUpdate();
		}
	}

	private void seedBankingTxn(String bankingRecordId, String bankIdRecordId,
		int journalTxnId) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement("""
				 INSERT INTO banking_transaction_record(
				     banking_record_id, bank_id_record_id, journal_txn_id,
				     transaction_date, amount, match_status
				 ) VALUES (?,?,?,?,?,?)
				 """))
		{
			ps.setString(1, bankingRecordId);
			ps.setString(2, bankIdRecordId);
			ps.setInt(3, journalTxnId);
			ps.setDate(4, java.sql.Date.valueOf(LocalDate.of(2026, 4, 10)));
			ps.setBigDecimal(5, new BigDecimal("1200.00"));
			ps.setString(6, "UNMATCHED");
			ps.executeUpdate();
		}
	}

	private int readJournalTxnId(String bankingRecordId) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT journal_txn_id FROM banking_transaction_record WHERE banking_record_id = ?"))
		{
			ps.setString(1, bankingRecordId);
			try (var rs = ps.executeQuery())
			{
				assertEquals(true, rs.next());
				return rs.getInt(1);
			}
		}
	}

	private Long readCanonicalTxnId(String bankingRecordId) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT canonical_txn_id FROM banking_transaction_record WHERE banking_record_id = ?"))
		{
			ps.setString(1, bankingRecordId);
			try (var rs = ps.executeQuery())
			{
				assertEquals(true, rs.next());
				long value = rs.getLong(1);
				return rs.wasNull() ? null : value;
			}
		}
	}

	private void seedAccounts() throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement("MERGE INTO account(account_number, name, account_code, account_type, supplemental_kinds, increase_side) KEY(account_number) VALUES (?,?,?,?,?,?)"))
		{
			ps.setString(1, "6100"); ps.setString(2, "Depreciation Expense"); ps.setString(3, "DEPRECIATION_EXPENSE"); ps.setString(4, "EXPENSE"); ps.setString(5, null); ps.setString(6, "DEBIT"); ps.addBatch();
			ps.setString(1, "1700"); ps.setString(2, "Accumulated Depreciation"); ps.setString(3, "ACCUMULATED_DEPRECIATION"); ps.setString(4, "ASSET"); ps.setString(5, "OTHER_ASSET"); ps.setString(6, "CREDIT"); ps.addBatch();
			ps.executeBatch();
		}
	}

	private String readStatementStatus(String bankName) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT status FROM bank_statement WHERE bank_name = ? ORDER BY statement_date DESC LIMIT 1"))
		{
			ps.setString(1, bankName);
			try (var rs = ps.executeQuery())
			{
				assertEquals(true, rs.next());
				return rs.getString(1);
			}
		}
	}

	private String readStatementAccountLabel(String bankName) throws Exception
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT account_label FROM bank_statement WHERE bank_name = ? ORDER BY statement_date DESC LIMIT 1"))
		{
			ps.setString(1, bankName);
			try (var rs = ps.executeQuery())
			{
				assertEquals(true, rs.next());
				return rs.getString(1);
			}
		}
	}
}
