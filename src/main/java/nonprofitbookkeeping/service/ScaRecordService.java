package nonprofitbookkeeping.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.model.ScaRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class responsible for managing {@link ScaRecord} instances. Records
 * are kept in memory and can be persisted to a JSON file in the company
 * directory. The persistence behavior mirrors that of
 * {@link FundAccountingService}.
 */
public class ScaRecordService {

    /** In-memory store of records keyed by record id. */
    @JsonProperty
    private final Map<String, ScaRecord> recordMap;

    /** Logger for diagnostic messages. */
    private static final Logger LOGGER = Logger.getLogger(ScaRecordService.class.getName());

    /** Filename used to persist records. */
    private static final String RECORDS_FILENAME = "sca_records.json";

    /** Creates a new service with an empty record map. */
    public ScaRecordService() {
        this.recordMap = new HashMap<>();
    }

    /** Adds a record to the service. */
    public void addRecord(ScaRecord record) {
        if (record == null || record.getId() == null) {
            throw new IllegalArgumentException("Record and record id must not be null.");
        }
        this.recordMap.put(record.getId(), record);
    }

    /** Removes a record by id. */
    public boolean removeRecord(String id) {
        return id != null && this.recordMap.remove(id) != null;
    }

    /** Returns a copy of all records. */
    public List<ScaRecord> listRecords() {
        return new ArrayList<>(this.recordMap.values());
    }

    /** Clears all records from memory. */
    public void clear() {
        this.recordMap.clear();
    }

    /** Saves records to {@code sca_records.json} inside the given directory. */
    public void saveRecords(File companyDirectory) throws IOException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, RECORDS_FILENAME);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(target, listRecords());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                    "Failed to save SCA records to " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }

    /** Loads records from {@code sca_records.json} inside the given directory. */
    public void loadRecords(File companyDirectory) throws IOException {
        this.recordMap.clear();
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, RECORDS_FILENAME);
        if (!target.exists() || target.length() == 0) {
            return; // nothing to load
        }
        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType =
                mapper.getTypeFactory().constructCollectionType(List.class, ScaRecord.class);
        try {
            List<ScaRecord> loaded = mapper.readValue(target, listType);
            for (ScaRecord r : loaded) {
                if (r.getId() != null) {
                    this.recordMap.put(r.getId(), r);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                    "Failed to load SCA records from " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }
}

