package nonprofitbookkeeping.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    private Company company;

    @BeforeEach
    void setUp() {
        this.company = new Company();
    }

    @Test
    @DisplayName("getCompanyId: Initially should return null")
    void testGetCompanyId_initial_isNull() {
        assertNull(this.company.getCompanyId(), "Initially, companyId should be null.");
    }

    @Test
    @DisplayName("setCompanyId/getCompanyId: Set with a valid identifier")
    void testSetAndGetCompanyId_withValidId_returnsSame() {
        String id = "12345";
        this.company.setCompanyId(id);
        assertEquals(id, this.company.getCompanyId(), "getCompanyId should return the identifier that was set.");
    }
}
