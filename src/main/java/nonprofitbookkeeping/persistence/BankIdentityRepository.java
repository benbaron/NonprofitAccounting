package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.BankIdentityRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class BankIdentityRepository
{
	private BankIdentityRepository()
	{
	}

	public static Optional<BankIdentityRecord> findByAccountOrRecordId(
		String identifier) throws SQLException
	{
		String sql = """
			SELECT bank_id_record_id, bank_id, bank_name, account_id, account_type
			  FROM bank_id_record
			 WHERE bank_id_record_id = ?
			    OR account_id = ?
			 ORDER BY CASE WHEN bank_id_record_id = ? THEN 0 ELSE 1 END
			 LIMIT 1
		""";
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setString(1, identifier);
			ps.setString(2, identifier);
			ps.setString(3, identifier);
			try (ResultSet rs = ps.executeQuery())
			{
				if (!rs.next())
				{
					return Optional.empty();
				}
				BankIdentityRecord row = new BankIdentityRecord();
				row.setBankIdRecordId(rs.getString("bank_id_record_id"));
				row.setBankId(rs.getString("bank_id"));
				row.setBankName(rs.getString("bank_name"));
				row.setAccountId(rs.getString("account_id"));
				row.setAccountType(rs.getString("account_type"));
				return Optional.of(row);
			}
		}
	}
}
