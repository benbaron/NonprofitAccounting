package nonprofitbookkeeping.model.ofx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class SecurityNodeTest {

    @Test
    @DisplayName("Constructor and Getters: Test with valid ISIN and symbol")
    void testConstructorAndGetters_withValidStrings() {
        String isin = "US0378331005";
        String symbol = "AAPL";
        SecurityNode securityNode = new SecurityNode(isin, symbol);

        assertEquals(isin, securityNode.getISIN(), "getISIN() should return the ISIN provided in the constructor.");
        assertEquals(symbol, securityNode.getSymbol(), "getSymbol() should return the symbol provided in the constructor.");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with null ISIN")
    void testConstructorAndGetters_withNullISIN() {
        String symbol = "MSFT";
        SecurityNode securityNode = new SecurityNode(null, symbol);

        assertNull(securityNode.getISIN(), "getISIN() should return null if null was passed for ISIN.");
        assertEquals(symbol, securityNode.getSymbol(), "getSymbol() should return the correct symbol even if ISIN is null.");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with null symbol")
    void testConstructorAndGetters_withNullSymbol() {
        String isin = "DE000BASF111";
        SecurityNode securityNode = new SecurityNode(isin, null);

        assertEquals(isin, securityNode.getISIN(), "getISIN() should return the correct ISIN even if symbol is null.");
        assertNull(securityNode.getSymbol(), "getSymbol() should return null if null was passed for symbol.");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with both ISIN and symbol as null")
    void testConstructorAndGetters_withBothNull() {
        SecurityNode securityNode = new SecurityNode(null, null);

        assertNull(securityNode.getISIN(), "getISIN() should return null if ISIN was null.");
        assertNull(securityNode.getSymbol(), "getSymbol() should return null if symbol was null.");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with empty ISIN string")
    void testConstructorAndGetters_withEmptyISIN() {
        String symbol = "GOOG";
        SecurityNode securityNode = new SecurityNode("", symbol);

        assertEquals("", securityNode.getISIN(), "getISIN() should return an empty string if an empty string was passed for ISIN.");
        assertEquals(symbol, securityNode.getSymbol(), "getSymbol() should return the correct symbol even if ISIN is empty.");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with empty symbol string")
    void testConstructorAndGetters_withEmptySymbol() {
        String isin = "GB00BH4HKS39";
        SecurityNode securityNode = new SecurityNode(isin, "");

        assertEquals(isin, securityNode.getISIN(), "getISIN() should return the correct ISIN even if symbol is empty.");
        assertEquals("", securityNode.getSymbol(), "getSymbol() should return an empty string if an empty string was passed for symbol.");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with both ISIN and symbol as empty strings")
    void testConstructorAndGetters_withBothEmpty() {
        SecurityNode securityNode = new SecurityNode("", "");

        assertEquals("", securityNode.getISIN(), "getISIN() should return an empty string if ISIN was an empty string.");
        assertEquals("", securityNode.getSymbol(), "getSymbol() should return an empty string if symbol was an empty string.");
    }
}
