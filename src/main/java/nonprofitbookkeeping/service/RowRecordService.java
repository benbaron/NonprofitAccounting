package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.model.reports.ReportRowRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for persisting {@link ReportRowRecord} instances. Records are kept
 * in memory and may be saved to / loaded from a JSON file inside the company
 * directory. The design mirrors simple services such as
 * {@code DonationRowService}.
 */
public class RowRecordService {

    private static final Logger LOGGER = Logger.getLogger(RowRecordService.class.getName());
    private static final String FILENAME = "row_records.json";

    /** In-memory list of row records. */
    private final List<ReportRowRecord> records = new ArrayList<>();

    private final ObjectMapper mapper;

    public RowRecordService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** Returns a copy of all records. */
    public List<ReportRowRecord> listRecords() {
        return new ArrayList<>(this.records);
    }

    /** Adds a new record to the collection. */
    public void addRecord(ReportRowRecord record) {
        if (record != null) {
            this.records.add(record);
        }
    }

    /** Removes all records associated with the given transaction id. */
    public void removeRecordsForTransaction(String transactionId) {
        if (transactionId != null) {
            this.records.removeIf(r -> transactionId.equals(r.getTransactionId()));
        }
    }

    /** Clears all records from memory. */
    public void clear() {
        this.records.clear();
    }

    /** Saves the current records to {@code row_records.json} within the company directory. */
    public void saveRecords(File companyDirectory) throws IOException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, FILENAME);
        try {
            this.mapper.writeValue(target, this.records);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                    "Failed to save row records to " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }

    /** Loads records from {@code row_records.json} within the company directory. */
    public void loadRecords(File companyDirectory) throws IOException {
        this.records.clear();
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, FILENAME);
        if (!target.exists() || target.length() == 0) {
            return; // nothing to load
        }
        CollectionType listType = this.mapper.getTypeFactory()
                .constructCollectionType(List.class, ReportRowRecord.class);
        try {
            this.records.addAll(this.mapper.readValue(target, listType));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                    "Failed to load row records from " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }
}

