
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.DonorContact;

import java.sql.*;
import java.util.*;

public class DonorRepository
{
	public void upsert(DonorContact d) throws SQLException
	{
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(
				"MERGE INTO donor(name) KEY(name) VALUES (?)"))
		{
			ps.setString(1, d.getName());
			ps.executeUpdate();
		}
		
	}
	
	public List<DonorContact> list() throws SQLException
	{
		List<DonorContact> out = new ArrayList<>();
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps =
				c.prepareStatement("SELECT name FROM donor ORDER BY name");
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				DonorContact d = new DonorContact();
				d.setName(rs.getString(1));
				out.add(d);
			}
			
		}
		
		return out;
		
	}
	
}
