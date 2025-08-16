package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.reports.datasource.scareports.DonationRow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing {@link DonationRow} instances. Rows are kept in-memory
 * and persisted as JSON in the company directory, mirroring the pattern used
 * by {@code InventoryService} prior to the JPA migration.
 */
public class DonationRowService {

    private static final Logger LOGGER = Logger.getLogger(DonationRowService.class.getName());
    private static final String FILENAME = "donation_rows.json";

    /** In-memory list of rows. */
    private final List<DonationRow> rows = new ArrayList<>();

    private final ObjectMapper mapper;

    public DonationRowService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** Return a copy of all donation rows. */
    public List<DonationRow> listRows() {
        return new ArrayList<>(this.rows);
    }

    /** Add a new row. */
    public void addRow(DonationRow row) {
        if (row != null) {
            this.rows.add(row);
        }
    }

    /** Remove an existing row. */
    public void deleteRow(DonationRow row) {
        this.rows.remove(row);
    }

    /** Save rows to JSON file within given directory. */
    public void saveRows(File companyDirectory) throws IOException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, FILENAME);
        try {
            this.mapper.writeValue(target, this.rows);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to save donation rows to " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }

    /** Load rows from JSON file within given directory. */
    public void loadRows(File companyDirectory) throws IOException {
        this.rows.clear();
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, FILENAME);
        if (!target.exists() || target.length() == 0) {
            return; // nothing to load
        }
        CollectionType listType = this.mapper.getTypeFactory()
                .constructCollectionType(List.class, DonationRow.class);
        try {
            this.rows.addAll(this.mapper.readValue(target, listType));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load donation rows from " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }
}

