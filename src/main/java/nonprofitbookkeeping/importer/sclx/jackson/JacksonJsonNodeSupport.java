
package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared helper methods for SCLX Jackson adapters and mappers.
 */
public final class JacksonJsonNodeSupport
{
	
	private JacksonJsonNodeSupport()
	{
	
	}
	
	public static JsonNode readTree(JsonParser parser)
	{
		
		try
		{
			ObjectCodec codec = parser.getCodec();
			return codec.readTree(parser);
		}
		catch (Exception ex)
		{
			throw new IllegalArgumentException("Unable to read JSON tree.", ex);
		}
		
	}
	
	public static String text(JsonNode node, String field)
	{
		JsonNode child = node == null ? null : node.get(field);
		return child == null || child.isNull() ? null : child.asText();
		
	}
	
	public static LocalDate localDate(JsonNode node, String field)
	{
		String value = text(node, field);
		return value == null || value.isBlank() ? null : LocalDate.parse(value);
		
	}
	
	public static OffsetDateTime offsetDateTime(JsonNode node, String field)
	{
		String value = text(node, field);
		return value == null || value.isBlank() ? null :
			OffsetDateTime.parse(value);
		
	}
	
	public static BigDecimal decimal(JsonNode node, String field)
	{
		String value = text(node, field);
		return value == null || value.isBlank() ? null : new BigDecimal(value);
		
	}
	
	public static Integer integer(JsonNode node, String field)
	{
		JsonNode child = node == null ? null : node.get(field);
		
		if (child == null || child.isNull())
		{
			return null;
		}
		
		if (child.isInt() || child.isLong())
		{
			return child.intValue();
		}
		
		String value = child.asText();
		return value == null || value.isBlank() ? null : Integer.valueOf(value);
		
	}
	
	public static boolean bool(JsonNode node, String field,
		boolean defaultValue)
	{
		JsonNode child = node == null ? null : node.get(field);
		return child == null || child.isNull() ? defaultValue :
			child.asBoolean();
		
	}
	
	public static List<String> stringList(JsonNode node, String field)
	{
		JsonNode arr = node == null ? null : node.get(field);
		
		if (arr == null || arr.isNull() || !arr.isArray())
		{
			return List.of();
		}
		
		List<String> result = new ArrayList<>();
		
		for (JsonNode item : arr)
		{
			
			if (!item.isNull())
			{
				result.add(item.asText());
			}
			
		}
		
		return List.copyOf(result);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> objectMap(JsonNode node, String field,
		ObjectMapper mapper)
	{
		JsonNode child = node == null ? null : node.get(field);
		
		if (child == null || child.isNull() || child.isMissingNode())
		{
			return Map.of();
		}
		
		return mapper.convertValue(child, Map.class);
		
	}
	
	public static <E extends Enum<E>> E enumValue(JsonNode node, String field,
		Class<E> enumType)
	{
		String value = text(node, field);
		return value == null ? null : enumValue(value, enumType);
		
	}
	
	public static <E extends Enum<E>> E enumValue(String value,
		Class<E> enumType)
	{
		
		if (value == null || value.isBlank())
		{
			return null;
		}
		
		String normalized = value
			.trim()
			.replace('-', '_')
			.replace(' ', '_')
			.toUpperCase(Locale.ROOT);
		
		return Enum.valueOf(enumType, normalized);
		
	}
	
}
