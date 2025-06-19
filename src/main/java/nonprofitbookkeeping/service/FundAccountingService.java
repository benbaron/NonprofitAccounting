
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Fund;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service class for managing fund accounting operations.
 * This includes managing funds and accounts, their relationships,
 * retrieving fund balances, and performing fund transfers.
 * Data is stored in a SQL database via {@link DatabaseManager}.
 */
public class FundAccountingService
{
        /** Constructs a new {@code FundAccountingService}. */
        public FundAccountingService()
        {
        }
	
	/**
	 * Adds a new fund to the service.
	 * The fund is stored using its name as the key.
	 *
	 * @param fund The {@link Fund} to add. Must not be null.
	 * @throws IllegalArgumentException if a fund with the same name already exists, or if {@code fund} or its name is null.
	 */
        public void addFund(Fund fund)
        {
                if (fund == null || fund.getName() == null) {
                        throw new IllegalArgumentException("Fund and fund name must not be null.");
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "MERGE INTO fund(fund_id,name,balance) KEY(fund_id) VALUES(?,?,?)"))
                {
                        ps.setString(1, fund.getName());
                        ps.setString(2, fund.getName());
                        ps.setBigDecimal(3, fund.getBalance());
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error adding fund", e);
                }
        }
	
	/**
	 * Removes a fund from the service by its name.
	 * This method also ensures that the removed fund is disassociated from any accounts
	 * it was previously linked with by calling {@link Account#removeFund(Fund)}.
	 *
	 * @param fundName The name of the fund to remove.
	 * @return {@code true} if the fund was found and removed, {@code false} otherwise.
	 */
        public boolean removeFund(String fundName)
        {
                if (fundName == null) {
                        return false;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM fund WHERE fund_id=?"))
                {
                        ps.setString(1, fundName);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        throw new RuntimeException("Error removing fund", e);
                }
        }
	
	/**
	 * Adds a new account to the service.
	 * The account is stored using its name as the key.
	 *
	 * @param account The {@link Account} to add. Must not be null.
	 * @throws IllegalArgumentException if an account with the same name already exists, or if {@code account} or its name is null.
	 */
        public void addAccount(Account account)
        {
                AccountService.addAccount(account);
        }
	
	/**
	 * Removes an account from the service by its name.
	 * This method also ensures that the removed account is disassociated from any funds
	 * it was previously linked with by calling {@link Fund#removeAccount(Account)}.
	 *
	 * @param accountName The name of the account to remove.
	 * @return {@code true} if the account was found and removed, {@code false} otherwise.
	 */
        public boolean removeAccount(String accountName)
        {
                return AccountService.removeAccount(accountName);
        }
	
	/**
	 * Retrieves a list of all funds currently managed by this service.
	 *
	 * @return A new {@link ArrayList} containing all {@link Fund} objects.
	 *         This is a copy, so modifications to the returned list will not affect internal storage.
	 */
        public List<Fund> listFunds()
        {
                List<Fund> list = new ArrayList<>();
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT fund_id,balance FROM fund"))
                {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                Fund f = new Fund(rs.getString(1));
                                f.setBalance(rs.getBigDecimal(2));
                                list.add(f);
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error listing funds", e);
                }
                return list;
        }
	
	/**
	 * Retrieves a list of all accounts currently managed by this service.
	 *
	 * @return A new {@link ArrayList} containing all {@link Account} objects.
	 *         This is a copy, so modifications to the returned list will not affect internal storage.
	 */
        public List<Account> listAccounts()
        {
                return AccountService.getAllAccounts();
        }
	
	/**
	 * Gets the current balances for all funds.
	 * The balance for each fund is obtained via {@link Fund#getBalance()}.
	 *
	 * @return A {@link Map} where keys are fund names (String) and values are their
	 *         corresponding balances ({@link BigDecimal}).
	 */
        public Map<String, BigDecimal> getFundBalances()
        {
                Map<String, BigDecimal> balances = new HashMap<>();
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT fund_id,balance FROM fund"))
                {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                balances.put(rs.getString(1), rs.getBigDecimal(2));
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error retrieving fund balances", e);
                }
                return balances;
        }
	
	/**
	 * Transfers a specified amount between two funds.
	 * This method directly adjusts the balances of the source and destination funds
	 * using {@link Fund#setBalance(BigDecimal)}. It does not create accounting transactions.
	 *
	 * @param fromFund The name of the fund from which the amount will be transferred.
	 * @param toFund The name of the fund to which the amount will be transferred.
	 * @param amount The {@link BigDecimal} amount to transfer. Must be greater than zero.
	 * @throws IllegalArgumentException if {@code amount} is not greater than zero,
	 *                                  or if either {@code fromFund} or {@code toFund} does not exist.
	 */
        public void transferFunds(String fromFund, String toFund, BigDecimal amount)
        {
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Amount must be greater than zero.");
                }
                try (Connection conn = DatabaseManager.getConnection()) {
                        conn.setAutoCommit(false);
                        try (PreparedStatement getStmt = conn.prepareStatement("SELECT balance FROM fund WHERE fund_id=?");
                             PreparedStatement updateStmt = conn.prepareStatement("UPDATE fund SET balance=? WHERE fund_id=?")) {
                                getStmt.setString(1, fromFund);
                                ResultSet rsFrom = getStmt.executeQuery();
                                if (!rsFrom.next()) throw new IllegalArgumentException("Source fund not found");
                                BigDecimal fromBal = rsFrom.getBigDecimal(1);

                                getStmt.setString(1, toFund);
                                ResultSet rsTo = getStmt.executeQuery();
                                if (!rsTo.next()) throw new IllegalArgumentException("Destination fund not found");
                                BigDecimal toBal = rsTo.getBigDecimal(1);

                                updateStmt.setBigDecimal(1, fromBal.subtract(amount));
                                updateStmt.setString(2, fromFund);
                                updateStmt.executeUpdate();

                                updateStmt.setBigDecimal(1, toBal.add(amount));
                                updateStmt.setString(2, toFund);
                                updateStmt.executeUpdate();

                                conn.commit();
                        } catch (SQLException e) {
                                conn.rollback();
                                throw e;
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error transferring funds", e);
                }
        }
	
}
