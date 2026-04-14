package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.BankStatementRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BankStatementRepositoryTest
{
	@TempDir
	Path tempDir;

	@Test
	void upsertAndFind_roundTripsNewReconciliationFields() throws Exception
	{
		Path dbPath = tempDir.resolve("bank-statement-repo");
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

		BankStatementRecord row = new BankStatementRecord();
		row.setBankName("First Bank");
		row.setAccountLabel("Operating");
		row.setStatementDate(LocalDate.of(2026, 3, 31));
		row.setStatementBalance(new BigDecimal("1000.00"));
		row.setLedgerBalance(new BigDecimal("950.00"));
		row.setOutstanding(new BigDecimal("50.00"));
		row.setBankAfterOutstanding(new BigDecimal("950.00"));
		row.setDifference(BigDecimal.ZERO);
		row.setLedgerStatus("Balanced");
		row.setBankIdRecordId("bank-001");
		row.setPeriodStart(LocalDate.of(2026, 3, 1));
		row.setPeriodEnd(LocalDate.of(2026, 3, 31));
		row.setStatus("CLOSED");
		row.setImportedAt(LocalDateTime.of(2026, 4, 1, 8, 30, 0));
		row.setClosedAt(LocalDateTime.of(2026, 4, 2, 9, 15, 0));
		row.setRetentionUntil(LocalDate.of(2033, 3, 31));

		BankStatementRepository.upsert(row);

		List<BankStatementRecord> rows = BankStatementRepository.findByBankAndYear("First Bank", 2026);
		assertEquals(1, rows.size());
		BankStatementRecord found = rows.get(0);
		assertEquals("bank-001", found.getBankIdRecordId());
		assertEquals(LocalDate.of(2026, 3, 1), found.getPeriodStart());
		assertEquals(LocalDate.of(2026, 3, 31), found.getPeriodEnd());
		assertEquals("CLOSED", found.getStatus());
		assertNotNull(found.getImportedAt());
		assertNotNull(found.getClosedAt());
		assertEquals(LocalDate.of(2033, 3, 31), found.getRetentionUntil());
	}
}
