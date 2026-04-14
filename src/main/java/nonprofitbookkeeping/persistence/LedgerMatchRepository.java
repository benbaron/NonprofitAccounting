package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.LedgerMatchRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class LedgerMatchRepository
{
	private LedgerMatchRepository()
	{
	}

	public static void upsert(LedgerMatchRecord row) throws SQLException
	{
		String sql = """
			MERGE INTO ledger_record(
				ledger_record_id, ledger_id, journal_entry_id, bank_id_record_id,
				banking_record_id, match_group_id, match_method, reviewer_user,
				reviewed_at, link_status
			) KEY(ledger_record_id)
			VALUES(?,?,?,?,?,?,?,?,?,?)
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			int i = 0;
			ps.setString(++i, row.getLedgerRecordId());
			ps.setString(++i,
				row.getLedgerId() == null ? "PRIMARY_LEDGER" : row.getLedgerId());
			if (row.getJournalEntryId() == null)
			{
				ps.setNull(++i, java.sql.Types.BIGINT);
			}
			else
			{
				ps.setLong(++i, row.getJournalEntryId());
			}
			ps.setString(++i, row.getBankIdRecordId());
			ps.setString(++i, row.getBankingRecordId());
			ps.setString(++i, row.getMatchGroupId());
			ps.setString(++i, row.getMatchMethod());
			ps.setString(++i, row.getReviewerUser());
			ps.setTimestamp(++i,
				row.getReviewedAt() == null ? null : Timestamp.valueOf(row.getReviewedAt()));
			ps.setString(++i, row.getLinkStatus() == null ? "ACTIVE" : row.getLinkStatus());
			ps.executeUpdate();
		}
	}

	public static int setLinkStatus(String ledgerRecordId, String linkStatus)
		throws SQLException
	{
		String sql = """
			UPDATE ledger_record
			   SET link_status = ?,
			       reviewed_at = CURRENT_TIMESTAMP
			 WHERE ledger_record_id = ?
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, linkStatus);
			ps.setString(2, ledgerRecordId);
			return ps.executeUpdate();
		}
	}

	public static List<LedgerMatchRecord> findActiveByBankingRecordId(
		String bankingRecordId) throws SQLException
	{
		String sql = """
			SELECT * FROM ledger_record
			WHERE banking_record_id = ?
			  AND link_status = 'ACTIVE'
			ORDER BY created_at
		""";
		List<LedgerMatchRecord> rows = new ArrayList<>();
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, bankingRecordId);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					rows.add(mapRow(rs));
				}
			}
		}
		return rows;
	}

	private static LedgerMatchRecord mapRow(ResultSet rs) throws SQLException
	{
		LedgerMatchRecord r = new LedgerMatchRecord();
		r.setLedgerRecordId(rs.getString("ledger_record_id"));
		r.setLedgerId(rs.getString("ledger_id"));
		long journalEntryId = rs.getLong("journal_entry_id");
		r.setJournalEntryId(rs.wasNull() ? null : journalEntryId);
		r.setBankIdRecordId(rs.getString("bank_id_record_id"));
		r.setBankingRecordId(rs.getString("banking_record_id"));
		r.setMatchGroupId(rs.getString("match_group_id"));
		r.setMatchMethod(rs.getString("match_method"));
		r.setReviewerUser(rs.getString("reviewer_user"));
		Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
		r.setReviewedAt(reviewedAt == null ? null : reviewedAt.toLocalDateTime());
		r.setLinkStatus(rs.getString("link_status"));
		return r;
	}
}
