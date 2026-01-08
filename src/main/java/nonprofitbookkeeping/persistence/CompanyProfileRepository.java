
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CompanyProfileModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Repository for reading and writing the single company profile row. */
public class CompanyProfileRepository
{
	
	private static final String UPSERT_SQL =
		"""
			    MERGE INTO company_profile(id, name, address, phone, email,
			                               fiscal_year_start, base_currency, starting_balance_date,
			                               chart_of_accounts_type, admin_username, admin_password,
			                               default_bank_account, enable_fund_accounting, enable_inventory,
			                               enable_multi_currency, legal_structure, tax_id, company_file_dir,
			                               company_file_name)
			    KEY(id)
			    VALUES(1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";
	private static final String SELECT_SQL =
		"SELECT name, address, phone, email, fiscal_year_start, base_currency, starting_balance_date, " +
			"chart_of_accounts_type, admin_username, admin_password, default_bank_account, enable_fund_accounting, " +
			"enable_inventory, enable_multi_currency, legal_structure, tax_id, company_file_dir, company_file_name " +
			"FROM company_profile WHERE id = 1";
	private static final String DELETE_SQL =
		"DELETE FROM company_profile WHERE id = 1";
	
	/**
	 * Saves the provided company profile. Passing {@code null} clears the stored row
	 * to reflect the absence of profile data.
	 *
	 * @param profile profile model to persist or {@code null} to delete
	 * @throws SQLException if any database statement fails
	 */
	public void save(CompanyProfileModel profile) throws SQLException
	{
		
		if (profile == null)
		{
			
			try (Connection c = Database.get().getConnection();
				PreparedStatement ps = c.prepareStatement(DELETE_SQL))
			{
				ps.executeUpdate();
			}
			
			return;
		}
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
		{
			int i = 0;
			ps.setString(++i, profile.getCompanyName());
			ps.setString(++i, profile.getAddress());
			ps.setString(++i, profile.getPhone());
			ps.setString(++i, profile.getEmail());
			ps.setString(++i, profile.getFiscalYearStart());
			ps.setString(++i, profile.getBaseCurrency());
			ps.setString(++i, profile.getStartingBalanceDate());
			ps.setString(++i, profile.getChartOfAccountsType());
			ps.setString(++i, profile.getAdminUsername());
			ps.setString(++i, profile.getAdminPassword());
			ps.setString(++i, profile.getDefaultBankAccount());
			ps.setBoolean(++i, profile.isEnableFundAccounting());
			ps.setBoolean(++i, profile.isEnableInventory());
			ps.setBoolean(++i, profile.isEnableMultiCurrency());
			ps.setString(++i, profile.getLegalStructure());
			ps.setString(++i, profile.getTaxId());
			ps.setString(++i, profile.getCompanyFileDir());
			ps.setString(++i, profile.getCompanyFileName());
			ps.executeUpdate();
		}
		
	}
	
	/**
	 * Loads the persisted company profile if present.
	 *
	 * @return an {@link Optional} containing the profile when stored
	 * @throws SQLException if the query fails
	 */
	public Optional<CompanyProfileModel> load() throws SQLException
	{
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(SELECT_SQL);
			ResultSet rs = ps.executeQuery())
		{
			
			if (!rs.next())
			{
				return Optional.empty();
			}
			
			CompanyProfileModel profile = new CompanyProfileModel();
			profile.setCompanyName(rs.getString("name"));
			profile.setAddress(rs.getString("address"));
			profile.setPhone(rs.getString("phone"));
			profile.setEmail(rs.getString("email"));
			profile.setFiscalYearStart(rs.getString("fiscal_year_start"));
			profile.setBaseCurrency(rs.getString("base_currency"));
			profile
				.setStartingBalanceDate(rs.getString("starting_balance_date"));
			profile
				.setChartOfAccountsType(rs.getString("chart_of_accounts_type"));
			profile.setAdminUsername(rs.getString("admin_username"));
			profile.setAdminPassword(rs.getString("admin_password"));
			profile.setDefaultBankAccount(rs.getString("default_bank_account"));
			profile.setEnableFundAccounting(
				rs.getBoolean("enable_fund_accounting"));
			profile.setEnableInventory(rs.getBoolean("enable_inventory"));
			profile
				.setEnableMultiCurrency(rs.getBoolean("enable_multi_currency"));
			profile.setLegalStructure(rs.getString("legal_structure"));
			profile.setTaxId(rs.getString("tax_id"));
			profile.setCompanyFileDir(rs.getString("company_file_dir"));
			profile.setCompanyFileName(rs.getString("company_file_name"));
			return Optional.of(profile);
		}
		
	}
	
}
