package nonprofitbookkeeping.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractRecordModelTest
{
    @Test
    void customerInheritsIdAndNameAccessors()
    {
        Customer customer = new Customer("C-100", "Alice Donor");

        assertEquals("C-100", customer.getId());
        assertEquals("Alice Donor", customer.getName());
        assertTrue(customer.hasId());
        assertTrue(customer.hasName());
    }

    @Test
    void blankValuesAreReportedAsMissing()
    {
        Customer customer = new Customer("   ", "");

        assertFalse(customer.hasId());
        assertFalse(customer.hasName());
    }
}
