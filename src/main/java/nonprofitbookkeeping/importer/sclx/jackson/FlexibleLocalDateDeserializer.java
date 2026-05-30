package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Accepts multiple date shapes for SCLX compatibility:
 * - ISO-8601 date string (yyyy-MM-dd)
 * - array form [yyyy, mm, dd]
 */
public class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate>
{
    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        JsonToken token = parser.currentToken();

        if (token == JsonToken.VALUE_STRING)
        {
            String raw = parser.getText();
            if (raw == null || raw.isBlank())
            {
                return null;
            }
            try
            {
                return LocalDate.parse(raw.trim());
            }
            catch (DateTimeParseException ex)
            {
                throw context.weirdStringException(raw, LocalDate.class,
                    "Expected ISO date string (yyyy-MM-dd)");
            }
        }

        if (token == JsonToken.START_ARRAY)
        {
            int year = parser.nextIntValue(Integer.MIN_VALUE);
            int month = parser.nextIntValue(Integer.MIN_VALUE);
            int day = parser.nextIntValue(Integer.MIN_VALUE);

            if (year == Integer.MIN_VALUE || month == Integer.MIN_VALUE || day == Integer.MIN_VALUE)
            {
                throw JsonMappingException.from(parser, "Expected array date shape [yyyy, mm, dd]");
            }

            JsonToken endToken = parser.nextToken();
            if (endToken != JsonToken.END_ARRAY)
            {
                throw JsonMappingException.from(parser, "Expected array date shape [yyyy, mm, dd]");
            }

            return LocalDate.of(year, month, day);
        }

        if (token == JsonToken.VALUE_NULL)
        {
            return null;
        }

        throw JsonMappingException.from(parser, "Expected date string or [yyyy, mm, dd] array");
    }
}
