package nonprofitbookkeeping.persistence.impex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

final class JsonColumnCodec
{
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private JsonColumnCodec()
    {
    }

    static String toJson(Object value)
    {
        if (value == null)
        {
            return null;
        }

        try
        {
            return MAPPER.writeValueAsString(value);
        }
        catch (JsonProcessingException ex)
        {
            throw new IllegalStateException("Failed to serialize JSON column value.", ex);
        }
    }
}
