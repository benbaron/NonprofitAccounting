package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Grant; // Ensure this path is correct

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
// No @BeforeEach is strictly needed as each test creates its own service instance for isolation.

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrantsServiceTest {

    // --- Constructor Test ---
    @Test
    @DisplayName("Constructor: New GrantsService instance should have an empty list of grants")
    void testConstructor_newServiceInstance_shouldHaveEmptyGrantsList() {
        GrantsService service = new GrantsService();
        assertTrue(service.getAllGrants().isEmpty(), "A new service instance should have no grants.");
    }

    // --- addGrant(Grant grant) Tests ---
    @Test
    @DisplayName("addGrant: Valid grant should be added to the instance's list")
    void testAddGrant_validGrant_shouldBeAddedToInstanceList() {
        GrantsService service = new GrantsService();
        Grant grant = new Grant("G001", "Grantor A", BigDecimal.valueOf(1000), "2023-01-01", "Purpose A", "Active");
        service.addGrant(grant);
        List<Grant> grants = service.getAllGrants();
        assertEquals(1, grants.size());
        assertEquals("G001", grants.get(0).getGrantId());
    }

    @Test
    @DisplayName("addGrant: Null grant should be ignored")
    void testAddGrant_nullGrant_shouldBeIgnored() {
        GrantsService service = new GrantsService();
        service.addGrant(null);
        assertTrue(service.getAllGrants().isEmpty());
    }

    @Test
    @DisplayName("addGrant: Grant with null ID should be ignored")
    void testAddGrant_grantWithNullId_shouldBeIgnored() {
        GrantsService service = new GrantsService();
        Grant grant = new Grant(null, "Grantor B", BigDecimal.valueOf(500), "2023-02-01", "Purpose B", "Pending");
        service.addGrant(grant);
        assertTrue(service.getAllGrants().isEmpty());
    }

    @Test
    @DisplayName("addGrant: Grant with blank ID should be ignored")
    void testAddGrant_grantWithBlankId_shouldBeIgnored() {
        GrantsService service = new GrantsService();
        Grant grant = new Grant("   ", "Grantor C", BigDecimal.valueOf(200), "2023-03-01", "Purpose C", "Awarded");
        service.addGrant(grant);
        assertTrue(service.getAllGrants().isEmpty());
    }

    @Test
    @DisplayName("addGrant: Multiple grants should all be added to the instance's list")
    void testAddGrant_multipleGrants_shouldAllBeAddedToInstanceList() {
        GrantsService service = new GrantsService();
        Grant grant1 = new Grant("G001", "Grantor A", BigDecimal.valueOf(1000), "2023-01-01", "Purpose A", "Active");
        Grant grant2 = new Grant("G002", "Grantor D", BigDecimal.valueOf(2000), "2023-04-01", "Purpose D", "Active");
        service.addGrant(grant1);
        service.addGrant(grant2);
        assertEquals(2, service.getAllGrants().size());
    }

    @Test
    @DisplayName("addGrant: Adding to separate instances should not affect each other")
    void testAddGrant_toSeparateInstances_shouldNotAffectEachOther() {
        GrantsService service1 = new GrantsService();
        GrantsService service2 = new GrantsService();
        Grant grant1 = new Grant("G001", "Grantor A", BigDecimal.valueOf(1000), "2023-01-01", "Purpose A", "Active");
        Grant grant2 = new Grant("G002", "Grantor D", BigDecimal.valueOf(2000), "2023-04-01", "Purpose D", "Active");

        service1.addGrant(grant1);
        service2.addGrant(grant2);

        assertEquals(1, service1.getAllGrants().size());
        assertTrue(service1.getAllGrants().stream().anyMatch(g -> "G001".equals(g.getGrantId())));
        assertEquals(1, service2.getAllGrants().size());
        assertTrue(service2.getAllGrants().stream().anyMatch(g -> "G002".equals(g.getGrantId())));
        assertFalse(service1.getAllGrants().stream().anyMatch(g -> "G002".equals(g.getGrantId())));
    }

    @Test
    @DisplayName("addGrant: Grant with duplicate ID should also be added (List behavior)")
    void testAddGrant_duplicateId_shouldBeAdded() {
        GrantsService service = new GrantsService();
        Grant grant1 = new Grant("G001", "Grantor A", BigDecimal.valueOf(1000), "2023-01-01", "Purpose A", "Active");
        Grant grant2 = new Grant("G001", "Grantor E", BigDecimal.valueOf(5000), "2023-05-01", "Purpose E", "Pending");
        service.addGrant(grant1);
        service.addGrant(grant2);
        List<Grant> grants = service.getAllGrants();
        assertEquals(2, grants.size());
        assertEquals(2, grants.stream().filter(g -> "G001".equals(g.getGrantId())).count());
    }

    // --- removeGrant(String grantId) Tests ---
    @Test
    @DisplayName("removeGrant: Existing ID should remove from instance and return true")
    void testRemoveGrant_existingId_shouldRemoveFromInstanceAndReturnTrue() {
        GrantsService service = new GrantsService();
        Grant grant1 = new Grant("G001", "ToRemove", BigDecimal.ZERO, "", "", "");
        Grant grant2 = new Grant("G002", "ToKeep", BigDecimal.ZERO, "", "", "");
        service.addGrant(grant1);
        service.addGrant(grant2);

        assertTrue(service.removeGrant("G001"));
        List<Grant> grants = service.getAllGrants();
        assertEquals(1, grants.size());
        assertEquals("G002", grants.get(0).getGrantId());
    }

    @Test
    @DisplayName("removeGrant: Non-existent ID should do nothing on instance and return false")
    void testRemoveGrant_nonExistentId_shouldDoNothingOnInstanceAndReturnFalse() {
        GrantsService service = new GrantsService();
        service.addGrant(new Grant("G001", "Grantor", BigDecimal.ZERO, "", "", ""));
        assertFalse(service.removeGrant("G999"));
        assertEquals(1, service.getAllGrants().size());
    }

    @Test
    @DisplayName("removeGrant: Null ID should do nothing on instance and return false")
    void testRemoveGrant_nullId_shouldDoNothingOnInstanceAndReturnFalse() {
        GrantsService service = new GrantsService();
        service.addGrant(new Grant("G001", "Grantor", BigDecimal.ZERO, "", "", ""));
        assertFalse(service.removeGrant(null));
        assertEquals(1, service.getAllGrants().size());
    }

    @Test
    @DisplayName("removeGrant: Blank ID should do nothing on instance and return false")
    void testRemoveGrant_blankId_shouldDoNothingOnInstanceAndReturnFalse() {
        GrantsService service = new GrantsService();
        service.addGrant(new Grant("G001", "Grantor", BigDecimal.ZERO, "", "", ""));
        assertFalse(service.removeGrant("   "));
        assertEquals(1, service.getAllGrants().size());
    }

    // --- getAllGrants() Tests ---
    // testGetAllGrants_onNewInstance_shouldReturnEmptyList is covered by constructor test.

    @Test
    @DisplayName("getAllGrants: After adding, should return all added grants for instance")
    void testGetAllGrants_afterAdding_shouldReturnAllAddedGrantsForInstance() {
        GrantsService service = new GrantsService();
        Grant grant1 = new Grant("G001", "Grantor A", BigDecimal.valueOf(1000), "2023-01-01", "Purpose A", "Active");
        Grant grant2 = new Grant("G002", "Grantor D", BigDecimal.valueOf(2000), "2023-04-01", "Purpose D", "Active");
        service.addGrant(grant1);
        service.addGrant(grant2);

        List<Grant> grants = service.getAllGrants();
        assertEquals(2, grants.size());
        assertTrue(grants.stream().anyMatch(g -> "G001".equals(g.getGrantId())));
        assertTrue(grants.stream().anyMatch(g -> "G002".equals(g.getGrantId())));
    }

    @Test
    @DisplayName("getAllGrants: Ensure returned list is a copy for instance")
    void testGetAllGrants_ensureReturnedListIsCopyForInstance() {
        GrantsService service = new GrantsService();
        service.addGrant(new Grant("G001", "Test Grant", BigDecimal.ZERO, "", "", ""));
        List<Grant> list1 = service.getAllGrants();
        assertNotNull(list1);
        assertEquals(1, list1.size());

        try {
            list1.add(new Grant("G002", "Dummy Grant", BigDecimal.ZERO, "", "", ""));
        } catch (UnsupportedOperationException e) {
            // Expected if an unmodifiable list is returned.
        }

        assertEquals(1, service.getAllGrants().size(), "Modifying the list returned by getAllGrants should not affect the instance's internal list.");

        List<Grant> list2 = service.getAllGrants();
        assertNotSame(list1, list2, "getAllGrants should return a new list instance (a copy).");
    }

    // --- clearGrants() Test ---
    @Test
    @DisplayName("clearGrants: Should empty the instance's grants list and not affect other instances")
    void testClearGrants_onInstance_shouldEmptyItsGrantsListAndNotAffectOtherInstances() {
        GrantsService service1 = new GrantsService();
        GrantsService service2 = new GrantsService();

        Grant grant1 = new Grant("G001", "Grant 1", BigDecimal.ONE, "", "", "");
        Grant grant2 = new Grant("G002", "Grant 2", BigDecimal.TEN, "", "", "");
        Grant grant3 = new Grant("G003", "Grant 3", BigDecimal.ONE, "", "", "");

        service1.addGrant(grant1);
        service1.addGrant(grant2);
        service2.addGrant(grant3);

        assertFalse(service1.getAllGrants().isEmpty());
        assertFalse(service2.getAllGrants().isEmpty());

        service1.clearGrants();

        assertTrue(service1.getAllGrants().isEmpty(), "Service 1 grants list should be empty after clearGrants.");
        assertFalse(service2.getAllGrants().isEmpty(), "Service 2 grants list should not be affected by clearing service 1.");
        assertEquals(1, service2.getAllGrants().size());
    }
}
