package nonprofitbookkeeping.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class JournalEntryTest
{
        @Test
        void constructorUsesProvidedTransactionId()
        {
                JournalEntry entry = new JournalEntry("entry-1", "txn-1", "2023-12-01", "Cash",
                        new BigDecimal("10.00"), BigDecimal.ZERO, "Memo");

                assertEquals("txn-1", entry.getTransactionId(),
                        "Constructor should keep the provided transaction identifier");
        }

        @Test
        void constructorFallsBackToEntryIdWhenTransactionIdMissing()
        {
                JournalEntry entry = new JournalEntry("entry-2", null, "2023-12-02", "Cash",
                        new BigDecimal("5.00"), BigDecimal.ZERO, "Memo");

                assertEquals("entry-2", entry.getTransactionId(),
                        "Missing transaction identifier should fall back to the entry identifier");
        }

        @Test
        void settersKeepFallbackBehaviour()
        {
                JournalEntry entry = new JournalEntry();
                entry.setId("entry-3");
                entry.setTransactionId("txn-3");

                assertEquals("txn-3", entry.getTransactionId(),
                        "Setter should accept explicit transaction identifiers");

                entry.setTransactionId(null);

                assertEquals("entry-3", entry.getTransactionId(),
                        "Setting a null transaction identifier should fall back to entry identifier");
        }
}
