package nonprofitbookkeeping.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class JournalEntryTest
{
        private static final String ENTRY_ID = "entry-123";
        private static final String TRANSACTION_ID = "txn-456";
        private static final String DATE = "2024-03-15";
        private static final String ACCOUNT = "Operating Cash";
        private static final BigDecimal DEBIT = new BigDecimal("15.34");
        private static final BigDecimal CREDIT = new BigDecimal("0");
        private static final String MEMO = "Membership dues";

        @Test
        void constructorUsesProvidedTransactionId()
        {
                JournalEntry entry = new JournalEntry(ENTRY_ID, TRANSACTION_ID, DATE, ACCOUNT, DEBIT, CREDIT, MEMO);

                assertThat(entry.getId()).isEqualTo(ENTRY_ID);
                assertThat(entry.getTransactionId()).isEqualTo(TRANSACTION_ID);
        }

        @Test
        void constructorDefaultsTransactionIdToEntryIdWhenMissing()
        {
                JournalEntry entry = new JournalEntry(ENTRY_ID, DATE, ACCOUNT, DEBIT, CREDIT, MEMO);

                assertThat(entry.getTransactionId()).isEqualTo(ENTRY_ID);
        }

        @Test
        void jacksonRoundTripPreservesTransactionId() throws Exception
        {
                JournalEntry entry = new JournalEntry(ENTRY_ID, TRANSACTION_ID, DATE, ACCOUNT, DEBIT, CREDIT, MEMO);
                ObjectMapper mapper = new ObjectMapper();

                String json = mapper.writeValueAsString(entry);
                JournalEntry restored = mapper.readValue(json, JournalEntry.class);

                assertThat(restored.getId()).isEqualTo(ENTRY_ID);
                assertThat(restored.getTransactionId()).isEqualTo(TRANSACTION_ID);
                assertThat(restored.getMemo()).isEqualTo(MEMO);
        }
}
