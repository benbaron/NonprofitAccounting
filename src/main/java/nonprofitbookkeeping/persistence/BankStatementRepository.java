package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.BankStatementRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class BankStatementRepository
{
	private BankStatementRepository() {}

	public static List<BankStatementRecord> findByBankAndYear(String bankName,
		int year) throws SQLException
	{
		String sql = """
			SELECT * FROM bank_statement
			WHERE bank_name = ? AND YEAR(statement_date) = ?
			ORDER BY statement_date
		""";
		List<BankStatementRecord> rows = new ArrayList<>();
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, bankName);
			ps.setInt(2, year);
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

	public static void upsert(BankStatementRecord row) throws SQLException
	{
		String sql = """
			MERGE INTO bank_statement(
				bank_name, account_label, statement_date, statement_balance,
				ledger_balance, outstanding, bank_after_outstanding, difference,
				ledger_status, institution_name, institution_contact, account_number,
				account_type, signature_requirement, interest_bearing, currency
			) KEY(bank_name, account_label, statement_date)
			VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			int i = 0;
			ps.setString(++i, row.getBankName());
			ps.setString(++i, row.getAccountLabel());
			ps.setDate(++i, Date.valueOf(row.getStatementDate()));
			ps.setBigDecimal(++i, nz(row.getStatementBalance()));
			ps.setBigDecimal(++i, nz(row.getLedgerBalance()));
			ps.setBigDecimal(++i, nz(row.getOutstanding()));
			ps.setBigDecimal(++i, nz(row.getBankAfterOutstanding()));
			ps.setBigDecimal(++i, nz(row.getDifference()));
			ps.setString(++i, row.getLedgerStatus());
			ps.setString(++i, row.getInstitutionName());
			ps.setString(++i, row.getInstitutionContact());
			ps.setString(++i, row.getAccountNumber());
			ps.setString(++i, row.getAccountType());
			ps.setString(++i, row.getSignatureRequirement());
			ps.setString(++i, row.getInterestBearing());
			ps.setString(++i, row.getCurrency());
			ps.executeUpdate();
		}
	}

	private static BigDecimal nz(BigDecimal v)
	{
		return v == null ? BigDecimal.ZERO : v;
	}

	private static BankStatementRecord mapRow(ResultSet rs) throws SQLException
	{
		BankStatementRecord r = new BankStatementRecord();
		r.setBankName(rs.getString("bank_name"));
		r.setAccountLabel(rs.getString("account_label"));
		r.setStatementDate(rs.getDate("statement_date").toLocalDate());
		r.setStatementBalance(rs.getBigDecimal("statement_balance"));
		r.setLedgerBalance(rs.getBigDecimal("ledger_balance"));
		r.setOutstanding(rs.getBigDecimal("outstanding"));
		r.setBankAfterOutstanding(rs.getBigDecimal("bank_after_outstanding"));
		r.setDifference(rs.getBigDecimal("difference"));
		r.setLedgerStatus(rs.getString("ledger_status"));
		r.setInstitutionName(rs.getString("institution_name"));
		r.setInstitutionContact(rs.getString("institution_contact"));
		r.setAccountNumber(rs.getString("account_number"));
		r.setAccountType(rs.getString("account_type"));
		r.setSignatureRequirement(rs.getString("signature_requirement"));
		r.setInterestBearing(rs.getString("interest_bearing"));
		r.setCurrency(rs.getString("currency"));
		return r;
	}
}
