package nonprofitbookkeeping.ui.actions.scaledger;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the mapping from dropdown labels in the ledger
 * to canonical chart-of-accounts strings.
 *
 * The JSON should look like a flat object:
 *
 * {
 *   "Checking": "I.a Undep. & non-interest cash",
 *   "General Supplies - AR": "19b General Supplies - Activity",
 *   "Occupancy - AR": "21b Occupancy - Activity Rel",
 *   "Asset Movement": "Asset Movement",
 *   "General Fund": "General Fund"
 * }
 *
 * Keys:
 *   EXACT text from the ledger dropdown cells, unchanged.
 *
 * Values:
 *   EXACT canonical strings to attach for reporting / persistence,
 *   also unchanged.
 */
public class ChartTranslationMap
{
    private final Map<String,String> rawToCanonical;

    public ChartTranslationMap(Map<String,String> rawToCanonical)
    {
        // preserve insertion order, punctuation, spacing
        this.rawToCanonical = new LinkedHashMap<>(rawToCanonical);
    }

    /**
     * Lookup canonical value for a ledger category string.
     * Returns null if not found.
     */
    public String translate(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        return rawToCanonical.get(raw);
    }

    public Map<String,String> asMap()
    {
        return Collections.unmodifiableMap(rawToCanonical);
    }

    /**
     * Load a map from disk from a simple JSON object.
     */
    public static ChartTranslationMap fromJsonFile(Path jsonFile) throws IOException
    {
        byte[] bytes = Files.readAllBytes(jsonFile);
        ObjectMapper mapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String,String> parsed = mapper.readValue(bytes, Map.class);

        return new ChartTranslationMap(parsed);
    }
}
