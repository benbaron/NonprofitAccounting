package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.DonationRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for donation workflow records.
 */
public class DonationRecordRepository
{
	public void upsert(DonationRecord row) throws SQLException
	{
		String sql = """
			MERGE INTO donation_record(
			  donation_id, donor_external_id, donation_date, amount, memo,
			  cash_account_number, revenue_account_number, fund_number, journal_txn_id, updated_at
			) KEY(donation_id)
			VALUES(?,?,?,?,?,?,?,?,?, CURRENT_TIMESTAMP)
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			int i = 0;
			ps.setString(++i, row.getDonationId());
			ps.setString(++i, row.getDonorExternalId());
			ps.setDate(++i,
				row.getDonationDate() == null ? null : Date.valueOf(row.getDonationDate()));
			ps.setBigDecimal(++i, row.getAmount());
			ps.setString(++i, row.getMemo());
			ps.setString(++i, row.getCashAccountNumber());
			ps.setString(++i, row.getRevenueAccountNumber());
			ps.setString(++i, row.getFundNumber());
			if (row.getJournalTxnId() == null)
			{
				ps.setNull(++i, java.sql.Types.INTEGER);
			}
			else
			{
				ps.setInt(++i, row.getJournalTxnId());
			}
			ps.executeUpdate();
		}
	}

	public Optional<DonationRecord> findByDonationId(String donationId)
		throws SQLException
	{
		String sql = "SELECT * FROM donation_record WHERE donation_id = ?";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, donationId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (!rs.next())
				{
					return Optional.empty();
				}
				return Optional.of(mapRow(rs));
			}
		}
	}

	public Optional<DonationRecord> findByJournalTxnId(int journalTxnId)
		throws SQLException
	{
		String sql = "SELECT * FROM donation_record WHERE journal_txn_id = ?";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setInt(1, journalTxnId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (!rs.next())
				{
					return Optional.empty();
				}
				return Optional.of(mapRow(rs));
			}
		}
	}

	public List<DonationRecord> listAll() throws SQLException
	{
		List<DonationRecord> rows = new ArrayList<>();
		String sql = "SELECT * FROM donation_record ORDER BY donation_date, donation_id";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				rows.add(mapRow(rs));
			}
		}
		return rows;
	}

	public List<DonationRecord> listByDonorExternalId(String donorExternalId)
		throws SQLException
	{
		List<DonationRecord> rows = new ArrayList<>();
		String sql = "SELECT * FROM donation_record WHERE donor_external_id = ? ORDER BY donation_date DESC, donation_id";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, donorExternalId);
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

	private static DonationRecord mapRow(ResultSet rs) throws SQLException
	{
		DonationRecord row = new DonationRecord();
		row.setDonationId(rs.getString("donation_id"));
		row.setDonorExternalId(rs.getString("donor_external_id"));
		Date d = rs.getDate("donation_date");
		row.setDonationDate(d == null ? null : d.toLocalDate());
		row.setAmount(rs.getBigDecimal("amount"));
		row.setMemo(rs.getString("memo"));
		row.setCashAccountNumber(rs.getString("cash_account_number"));
		row.setRevenueAccountNumber(rs.getString("revenue_account_number"));
		row.setFundNumber(rs.getString("fund_number"));
		int txnId = rs.getInt("journal_txn_id");
		row.setJournalTxnId(rs.wasNull() ? null : txnId);
		return row;
	}
}
