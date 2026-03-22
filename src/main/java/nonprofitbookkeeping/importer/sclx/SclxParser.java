package nonprofitbookkeeping.importer.sclx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson-based parser for SCLX documents.
 */
public class SclxParser
{
    private final ObjectMapper objectMapper;

    public SclxParser()
    {
        this(buildDefaultMapper());
    }

    public SclxParser(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public SclxDocument parse(Path path)
    {
        try (InputStream in = Files.newInputStream(path))
        {
            return parse(in);
        }
        catch (IOException ex)
        {
            throw new SclxImportException("Failed to read SCLX file: " + path, ex);
        }
    }

    public SclxDocument parse(InputStream inputStream)
    {
        try
        {
            return objectMapper.readValue(inputStream, SclxDocument.class);
        }
        catch (IOException ex)
        {
            throw new SclxImportException("Failed to parse SCLX JSON.", ex);
        }
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    public static ObjectMapper buildDefaultMapper()
    {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .build();
    }
}
