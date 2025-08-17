package nonprofitbookkeeping.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory service for associating ledger entries with
 * supplemental record identifiers and retrieving supplemental data
 * required by SCA reports. This implementation is intentionally light
 * weight and can be replaced by a persistent solution in the future.
 */
public class SupplementalRecordService
{
        /** Mapping of entry keys to supplemental record identifiers. */
        private final Map<String, String> entryToRecord = new ConcurrentHashMap<>();

        /** Supplemental detail data keyed by supplemental record id. */
        private final Map<String, Map<String, String>> recordData = new ConcurrentHashMap<>();

        /**
         * Persist the association between a ledger entry and a supplemental
         * record identifier.
         *
         * @param entryKey unique key for the ledger entry
         * @param recordId identifier for the supplemental record
         */
        public void linkEntry(String entryKey, String recordId)
        {
                if (entryKey != null && recordId != null)
                {
                        this.entryToRecord.put(entryKey, recordId);
                }
        }

        /**
         * Retrieve the supplemental record identifier for a given ledger entry.
         *
         * @param entryKey unique key for the ledger entry
         * @return the associated record identifier or {@code null}
         */
        public String getRecordId(String entryKey)
        {
                return this.entryToRecord.get(entryKey);
        }

        /**
         * Store arbitrary supplemental data for a record identifier. This is
         * primarily used by reporting services when merging supplemental data
         * into generated scareports.
         *
         * @param recordId supplemental record identifier
         * @param data     key/value data describing the supplemental record
         */
        public void saveSupplementalData(String recordId, Map<String, String> data)
        {
                if (recordId != null && data != null)
                {
                        this.recordData.put(recordId, Map.copyOf(data));
                }
        }

        /**
         * Retrieve stored supplemental data for a record identifier.
         *
         * @param recordId supplemental record identifier
         * @return stored data or an empty map if none is found
         */
        public Map<String, String> getSupplementalData(String recordId)
        {
                return this.recordData.getOrDefault(recordId, Map.of());
        }
}

