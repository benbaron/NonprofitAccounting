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
 * Service class for managing customer and project-related data.
 * <p>
 * This in-memory implementation allows tests and simple UIs to store and
 * retrieve {@link Customer} instances without any backing database.  The
 * methods may later be replaced with versions that persist to disk or a real
 * service, but they are fully functional for the purposes of the demo
 * application and unit tests.
 * </p>
 */
public class CustomerService
{
       /** Internal list storing all known customers. */
       private static final List<Customer> customers = new ArrayList<>();

       /**
        * Retrieves the currently known customer project data.
        *
        * @return a new {@link List} containing all stored customers
        */
       public static List<Customer> getCustomerProjectData()
       {
               return new ArrayList<>(customers);
       }

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

       /**
        * Clears all customer data from the system.
        */
       public static void clearCustomers()
       {
               customers.clear();
       }

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


	
}
