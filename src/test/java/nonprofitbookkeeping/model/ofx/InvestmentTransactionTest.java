package nonprofitbookkeeping.model.ofx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class InvestmentTransactionTest {

    private SecurityNode createSampleSecurityNode() {
        return new SecurityNode("US0378331005", "AAPL");
    }

    @Test
    @DisplayName("Constructor and Getters: Test with valid inputs")
    void testConstructorAndGetters_withValidInputs() {
        SecurityNode securityNode = createSampleSecurityNode();
        String fitId = "FITID12345";
        String dtPosted = "20230115"; // yyyyMMdd
        String memo = "Bought Apple shares";
        String transactionName = "AAPL Stock Purchase"; // Maps to super.name (payee)
        String transactionType = "BUY";
        BigDecimal quantity = new BigDecimal("10.0");
        BigDecimal price = new BigDecimal("150.00");
        BigDecimal fees = new BigDecimal("5.00");

        InvestmentTransaction tx = new InvestmentTransaction(
                fitId, dtPosted, memo, transactionName, transactionType,
                securityNode, quantity, price, fees
        );

        // Test InvestmentTransaction specific fields
        assertEquals(securityNode, tx.getSecurityNode());
        assertEquals(0, quantity.compareTo(tx.getQuantity()));
        assertEquals(0, price.compareTo(tx.getPrice()));
        assertEquals(0, fees.compareTo(tx.getFees()));

        // Test fields set in parent Transaction class
        assertEquals(fitId, tx.getFitid());
        assertEquals(LocalDate.parse(dtPosted, DateTimeFormatter.ofPattern("yyyyMMdd")), tx.getLocalDate());
        assertEquals(memo, tx.getMemo());
        assertEquals(transactionName, tx.getPayee()); // 'transactionName' in IT constructor maps to 'name' (payee) in Transaction
        assertEquals(transactionType, tx.getTransactionType());

        // Test trnAmt passed to super constructor: (quantity * price) - fees
        // (10 * 150) - 5 = 1500 - 5 = 1495
        // The constructor logic was: calculatedSuperAmount = quantity.multiply(price).subtract(transactionFees);
        // This needs to be consistent with how trnAmt is set.
        // If it's a BUY, trnAmt might be negative if it represents cash flow.
        // The current super() call passes (qty*price)-fees. Let's assume this is the principal value.
        // If it's a BUY, the `transactionType` "BUY" implies cash outflow.
        // If `trnAmt` itself should be negative for a buy, the constructor logic for `calculatedSuperAmount`
        // would need to be `quantity.multiply(price).add(fees)` and then negated, or similar.
        // The current implementation of InvestmentTransaction constructor passes `(quantity * price) - fees` as `trnAmt`.
        // Let's verify this value.
        BigDecimal expectedTrnAmt = new BigDecimal("1495.00"); // (10 * 150) - 5
        assertNotNull(tx.getAmount(), "Transaction amount (trnAmt) in parent should not be null.");
        assertEquals(0, expectedTrnAmt.compareTo(tx.getAmount()), "trnAmt passed to super should be (qty*price)-fees.");
    }

    // --- getTotal() Method Tests ---

    @Test
    @DisplayName("getTotal: Positive quantity, price, and fees")
    void testGetTotal_withPositiveValues() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2")
        );
        // (10 * 5) - 2 = 48
        assertEquals(0, new BigDecimal("48").compareTo(tx.getTotal()));
    }

    @Test
    @DisplayName("getTotal: Zero fees")
    void testGetTotal_withZeroFees() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), new BigDecimal("10"), new BigDecimal("5"), BigDecimal.ZERO
        );
        // (10 * 5) - 0 = 50
        assertEquals(0, new BigDecimal("50").compareTo(tx.getTotal()));
    }

    @Test
    @DisplayName("getTotal: Null fees (should be treated as zero)")
    void testGetTotal_withNullFees() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), new BigDecimal("10"), new BigDecimal("5"), null // null fees
        );
        // (10 * 5) - 0 = 50
        assertEquals(0, new BigDecimal("50").compareTo(tx.getTotal()));
    }

    @Test
    @DisplayName("getTotal: Null quantity should return null")
    void testGetTotal_withNullQuantity() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), null, new BigDecimal("5"), new BigDecimal("2")
        );
        assertNull(tx.getTotal(), "getTotal should return null if quantity is null.");
    }

    @Test
    @DisplayName("getTotal: Null price should return null")
    void testGetTotal_withNullPrice() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), new BigDecimal("10"), null, new BigDecimal("2")
        );
        assertNull(tx.getTotal(), "getTotal should return null if price is null.");
    }

    @Test
    @DisplayName("getTotal: Zero quantity")
    void testGetTotal_withZeroQuantity() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), BigDecimal.ZERO, new BigDecimal("5"), new BigDecimal("2")
        );
        // (0 * 5) - 2 = -2
        assertEquals(0, new BigDecimal("-2").compareTo(tx.getTotal()));
    }

    @Test
    @DisplayName("getTotal: Zero price")
    void testGetTotal_withZeroPrice() {
        InvestmentTransaction tx = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("2")
        );
        // (10 * 0) - 2 = -2
        assertEquals(0, new BigDecimal("-2").compareTo(tx.getTotal()));
    }

    @Test
    @DisplayName("getTotal: Zero quantity and zero/null fees")
    void testGetTotal_withZeroQuantityAndZeroOrNullFees() {
        InvestmentTransaction tx1 = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), BigDecimal.ZERO, new BigDecimal("5"), BigDecimal.ZERO
        );
        // (0 * 5) - 0 = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(tx1.getTotal()));

        InvestmentTransaction tx2 = new InvestmentTransaction(
                "F2", "20230102", "M2", "N2", "T2",
                createSampleSecurityNode(), BigDecimal.ZERO, new BigDecimal("5"), null
        );
        // (0 * 5) - 0 = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(tx2.getTotal()));
    }

    @Test
    @DisplayName("getTotal: Zero price and zero/null fees")
    void testGetTotal_withZeroPriceAndZeroOrNullFees() {
        InvestmentTransaction tx1 = new InvestmentTransaction(
                "F1", "20230101", "M1", "N1", "T1",
                createSampleSecurityNode(), new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO
        );
        // (10 * 0) - 0 = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(tx1.getTotal()));

        InvestmentTransaction tx2 = new InvestmentTransaction(
                "F2", "20230102", "M2", "N2", "T2",
                createSampleSecurityNode(), new BigDecimal("10"), BigDecimal.ZERO, null
        );
        // (10 * 0) - 0 = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(tx2.getTotal()));
    }
}
