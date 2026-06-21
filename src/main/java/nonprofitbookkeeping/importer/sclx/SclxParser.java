package nonprofitbookkeeping.importer.sclx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jackson-based parser for SCLX documents.
 */
public class SclxParser
{
    private static final Logger log = LoggerFactory.getLogger(SclxParser.class);
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
        log.debug("Parsing SCLX file from path={}", path);
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
        Objects.requireNonNull(inputStream, "inputStream");
        try
        {
            SclxDocument document = this.objectMapper.readValue(inputStream, SclxDocument.class);
            log.debug(
                "Parsed SCLX envelope format={}, version={}, hasOrganization={}, hasReportingPeriod={}, transactions={}",
                document.format(),
                document.version(),
                document.organization() != null,
                document.reportingPeriod() != null,
                document.transactions() == null ? 0 : document.transactions().size());
            return document;
        }
        catch (IOException ex)
        {
            throw new SclxImportException("Failed to parse SCLX JSON.", ex);
        }
    }

    public SclxDocument parse(String jsonSource)
    {
        Objects.requireNonNull(jsonSource, "jsonSource");
        try
        {
            SclxDocument document = this.objectMapper.readValue(jsonSource, SclxDocument.class);
            log.debug(
                "Parsed SCLX envelope format={}, version={}, hasOrganization={}, hasReportingPeriod={}, transactions={}",
                document.format(),
                document.version(),
                document.organization() != null,
                document.reportingPeriod() != null,
                document.transactions() == null ? 0 : document.transactions().size());
            return document;
        }
        catch (IOException ex)
        {
            throw new SclxImportException("Failed to parse SCLX JSON.", ex);
        }
    }

    public ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
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
