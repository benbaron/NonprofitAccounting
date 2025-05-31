package nonprofitbookkeeping.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class FundTest {

    @Test
    @DisplayName("getFundId should return the name provided in the constructor")
    void testGetFundId_withValidName_returnsName() {
        String fundName = "General Operating Fund";
        Fund fund = new Fund(fundName);
        assertEquals(fundName, fund.getFundId(),
            "getFundId() should return the exact name passed to the constructor.");
    }

    @Test
    @DisplayName("getFundId should return null if null was passed as name in constructor")
    void testGetFundId_withNullNameInConstructor_returnsNull() {
        Fund fund = new Fund(null);
        assertNull(fund.getFundId(),
            "getFundId() should return null if the name provided to constructor was null.");
    }

    @Test
    @DisplayName("getFundId should return an empty string if an empty string was passed as name in constructor")
    void testGetFundId_withEmptyNameInConstructor_returnsEmptyString() {
        String emptyName = "";
        Fund fund = new Fund(emptyName);
        assertEquals(emptyName, fund.getFundId(),
            "getFundId() should return an empty string if an empty string was passed to the constructor.");
    }

    @Test
    @DisplayName("getName should also return the name provided in the constructor (testing Lombok getter)")
    void testGetName_withValidName_returnsName() {
        // This test also implicitly verifies the name field used by getFundId
        String fundName = "Restricted Grant Fund";
        Fund fund = new Fund(fundName);
        assertEquals(fundName, fund.getName(),
            "getName() should return the name passed to the constructor.");
    }
}
