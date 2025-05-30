package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Customer; // Assuming Customer is in this package or imported

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    // CustomerService methods are static, so no instance field for the service itself.

    @BeforeEach
    void setUp() {
        CustomerService.clearCustomers(); // Clear the static list before each test
    }

    // --- addCustomer(Customer customer) Tests ---
    @Test
    @DisplayName("addCustomer: Valid customer should be added")
    void testAddCustomer_validCustomer_shouldBeAdded() {
        Customer customer = new Customer("C001", "Valid Customer");
        CustomerService.addCustomer(customer);
        List<Customer> customers = CustomerService.getCustomerProjectData();
        assertEquals(1, customers.size());
        assertEquals("C001", customers.get(0).getId());
        assertEquals("Valid Customer", customers.get(0).getName());
    }

    @Test
    @DisplayName("addCustomer: Null customer should be ignored")
    void testAddCustomer_nullCustomer_shouldBeIgnored() {
        CustomerService.addCustomer(null);
        assertTrue(CustomerService.getCustomerProjectData().isEmpty());
    }

    @Test
    @DisplayName("addCustomer: Customer with null ID should be ignored")
    void testAddCustomer_customerWithNullId_shouldBeIgnored() {
        Customer customer = new Customer(null, "Customer With Null ID");
        CustomerService.addCustomer(customer);
        assertTrue(CustomerService.getCustomerProjectData().isEmpty());
    }

    @Test
    @DisplayName("addCustomer: Customer with blank ID should be ignored")
    void testAddCustomer_customerWithBlankId_shouldBeIgnored() {
        Customer customer = new Customer("   ", "Customer With Blank ID");
        CustomerService.addCustomer(customer);
        assertTrue(CustomerService.getCustomerProjectData().isEmpty());
    }

    @Test
    @DisplayName("addCustomer: Multiple distinct customers should all be added")
    void testAddCustomer_multipleDistinctCustomers_shouldAllBeAdded() {
        Customer cust1 = new Customer("C001", "Customer One");
        Customer cust2 = new Customer("C002", "Customer Two");
        CustomerService.addCustomer(cust1);
        CustomerService.addCustomer(cust2);
        assertEquals(2, CustomerService.getCustomerProjectData().size());
    }

    @Test
    @DisplayName("addCustomer: Customer with duplicate ID should also be added")
    void testAddCustomer_duplicateId_shouldBeAdded() {
        Customer cust1 = new Customer("C001", "First Customer");
        Customer cust2 = new Customer("C001", "Second Customer Same ID");
        CustomerService.addCustomer(cust1);
        CustomerService.addCustomer(cust2);
        List<Customer> customers = CustomerService.getCustomerProjectData();
        assertEquals(2, customers.size());
        // Verify both are there (List allows duplicates)
        long count = customers.stream().filter(c -> "C001".equals(c.getId())).count();
        assertEquals(2, count);
    }

    // --- removeCustomer(String customerId) Tests ---
    @Test
    @DisplayName("removeCustomer: Existing ID should remove customer and return true")
    void testRemoveCustomer_existingId_shouldRemoveAndReturnTrue() {
        Customer cust1 = new Customer("C001", "To Remove");
        Customer cust2 = new Customer("C002", "To Keep");
        CustomerService.addCustomer(cust1);
        CustomerService.addCustomer(cust2);

        assertTrue(CustomerService.removeCustomer("C001"));
        List<Customer> customers = CustomerService.getCustomerProjectData();
        assertEquals(1, customers.size());
        assertEquals("C002", customers.get(0).getId());
    }

    @Test
    @DisplayName("removeCustomer: Non-existent ID should do nothing and return false")
    void testRemoveCustomer_nonExistentId_shouldDoNothingAndReturnFalse() {
        CustomerService.addCustomer(new Customer("C001", "Customer"));
        assertFalse(CustomerService.removeCustomer("C999"));
        assertEquals(1, CustomerService.getCustomerProjectData().size());
    }

    @Test
    @DisplayName("removeCustomer: Null ID should do nothing and return false")
    void testRemoveCustomer_nullId_shouldDoNothingAndReturnFalse() {
        CustomerService.addCustomer(new Customer("C001", "Customer"));
        assertFalse(CustomerService.removeCustomer(null));
        assertEquals(1, CustomerService.getCustomerProjectData().size());
    }

    @Test
    @DisplayName("removeCustomer: Blank ID should do nothing and return false")
    void testRemoveCustomer_blankId_shouldDoNothingAndReturnFalse() {
        CustomerService.addCustomer(new Customer("C001", "Customer"));
        assertFalse(CustomerService.removeCustomer("   "));
        assertEquals(1, CustomerService.getCustomerProjectData().size());
    }

    // --- getCustomerProjectData() Tests ---
    @Test
    @DisplayName("getCustomerProjectData: When empty, should return an empty list")
    void testGetCustomerProjectData_whenEmpty_shouldReturnEmptyList() {
        assertTrue(CustomerService.getCustomerProjectData().isEmpty());
    }

    @Test
    @DisplayName("getCustomerProjectData: With added customers, should return all customers")
    void testGetCustomerProjectData_withAddedCustomers_shouldReturnAllCustomers() {
        Customer cust1 = new Customer("C001", "Customer One");
        Customer cust2 = new Customer("C002", "Customer Two");
        CustomerService.addCustomer(cust1);
        CustomerService.addCustomer(cust2);

        List<Customer> customers = CustomerService.getCustomerProjectData();
        assertEquals(2, customers.size());
        assertTrue(customers.stream().anyMatch(c -> "C001".equals(c.getId())));
        assertTrue(customers.stream().anyMatch(c -> "C002".equals(c.getId())));
    }

    @Test
    @DisplayName("getCustomerProjectData: Ensure returned list is a copy")
    void testGetCustomerProjectData_ensureReturnedListIsCopy() {
        CustomerService.addCustomer(new Customer("C001", "Test Customer"));
        List<Customer> list1 = CustomerService.getCustomerProjectData();
        assertNotNull(list1);
        assertEquals(1, list1.size());

        // Attempt to modify the returned list.
        // If CustomerService.getCustomerProjectData() returns a direct reference, this would fail.
        // If it returns a copy, the original list in the service remains unchanged.
        try {
            list1.add(new Customer("C002", "Dummy Customer"));
        } catch (UnsupportedOperationException e) {
            // This would happen if an unmodifiable list is returned, which is also a valid way to return a copy.
        }
        
        assertEquals(1, CustomerService.getCustomerProjectData().size(), "Modifying the list returned by getCustomerProjectData should not affect the internal list.");
        
        List<Customer> list2 = CustomerService.getCustomerProjectData();
        assertNotSame(list1, list2, "getCustomerProjectData should return a new list instance (a copy).");
    }

    // --- clearCustomers() Tests ---
    @Test
    @DisplayName("clearCustomers: After adding and then clearing, list should be empty")
    void testClearCustomers_shouldEmptyTheCustomerList() {
        CustomerService.addCustomer(new Customer("C001", "Test Customer"));
        assertFalse(CustomerService.getCustomerProjectData().isEmpty(), "List should not be empty before clear");

        CustomerService.clearCustomers();
        assertTrue(CustomerService.getCustomerProjectData().isEmpty(), "List should be empty after clearCustomers.");
    }
}
