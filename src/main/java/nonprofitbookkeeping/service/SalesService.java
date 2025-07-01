package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.model.SaleRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link SaleRecord} objects.
 */
public class SalesService {
    /** Shared list storing sale records across service instances. */
    private static final List<SaleRecord> SHARED_SALES = new ArrayList<>();
    /** Logger instance for this service. */
    private static final Logger LOGGER = Logger.getLogger(SalesService.class.getName());
    /** Standard filename for storing sales data. */
    private static final String SALES_FILENAME = "sales.json";

    /** In-memory list of sales. */
    private final List<SaleRecord> sales;

    public SalesService() {
        this.sales = SHARED_SALES;
    }

    /** Returns a copy of all sales. */
    public List<SaleRecord> listSales() {
        return new ArrayList<>(this.sales);
    }

    /** Adds a sale record. */
    public void addSale(SaleRecord sale) {
        if (sale != null) {
            this.sales.add(sale);
        }
    }

    /** Removes a sale by id. */
    public boolean removeSale(String id) {
        return this.sales.removeIf(s -> id != null && id.equals(s.getId()));
    }

    /** Clears all sales. */
    public void clear() { this.sales.clear(); }

    /** Saves sales to JSON in the company directory. */
    public void saveSales(File companyDirectory) throws IOException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, SALES_FILENAME);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(target, listSales());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to save sales to " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }

    /** Loads sales from JSON in the company directory. */
    public void loadSales(File companyDirectory) throws IOException {
        this.sales.clear();
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }
        File target = new File(companyDirectory, SALES_FILENAME);
        if (!target.exists() || target.length() == 0) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, SaleRecord.class);
        try {
            List<SaleRecord> loaded = mapper.readValue(target, listType);
            this.sales.addAll(loaded);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load sales from " + target.getAbsolutePath(), ex);
            throw ex;
        }
    }
}

