package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.BankingTransactionRecord;
import nonprofitbookkeeping.model.BankStatementRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BankingTransactionRepositoryTest
{
	@TempDir
	Path tempDir;

	@Test
	void upsertFindOpenAndMarkReconciled_usesNewMatchingStructure()
		throws Exception
	{
		Path dbPath = tempDir.resolve("banking-transaction-repo");
		Database.init(dbPath);
		Database.get().ensureSchema();

		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "INSERT INTO bank_id_record(bank_id_record_id, bank_id, bank_name, account_id, account_type) VALUES (?,?,?,?,?)"))
		{
			ps.setString(1, "bank-001");
			ps.setString(2, "ext-bank");
			ps.setString(3, "First Bank");
			ps.setString(4, "operating-001");
			ps.setString(5, "CHECKING");
			ps.executeUpdate();
		}

		BankStatementRecord stmt = new BankStatementRecord();
		stmt.setBankName("First Bank");
		stmt.setAccountLabel("Operating");
		stmt.setStatementDate(LocalDate.of(2026, 3, 31));
		stmt.setBankIdRecordId("bank-001");
		stmt.setStatus("OPEN");
		BankStatementRepository.upsert(stmt);

		BankingTransactionRecord row = new BankingTransactionRecord();
		row.setBankingRecordId("btx-001");
		row.setBankIdRecordId("bank-001");
		row.setTransactionDate(LocalDate.of(2026, 3, 30));
		row.setExternalTransactionId("ext-001");
		row.setSourceFingerprint("fp-001");
		row.setNormalizedDescription("donation received");
		row.setAmount(new BigDecimal("125.00"));
		row.setMatchStatus("UNMATCHED");
		BankingTransactionRepository.upsert(row);

		List<BankingTransactionRecord> open =
			BankingTransactionRepository.findOpenByBankId("bank-001");
		assertEquals(1, open.size());
		assertEquals("UNMATCHED", open.get(0).getMatchStatus());
		assertEquals("fp-001", open.get(0).getSourceFingerprint());

		int updated = BankingTransactionRepository.markReconciled("btx-001");
		assertEquals(1, updated);

		List<BankingTransactionRecord> after =
			BankingTransactionRepository.findOpenByBankId("bank-001");
		assertEquals(0, after.size());

		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(
				 "SELECT match_status, matched_at FROM banking_transaction_record WHERE banking_record_id = ?"))
		{
			ps.setString(1, "btx-001");
			try (var rs = ps.executeQuery())
			{
				assertEquals(true, rs.next());
				assertEquals("RECONCILED", rs.getString(1));
				assertNotNull(rs.getTimestamp(2));
			}
		}
	}
}
