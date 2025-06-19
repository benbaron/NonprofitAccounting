/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CustomerService.java
 * CustomerService
 */
package nonprofitbookkeeping.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nonprofitbookkeeping.model.Customer;
import nonprofitbookkeeping.service.DatabaseManager;

/**
 * Service class for managing customer and project-related data.
 * Data is persisted using the SQL database via {@link DatabaseManager}.
 */
public class CustomerService
{

        /**
         * Retrieves all customers currently stored in the database.
         *
         * @return a list of {@link Customer} objects.
         */
        public static List<Customer> getCustomerProjectData()
        {
                List<Customer> list = new ArrayList<>();
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT id,name FROM customer"))
                {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                list.add(new Customer(rs.getString(1), rs.getString(2)));
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error loading customers", e);
                }
                return list;
        }

        /**
         * Adds a new customer to the system.
         *
         * @param customer The {@link Customer} object to add.
         */
        public static void addCustomer(Customer customer)
        {
                if (customer == null || customer.getId() == null || customer.getId().trim().isEmpty()) {
                        return;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO customer(id,name) VALUES(?,?)"))
                {
                        ps.setString(1, customer.getId());
                        ps.setString(2, customer.getName());
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error adding customer", e);
                }

        }

        /**
         * Clears all customer data from the system.
         */
        public static void clearCustomers()
        {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM customer"))
                {
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error clearing customers", e);
                }

        }

        /**
         * Removes a customer from the system based on a given identifier.
         *
         * @param identifier The string identifier (e.g., customer ID or name) of the customer to remove.
         * @return {@code true} if a customer was removed, {@code false} otherwise.
         */
        public static boolean removeCustomer(String identifier)
        {
                if (identifier == null || identifier.trim().isEmpty()) {
                        return false;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM customer WHERE id=? OR name=?"))
                {
                        ps.setString(1, identifier);
                        ps.setString(2, identifier);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        throw new RuntimeException("Error removing customer", e);
                }
        }


	
}
