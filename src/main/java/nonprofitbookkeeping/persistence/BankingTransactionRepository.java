package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.BankingTransactionRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class BankingTransactionRepository
{
	private BankingTransactionRepository()
	{
	}

	public static void upsert(BankingTransactionRecord row) throws SQLException
	{
		String sql = """
			MERGE INTO banking_transaction_record(
				banking_record_id, bank_id_record_id, statement_id, journal_txn_id,
				fund_id, transaction_date, external_transaction_id,
				source_fingerprint, normalized_description, amount, match_status,
				matched_at, anomaly_duplicate, anomaly_amount_outlier, anomaly_date_outlier
			) KEY(banking_record_id)
			VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			int i = 0;
			ps.setString(++i, row.getBankingRecordId());
			ps.setString(++i, row.getBankIdRecordId());
			if (row.getStatementId() == null)
			{
				ps.setNull(++i, java.sql.Types.BIGINT);
			}
			else
			{
				ps.setLong(++i, row.getStatementId());
			}
			if (row.getJournalTxnId() == null)
			{
				ps.setNull(++i, java.sql.Types.INTEGER);
			}
			else
			{
				ps.setInt(++i, row.getJournalTxnId());
			}
			if (row.getFundId() == null)
			{
				ps.setNull(++i, java.sql.Types.BIGINT);
			}
			else
			{
				ps.setLong(++i, row.getFundId());
			}
			ps.setDate(++i,
				row.getTransactionDate() == null ? null : Date.valueOf(row.getTransactionDate()));
			ps.setString(++i, row.getExternalTransactionId());
			ps.setString(++i, row.getSourceFingerprint());
			ps.setString(++i, row.getNormalizedDescription());
			ps.setBigDecimal(++i,
				row.getAmount() == null ? BigDecimal.ZERO : row.getAmount());
			ps.setString(++i, row.getMatchStatus() == null ? "NEW" : row.getMatchStatus());
			ps.setTimestamp(++i,
				row.getMatchedAt() == null ? null : Timestamp.valueOf(row.getMatchedAt()));
			ps.setBoolean(++i, row.isAnomalyDuplicate());
			ps.setBoolean(++i, row.isAnomalyAmountOutlier());
			ps.setBoolean(++i, row.isAnomalyDateOutlier());
			ps.executeUpdate();
		}
	}

	public static List<BankingTransactionRecord> findOpenByBankId(
		String bankIdRecordId) throws SQLException
	{
		String sql = """
			SELECT * FROM banking_transaction_record
			WHERE bank_id_record_id = ?
			  AND match_status IN ('NEW','UNMATCHED','AUTO_MATCHED','STALE_UNMATCHED')
			ORDER BY transaction_date, banking_record_id
		""";
		List<BankingTransactionRecord> rows = new ArrayList<>();
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, bankIdRecordId);
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

	public static int markReconciled(String bankingRecordId) throws SQLException
	{
		return transitionMatchStatus(bankingRecordId, "RECONCILED");
	}

	public static int transitionMatchStatus(String bankingRecordId,
		String matchStatus) throws SQLException
	{
		String sql = """
			UPDATE banking_transaction_record
			   SET match_status = ?,
			       matched_at = CURRENT_TIMESTAMP
			 WHERE banking_record_id = ?
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, matchStatus);
			ps.setString(2, bankingRecordId);
			return ps.executeUpdate();
		}
	}

	private static BankingTransactionRecord mapRow(ResultSet rs) throws SQLException
	{
		BankingTransactionRecord r = new BankingTransactionRecord();
		r.setBankingRecordId(rs.getString("banking_record_id"));
		r.setBankIdRecordId(rs.getString("bank_id_record_id"));
		long statementId = rs.getLong("statement_id");
		r.setStatementId(rs.wasNull() ? null : statementId);
		int journalTxnId = rs.getInt("journal_txn_id");
		r.setJournalTxnId(rs.wasNull() ? null : journalTxnId);
		long fundId = rs.getLong("fund_id");
		r.setFundId(rs.wasNull() ? null : fundId);
		Date txnDate = rs.getDate("transaction_date");
		r.setTransactionDate(txnDate == null ? null : txnDate.toLocalDate());
		r.setExternalTransactionId(rs.getString("external_transaction_id"));
		r.setSourceFingerprint(rs.getString("source_fingerprint"));
		r.setNormalizedDescription(rs.getString("normalized_description"));
		r.setAmount(rs.getBigDecimal("amount"));
		r.setMatchStatus(rs.getString("match_status"));
		Timestamp matchedAt = rs.getTimestamp("matched_at");
		r.setMatchedAt(matchedAt == null ? null : matchedAt.toLocalDateTime());
		r.setAnomalyDuplicate(rs.getBoolean("anomaly_duplicate"));
		r.setAnomalyAmountOutlier(rs.getBoolean("anomaly_amount_outlier"));
		r.setAnomalyDateOutlier(rs.getBoolean("anomaly_date_outlier"));
		return r;
	}
}
