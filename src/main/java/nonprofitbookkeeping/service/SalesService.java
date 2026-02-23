
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.model.SaleRecord;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing {@link SaleRecord} objects.
 */
public class SalesService
{
	/** Logger instance for this service. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SalesService.class);
	/** Database document name for storing sales data. */
	private static final String DOCUMENT_NAME = "sales";
	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);
	private static final CollectionType LIST_TYPE =
		MAPPER.getTypeFactory().constructCollectionType(List.class,
			SaleRecord.class);
	
	/** In-memory list of sales. */
	private final List<SaleRecord> sales;
	
	public SalesService()
	{
		this.sales = new ArrayList<>();
		
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
	
	/** Saves sales to the database. */
	public void saveSales(File companyDirectory) throws IOException
	{
		
		try
		{
			String payload = MAPPER.writeValueAsString(listSales());
			new DocumentRepository().upsert(DOCUMENT_NAME, payload);
			LOGGER.info("Sales saved to database document '{}'.",
				DOCUMENT_NAME);
		}
		catch (SQLException e)
		{
			throw new IOException("Failed to save sales to database", e);
		}
		
	}
	
	/** Loads sales from the database. */
	public void loadSales(File companyDirectory) throws IOException
	{
		this.sales.clear();
		
		try
		{
			new DocumentRepository().find(DOCUMENT_NAME)
				.ifPresent(payload ->
				{
					
					try
					{
						List<SaleRecord> loaded =
							MAPPER.readValue(payload, LIST_TYPE);
						this.sales.addAll(loaded);
						LOGGER.info(
							"Sales loaded from database document '{}'.",
							DOCUMENT_NAME);
					}
					catch (IOException ex)
					{
						LOGGER.error(
							"Failed to deserialize sales JSON from database",
							ex);
					}
					
				});
		}
		catch (SQLException e)
		{
			throw new IOException("Failed to load sales from database", e);
		}
		
	}
	
}
