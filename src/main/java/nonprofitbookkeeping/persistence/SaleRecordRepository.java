package nonprofitbookkeeping.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.SaleRecord;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for the {@code sale_record} table.
 */
public class SaleRecordRepository
{
	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);

	public void replaceAll(List<SaleRecord> sales) throws SQLException
	{
		try (Connection c = Database.get().getConnection())
		{
			c.setAutoCommit(false);
			try
			{
				try (PreparedStatement clear =
					c.prepareStatement("DELETE FROM sale_record"))
				{
					clear.executeUpdate();
				}
				String upsert = """
					MERGE INTO sale_record(
					  sale_id, sale_date_text, item, qty, unit_price, unit_cost, details, updated_at
					) KEY(sale_id)
					VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
					""";
				try (PreparedStatement ps = c.prepareStatement(upsert))
				{
					for (SaleRecord sale : sales == null ? List.<SaleRecord>of() : sales)
					{
						if (sale == null || sale.getId() == null || sale.getId().isBlank())
						{
							continue;
						}
						ps.setString(1, sale.getId());
						ps.setString(2, sale.getDate());
						ps.setString(3, sale.getItem());
						ps.setInt(4, sale.getQty());
						ps.setBigDecimal(5, sale.getPrice());
						ps.setBigDecimal(6, sale.getCost());
						ps.setString(7, toPayload(sale));
						ps.addBatch();
					}
					ps.executeBatch();
				}
				c.commit();
			}
			catch (SQLException e)
			{
				c.rollback();
				throw e;
			}
			finally
			{
				c.setAutoCommit(true);
			}
		}
	}

	public List<SaleRecord> listAll() throws SQLException
	{
		List<SaleRecord> rows = new ArrayList<>();
		try (Connection c = Database.get().getConnection();
		     PreparedStatement ps = c.prepareStatement("""
		     	SELECT sale_id, sale_date_text, item, qty, unit_price, unit_cost, details
		     	FROM sale_record
		     	ORDER BY sale_id
		     	""");
		     ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				SaleRecord sale = fromPayload(rs.getString("details"));
				if (sale.getId() == null || sale.getId().isBlank())
				{
					sale.setId(rs.getString("sale_id"));
				}
				if (sale.getDate() == null || sale.getDate().isBlank())
				{
					sale.setDate(rs.getString("sale_date_text"));
				}
				if (sale.getItem() == null || sale.getItem().isBlank())
				{
					sale.setItem(rs.getString("item"));
				}
				if (sale.getQty() == 0)
				{
					sale.setQty(rs.getInt("qty"));
				}
				if (sale.getPrice() == null)
				{
					sale.setPrice(rs.getBigDecimal("unit_price"));
				}
				if (sale.getCost() == null)
				{
					sale.setCost(rs.getBigDecimal("unit_cost"));
				}
				rows.add(sale);
			}
		}
		return rows;
	}

	private static String toPayload(SaleRecord sale) throws SQLException
	{
		try
		{
			return MAPPER.writeValueAsString(sale);
		}
		catch (IOException e)
		{
			throw new SQLException("Failed to serialize sale payload", e);
		}
	}

	private static SaleRecord fromPayload(String payload) throws SQLException
	{
		if (payload == null || payload.isBlank())
		{
			return new SaleRecord();
		}
		try
		{
			return MAPPER.readValue(payload, SaleRecord.class);
		}
		catch (IOException e)
		{
			throw new SQLException("Failed to deserialize sale payload", e);
		}
	}
}
