/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CustomerService.java
 * CustomerService
 */
package nonprofitbookkeeping.service;

import java.util.ArrayList;
import java.util.List;
import nonprofitbookkeeping.model.Customer;

/**
 * Service class for managing {@link Customer} objects.
 * Provides methods for retrieving, adding, and removing customers.
 * Customers are stored in an in-memory list.
 */
public class CustomerService
{
	/**
	 * In-memory list to store Customer objects.
	 */
	private static List<Customer> customers = new ArrayList<>();

	/**
	 * Retrieves all customer data.
	 * <p>
	 * Note: The method name "getCustomerProjectData" might imply specific filtering
	 * or data shaping related to projects in a more complex system. Currently, it
	 * returns all customers.
	 * </p>
	 *
	 * @return A new {@code List<Customer>} containing all stored customers.
	 *         Returns an empty list if no customers are present. This is a copy,
	 *         so modifications to the returned list do not affect internal storage.
	 */
	public static List<Customer> getCustomerProjectData()
	{
		// Return a copy to prevent external modification
		if (customers == null) { // Defensive check, though it's initialized
		    customers = new ArrayList<>();
		}
		return new ArrayList<>(customers);
	}

	/**
	 * Adds a new customer to the in-memory storage.
	 * If the provided customer is null, or its ID is null or blank,
	 * the customer is not added. This implementation allows duplicate customers
	 * if their IDs are the same (as List allows duplicates).
	 *
	 * @param customer The {@link Customer} object to be added. Must not be null
	 *                 and must have a valid, non-blank ID.
	 */
	public static void addCustomer(Customer customer) {
		if (customer == null || customer.getId() == null || customer.getId().trim().isEmpty()) {
			// Optionally, log a warning here
			return;
		}
		if (customers == null) { // Defensive, already initialized
			customers = new ArrayList<>();
		}
		customers.add(customer);
	}

	/**
	 * Removes a customer from the in-memory storage based on their ID.
	 * If the provided customer ID is null or blank, no action is taken.
	 *
	 * @param customerId The unique identifier of the customer to be removed.
	 * @return {@code true} if a customer was removed as a result of this call,
	 *         {@code false} otherwise (including if the customerId is invalid or
	 *         no such customer was found).
	 */
	public static boolean removeCustomer(String customerId) {
		if (customerId == null || customerId.trim().isEmpty() || customers == null) {
			return false;
		}
		return customers.removeIf(customer -> customerId.equals(customer.getId()));
	}

	/**
	 * Clears all customers from the in-memory storage.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * between test executions.
	 */
	public static void clearCustomers() {
		if (customers != null) {
			customers.clear();
		} else {
			// Should not happen if initialized at declaration, but defensive
			customers = new ArrayList<>();
		}
	}
	
}
