/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CustomerService.java
 * CustomerService
 */
package nonprofitbookkeeping.service;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
=======
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
>>>>>>> b1f07f2 Extend SQL support
import java.util.ArrayList;
=======
>>>>>>> 6159d55 Revert service changes
import java.util.List;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
=======
=======
import java.util.function.BooleanSupplier;
>>>>>>> 6159d55 Revert service changes

>>>>>>> b1f07f2 Extend SQL support
import nonprofitbookkeeping.model.Customer;

/**
 * Service class for managing customer and project-related data.
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * <p>
 * This in-memory implementation allows tests and simple UIs to store and
 * retrieve {@link Customer} instances without any backing database.  The
 * methods may later be replaced with versions that persist to disk or a real
 * service, but they are fully functional for the purposes of the demo
 * application and unit tests.
 * </p>
=======
 * Data is persisted using the SQL database via {@link DatabaseManager}.
>>>>>>> b1f07f2 Extend SQL support
=======
 * This class provides static methods to retrieve, add, clear, and remove customer information.
 * Note: All methods in this class are currently stub implementations and need to be fully implemented.
>>>>>>> 6159d55 Revert service changes
 */
public class CustomerService
{
       /** Internal list storing all known customers. */
       private static final List<Customer> customers = new ArrayList<>();

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
       /**
        * Retrieves the currently known customer project data.
        *
        * @return a new {@link List} containing all stored customers
        */
       public static List<Customer> getCustomerProjectData()
       {
               return new ArrayList<>(customers);
       }
=======
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
>>>>>>> b1f07f2 Extend SQL support
=======
	/**
	 * Retrieves data related to customer projects.
	 * Note: This is a stub implementation and currently returns null.
	 * It should be implemented to fetch and return a list of {@link Customer} objects
	 * or a custom data structure representing customer project information.
	 *
	 * @return A list of customer project data (e.g., {@code List<Customer>}),
	 *         or null if the implementation is not complete.
	 */
	public static List<Customer> getCustomerProjectData()
	{
		// TODO Auto-generated method stub
		// Implementation should fetch relevant customer and project data.
		return null;
	}
>>>>>>> 6159d55 Revert service changes

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
       /**
        * Adds a new customer to the system.
        *
        * @param customer The {@link Customer} object to add. Null values are ignored.
        */
       public static void addCustomer(Customer customer)
       {
               if (customer != null)
               {
                       customers.add(customer);
               }
       }
=======
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
>>>>>>> b1f07f2 Extend SQL support
=======
	/**
	 * Adds a new customer to the system.
	 * Note: This is a stub implementation and currently does nothing.
	 * It should be implemented to persist the provided {@link Customer} object.
	 *
	 * @param customer The {@link Customer} object to add.
	 */
	public static void addCustomer(Customer customer)
	{
		// TODO Auto-generated method stub
		// Implementation should add the customer to a data store.
		
	}
>>>>>>> 6159d55 Revert service changes

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
       /**
        * Clears all customer data from the system.
        */
       public static void clearCustomers()
       {
               customers.clear();
       }
=======
        }
>>>>>>> b1f07f2 Extend SQL support
=======
	/**
	 * Clears all customer data from the system.
	 * Note: This is a stub implementation and currently does nothing.
	 * It should be implemented to remove all customer records from the data store.
	 */
	public static void clearCustomers()
	{
		// TODO Auto-generated method stub
		// Implementation should clear all customers from the data store.
		
	}
>>>>>>> 6159d55 Revert service changes

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
       /**
        * Removes a customer from the system based on a given identifier.
        *
        * @param identifier The string identifier (e.g., customer ID or name) of the customer to remove.
        * @return {@code true} if a matching customer was removed; {@code false} otherwise
        */
       public static boolean removeCustomer(String identifier)
       {
               boolean removed = customers.removeIf(c ->
                       c != null && identifier != null &&
                               (identifier.equals(c.getId()) || identifier.equals(c.getName())));

               return removed;
       }
=======
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
>>>>>>> b1f07f2 Extend SQL support
=======
	/**
	 * Removes a customer from the system based on a given identifier.
	 * Note: This is a stub implementation and currently returns null.
	 * The return type {@link BooleanSupplier} is unusual for a remove operation;
	 * typically, it might return a boolean indicating success/failure or the removed object.
	 * This method should be implemented to find and remove the customer.
	 *
	 * @param identifier The string identifier (e.g., customer ID or name) of the customer to remove.
	 * @return A {@link BooleanSupplier} (currently null due to stub implementation).
	 *         The intended behavior of this supplier needs clarification.
	 */
	public static BooleanSupplier removeCustomer(String identifier)
	{
		// TODO Auto-generated method stub
		// Implementation should find and remove the customer based on the identifier.
		// The return type BooleanSupplier is atypical; consider returning boolean or void.
		return null;
	}
>>>>>>> 6159d55 Revert service changes


	
}
