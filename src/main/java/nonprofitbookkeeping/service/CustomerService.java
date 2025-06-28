/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CustomerService.java
 * CustomerService
 */
package nonprofitbookkeeping.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import nonprofitbookkeeping.model.Customer;

/**
 * Service class for managing customer and project-related data.
 * This class provides static methods to retrieve, add, clear, and remove customer information.
 * Note: All methods in this class are currently stub implementations and need to be fully implemented.
 */
public class CustomerService
{
       /** Internal list storing all known customers. */
       private static final List<Customer> customers = new ArrayList<>();

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
               return new ArrayList<>(customers);
       }

	/**
	 * Adds a new customer to the system.
	 * Note: This is a stub implementation and currently does nothing.
	 * It should be implemented to persist the provided {@link Customer} object.
	 *
	 * @param customer The {@link Customer} object to add.
	 */
       public static void addCustomer(Customer customer)
       {
               if (customer != null)
               {
                       customers.add(customer);
               }
       }

	/**
	 * Clears all customer data from the system.
	 * Note: This is a stub implementation and currently does nothing.
	 * It should be implemented to remove all customer records from the data store.
	 */
       public static void clearCustomers()
       {
               customers.clear();
       }

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
               boolean removed = customers.removeIf(c ->
                       c != null && identifier != null &&
                               (identifier.equals(c.getId()) || identifier.equals(c.getName())));

               final boolean result = removed;
               return () -> result;
       }


	
}
