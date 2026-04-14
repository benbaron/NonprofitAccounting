package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
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
}
