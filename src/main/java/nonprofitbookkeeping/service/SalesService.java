
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.model.SaleRecord;
import nonprofitbookkeeping.persistence.JsonStorageRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link SaleRecord} objects.
 */
public class SalesService
{
	/** Shared list storing sale records across service instances. */
	private static final List<SaleRecord> SHARED_SALES = new ArrayList<>();
	/** Logger instance for this service. */
	private static final Logger LOGGER = Logger.getLogger(SalesService.class.getName());
        /** Storage key used for the sales payload inside the database. */
        private static final String STORAGE_KEY = "sales";
	
	/** In-memory list of sales. */
	private final List<SaleRecord> sales;
	
	public SalesService()
	{
		this.sales = SHARED_SALES;
	}
	
	/** Returns a copy of all sales. */
	public List<SaleRecord> listSales()
	{
		return new ArrayList<>(this.sales);
	}
	
	/** Adds a sale record. */
	public void addSale(SaleRecord sale)
	{
		
		if (sale != null)
		{
			this.sales.add(sale);
		}
		
	}
	
	/** Removes a sale by id. */
	public boolean removeSale(String id)
	{
		return this.sales.removeIf(s -> id != null && id.equals(s.getId()));
	}
	
	/** Clears all sales. */
	public void clear()
	{
		this.sales.clear();
	}
	
        /** Saves sales to the shared H2 database. */
        public void saveSales(File companyDirectory) throws IOException
        {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                try
                {
                        String payload = mapper.writeValueAsString(listSales());
                        new JsonStorageRepository().save(STORAGE_KEY, payload);
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save sales to H2 database", e);
                }

        }
	
        /** Loads sales from the shared H2 database. */
	public void loadSales(File companyDirectory) throws IOException
	{
		this.sales.clear();
		
                ObjectMapper mapper = new ObjectMapper();
                CollectionType listType =
                        mapper.getTypeFactory().constructCollectionType(List.class, SaleRecord.class);

                try
                {
                        new JsonStorageRepository().load(STORAGE_KEY)
                                .filter(payload -> !payload.isBlank())
                                .ifPresent(payload -> {
                                        try
                                        {
                                                List<SaleRecord> loaded = mapper.readValue(payload, listType);
                                                this.sales.addAll(loaded);
                                        }
                                        catch (IOException ex)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Failed to parse sales payload from H2 database.", ex);
                                        }
                                });
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load sales from H2 database", e);
                }

        }
	
}

