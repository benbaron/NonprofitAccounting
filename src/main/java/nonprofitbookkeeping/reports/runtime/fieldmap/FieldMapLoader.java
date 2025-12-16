package nonprofitbookkeeping.reports.runtime.fieldmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a FieldMap from a fieldmap CSV resource.
 * Supports both 5-column (no dbExpr) and 6-column (with dbExpr) formats.
 */
public final class FieldMapLoader
{
    private FieldMapLoader()
    {
        // utility class
    }

    public static FieldMap loadFromResource(String resourcePath) throws IOException
    {
        if (resourcePath == null)
        {
            throw new IllegalArgumentException("resourcePath must not be null");
        }

        String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        InputStream in = FieldMapLoader.class.getResourceAsStream(normalized);
        if (in == null)
        {
            throw new IOException("Resource not found: " + normalized);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8)))
        {
            return parse(reader);
        }
    }

    private static FieldMap parse(BufferedReader reader) throws IOException
    {
        List<FieldMapEntry> entries = new ArrayList<>();
        String line;
        boolean first = true;

        while ((line = reader.readLine()) != null)
        {
            if (first)
            {
                first = false;
                String lower = line.toLowerCase();
                if (lower.startsWith("sheetname,"))
                {
                    // Skip header
                    continue;
                }
            }

            if (line.trim().isEmpty())
            {
                continue;
            }

            List<String> cols = parseCsvLine(line);
            if (cols.size() < 5)
            {
                // not enough columns, skip
                continue;
            }

            String sheetName = cols.get(0);
            String cellRef = cols.get(1);
            String fieldName = cols.get(2);
            String javaType = cols.get(3);
            String excelFormat = cols.get(4);
            String dbExpr = (cols.size() >= 6) ? cols.get(5) : null;

            entries.add(new FieldMapEntry(sheetName, cellRef, fieldName,
                    javaType, excelFormat, dbExpr));
        }

        return new FieldMap(entries);
    }

    /**
     * Minimal CSV parser supporting quotes and doubled quotes.
     */
    private static List<String> parseCsvLine(String line)
    {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (inQuotes)
            {
                if (c == '"')
                {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"')
                    {
                        // Escaped quote
                        sb.append('"');
                        i++;
                    }
                    else
                    {
                        inQuotes = false;
                    }
                }
                else
                {
                    sb.append(c);
                }
            }
            else
            {
                if (c == '"')
                {
                    inQuotes = true;
                }
                else if (c == ',')
                {
                    result.add(sb.toString());
                    sb.setLength(0);
                }
                else
                {
                    sb.append(c);
                }
            }
        }

        result.add(sb.toString());
        return result;
    }
}