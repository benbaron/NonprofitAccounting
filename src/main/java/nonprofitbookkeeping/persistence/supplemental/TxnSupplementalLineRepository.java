package nonprofitbookkeeping.persistence.supplemental;

import jakarta.enterprise.context.ApplicationScoped;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class TxnSupplementalLineRepository.
 */
@ApplicationScoped
public class TxnSupplementalLineRepository
{
	
	/** The Constant LIST_BY_TXN_SQL. */
	private static final String LIST_BY_TXN_SQL =
		"SELECT id, txn_id, entry_id, line_kind, counterparty_person_id, description, " +
		"reference, amount, due_date, start_date, end_date, notes " +
		"FROM txn_supplemental_line WHERE txn_id = ? ORDER BY id";
	
	/** The Constant DELETE_BY_TXN_SQL. */
	private static final String DELETE_BY_TXN_SQL =
		"DELETE FROM txn_supplemental_line WHERE txn_id = ?";
	
	/** The Constant INSERT_SQL. */
	private static final String INSERT_SQL =
		"INSERT INTO txn_supplemental_line(" +
		"txn_id, entry_id, line_kind, counterparty_person_id, description, reference, amount, " +
		"due_date, start_date, end_date, notes, created_at, updated_at" +
		") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

	/**
	 * List by txn id.
	 *
	 * @param txnId the txn id
	 * @return the list
	 * @throws SQLException the SQL exception
	 */
	public List<TxnSupplementalLineRecord> listByTxnId(long txnId) throws SQLException
	{
		List<TxnSupplementalLineRecord> records = new ArrayList<>();

		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(LIST_BY_TXN_SQL))
		{
			ps.setLong(1, txnId);

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					records.add(mapRow(rs));
				}
			}
		}

		return records;
	}

	/**
	 * Replace for txn.
	 *
	 * @param c the c
	 * @param txnId the txn id
	 * @param records the records
	 * @throws SQLException the SQL exception
	 */
	public void replaceForTxn(Connection c, long txnId, List<TxnSupplementalLineRecord> records)
		throws SQLException
	{
		deleteByTxnId(c, txnId);

		if (records != null && !records.isEmpty())
		{
			try (PreparedStatement ps = c.prepareStatement(INSERT_SQL,
				Statement.RETURN_GENERATED_KEYS))
			{
				for (TxnSupplementalLineRecord record : records)
				{
					record.txnId = txnId;
					bindInsert(ps, record);
					ps.addBatch();
				}

				ps.executeBatch();

				try (ResultSet keys = ps.getGeneratedKeys())
				{
					int index = 0;
					while (keys.next() && index < records.size())
					{
						records.get(index).id = keys.getLong(1);
						index++;
					}
				}
			}
		}
	}

	/**
	 * Delete by txn id.
	 *
	 * @param c the c
	 * @param txnId the txn id
	 * @throws SQLException the SQL exception
	 */
	private static void deleteByTxnId(Connection c, long txnId) throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement(DELETE_BY_TXN_SQL))
		{
			ps.setLong(1, txnId);
			ps.executeUpdate();
		}
	}

	/**
	 * Bind insert.
	 *
	 * @param ps the ps
	 * @param record the record
	 * @throws SQLException the SQL exception
	 */
	private static void bindInsert(PreparedStatement ps, TxnSupplementalLineRecord record)
		throws SQLException
	{
		ps.setLong(1, record.txnId);
		setNullableLong(ps, 2, record.entryId);
		ps.setString(3, record.kind == null ? SupplementalLineKind.RECEIVABLE.name() : record.kind.name());
		setNullableLong(ps, 4, record.counterpartyPersonId);
		ps.setString(5, record.description);
		ps.setString(6, record.reference);
		ps.setBigDecimal(7, record.amount);
		ps.setDate(8, record.dueDate == null ? null : Date.valueOf(record.dueDate));
		ps.setDate(9, record.startDate == null ? null : Date.valueOf(record.startDate));
		ps.setDate(10, record.endDate == null ? null : Date.valueOf(record.endDate));
		ps.setString(11, record.notes);
	}

	/**
	 * Map row.
	 *
	 * @param rs the rs
	 * @return the txn supplemental line record
	 * @throws SQLException the SQL exception
	 */
	private static TxnSupplementalLineRecord mapRow(ResultSet rs) throws SQLException
	{
		TxnSupplementalLineRecord record = new TxnSupplementalLineRecord();
		record.id = rs.getLong("id");
		record.txnId = rs.getLong("txn_id");
		record.entryId = getNullableLong(rs, "entry_id");
		record.kind = SupplementalLineKind.valueOf(rs.getString("line_kind"));
		record.counterpartyPersonId = getNullableLong(rs, "counterparty_person_id");
		record.description = rs.getString("description");
		record.reference = rs.getString("reference");
		record.amount = rs.getBigDecimal("amount");
		Date due = rs.getDate("due_date");
		record.dueDate = due == null ? null : due.toLocalDate();
		Date start = rs.getDate("start_date");
		record.startDate = start == null ? null : start.toLocalDate();
		Date end = rs.getDate("end_date");
		record.endDate = end == null ? null : end.toLocalDate();
		record.notes = rs.getString("notes");
		return record;
	}

	/**
	 * Gets the nullable long.
	 *
	 * @param rs the rs
	 * @param column the column
	 * @return the nullable long
	 * @throws SQLException the SQL exception
	 */
	private static Long getNullableLong(ResultSet rs, String column) throws SQLException
	{
		long value = rs.getLong(column);
		return rs.wasNull() ? null : value;
	}

	/**
	 * Sets the nullable long.
	 *
	 * @param ps the ps
	 * @param index the index
	 * @param value the value
	 * @throws SQLException the SQL exception
	 */
	private static void setNullableLong(PreparedStatement ps, int index, Long value)
		throws SQLException
	{
		if (value == null)
		{
			ps.setNull(index, Types.BIGINT);
		}
		else
		{
			ps.setLong(index, value);
		}
	}
}