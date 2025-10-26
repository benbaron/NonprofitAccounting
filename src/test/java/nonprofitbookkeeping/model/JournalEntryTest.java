package nonprofitbookkeeping.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class JournalEntryTest
{
        @Test
        void constructorUsesProvidedTransactionId()
        {
                JournalEntry entry = new JournalEntry(
                        "entry-123",
                        "txn-456",
                        "2024-01-10",
                        "100",
                        BigDecimal.ONE,
                        BigDecimal.ZERO,
                        "Memo");

                assertEquals("txn-456", entry.getTransactionId());
                assertEquals("entry-123", entry.getId());
        }

        @Test
        void constructorFallsBackToEntryIdWhenTransactionIdMissing()
        {
                JournalEntry nullTransaction = new JournalEntry(
                        "entry-1",
                        null,
                        "2024-01-10",
                        "100",
                        BigDecimal.ONE,
                        BigDecimal.ZERO,
                        "Memo");

                assertEquals("entry-1", nullTransaction.getTransactionId());

                JournalEntry blankTransaction = new JournalEntry(
                        "entry-2",
                        "   ",
                        "2024-01-10",
                        "100",
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        "Memo");

                assertEquals("entry-2", blankTransaction.getTransactionId());
        }
}
