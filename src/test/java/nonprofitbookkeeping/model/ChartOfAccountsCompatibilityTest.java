package nonprofitbookkeeping.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChartOfAccountsCompatibilityTest {

    @Test
    void legacyMapDeserializationPopulatesList() throws Exception {
        String json = "{ \"accountNumberToAccountDetails\": {" +
                "\"1000\": {\"accountNumber\":\"1000\",\"name\":\"Cash\",\"increaseSide\":\"DEBIT\"}," +
                "\"2000\": {\"accountNumber\":\"2000\",\"name\":\"AP\",\"increaseSide\":\"CREDIT\",\"parentAccount\":\"1000\"} } }";

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        ChartOfAccounts coa = mapper.readValue(json, ChartOfAccounts.class);

        List<String> numbers = coa.getAccounts().stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toList());
        assertTrue(numbers.contains("1000"));
        assertTrue(numbers.contains("2000"));
    }

    @Test
    void legacyMapWithoutNumbersStillSetsAccountNumbers() throws Exception {
        String json = "{ \"accountNumberToAccountDetails\": {" +
                "\"1000\": {\"name\":\"Cash\",\"increaseSide\":\"DEBIT\"}," +
                "\"2000\": {\"name\":\"AP\",\"increaseSide\":\"CREDIT\",\"parentAccount\":\"1000\"} } }";

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        ChartOfAccounts coa = mapper.readValue(json, ChartOfAccounts.class);

        List<String> numbers = coa.getAccounts().stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toList());
        assertTrue(numbers.contains("1000"));
        assertTrue(numbers.contains("2000"));
    }
}
