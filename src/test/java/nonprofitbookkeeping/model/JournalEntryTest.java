package nonprofitbookkeeping.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class JournalEntryTest
{
        @Test
        void constructorUsesProvidedTransactionId()
        {
                JournalEntry entry = new JournalEntry(
                        "ENTRY-1",
                        "TX-42",
                        "2024-06-01",
                        "Checking",
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO,
                        "Deposit");

                assertEquals("ENTRY-1", entry.getId());
                assertEquals("TX-42", entry.getTransactionId());
        }

        @Test
        void constructorDefaultsTransactionIdToEntryIdWhenMissing()
        {
                JournalEntry entry = new JournalEntry(
                        "ENTRY-2",
                        null,
                        "2024-06-01",
                        "Checking",
                        BigDecimal.ZERO,
                        new BigDecimal("100.00"),
                        "Payment");

                assertEquals("ENTRY-2", entry.getId());
                assertEquals("ENTRY-2", entry.getTransactionId());
        }
}
