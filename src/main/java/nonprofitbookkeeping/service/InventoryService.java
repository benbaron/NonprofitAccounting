
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.InventoryItem; // Correct import
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.persistence.DocumentRepository;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * InventoryService manages inventory items for the nonprofit bookkeeping system.
 * It maintains an in-memory map of inventory items, keyed by their unique ID,
 * and provides methods to list, add, update, delete, and manage these items,
 * including a placeholder for applying depreciation.
 */
public class InventoryService
{
	
        /** Logger instance for this service. */
        private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());
	
        /** Database document name for storing inventory data. */
        private static final String DOCUMENT_NAME = "inventory";
        private static final ObjectMapper MAPPER = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        private static final CollectionType LIST_TYPE =
                MAPPER.getTypeFactory().constructCollectionType(List.class, InventoryItem.class);
	
        /** Shared in-memory map to store {@link InventoryItem} objects, keyed by their unique ID. */
        private static final Map<String, InventoryItem> ITEMS = new HashMap<>();
        private final Map<String, InventoryItem> items;
	
	/**
	 * Constructs an {@code InventoryService} and initializes an empty inventory map.
	 * Optionally, sample data can be pre-populated here during development or testing.
	 */
        public InventoryService()
        {
                this.items = ITEMS;
                // Optionally pre-populate with sample data:
                // addItem(new InventoryItem("I001", "Item A", new BigDecimal("100"),
                // "2023-01-01", 5)); // Example with BigDecimal
		// addItem(new InventoryItem("I002", "Item B", new BigDecimal("50"),
		// "2023-02-01", 3));
	}
	
	/**
	 * Retrieves a list of all inventory items currently managed by this service.
	 *
	 * @return A new {@link ArrayList} containing all {@link InventoryItem} objects.
	 *         This is a copy, so modifications to the returned list will not affect internal storage.
	 *         Returns an empty list if no items are present.
	 */
	public List<InventoryItem> listItems()
	{
		return new ArrayList<>(this.items.values());
	}
	
	/**
	 * Adds a new inventory item to the service's collection.
	 * If the provided {@code item} is null or its ID (retrieved by {@link InventoryItem#getId()}) is null,
	 * the item is not added. If an item with the same ID already exists, it will be overwritten.
	 *
	 * @param item The {@link InventoryItem} to add.
	 */
	public void addItem(InventoryItem item)
	{
		
		if (item != null && item.getId() != null)
		{
			this.items.put(item.getId(), item);
		}
		
	}
	
	/**
	 * Updates an existing inventory item in the collection.
	 * The item is identified by its ID. If the provided {@code item} is null, its ID is null,
	 * or if no item with that ID exists in the inventory, no action is taken.
	 *
	 * @param item The {@link InventoryItem} containing the updated information.
	 *             The item's ID is used to find the existing item to update.
	 */
	public void updateItem(InventoryItem item)
	{
		
		if (item != null && item.getId() != null)
		{
			
			if (this.items.containsKey(item.getId()))
			{
				this.items.put(item.getId(), item);
			}
			
			// Else: Consider logging or throwing an exception if item to update is not
			// found,
			// depending on desired behavior.
		}
		
	}
	
	/**
	 * Deletes an inventory item from the collection based on its ID.
	 * If the provided {@code id} is null, or if no item with that ID exists, no action is taken.
	 *
	 * @param id The unique identifier (ID) of the inventory item to delete.
	 */
	public void deleteItem(String id)
	{
		
		if (id != null)
		{
			this.items.remove(id);
		}
		
	}
	
	/**
	 * Applies yearly depreciation to all applicable inventory items.
	 * <p>
	 * Note: This is currently a placeholder method (stub) and needs to be implemented.
	 * The implementation should iterate through items, check their depreciation method and rate,
	 * calculate depreciation, and update their accumulated depreciation and net book value.
	 * </p>
	 */
	public void applyYearlyDepreciation()
	{
		
                for (InventoryItem item : this.items.values())
                {

                        if (item.getCost() == null || item.getLifeYears() <= 0)
                        {
                                continue; // insufficient data
                        }

                        String method = item.getDepreciationMethod();
                        if (method == null || !"STRAIGHT-LINE".equalsIgnoreCase(method))
                        {
                                continue;
                        }

                        BigDecimal rate = item.getDepreciationRate();

                        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0)
                        {
                                continue;
                        }

                        BigDecimal yearly = item.getCost().multiply(rate);
                        BigDecimal current = item.getAccumulatedDepreciation();

                        if (current == null)
                        {
                                current = BigDecimal.ZERO;
                        }

                        item.withAccumDep(current.add(yearly));
                }
		
	}
	
	/**
	 * Clears all items from the inventory managed by this service instance.
	 * After this operation, the inventory will be empty.
	 */
	public void clearInventory()
	{
		this.items.clear();
		LOGGER.info("Inventory cleared.");
	}
	
        /**
         * Saves all inventory items to the database.
         *
         * @param companyDirectory unused but preserved for backwards compatibility
         * @throws IOException if the database write fails
         */
        public void saveItems(File companyDirectory) throws IOException
        {

                try
                {
                        String payload = MAPPER.writeValueAsString(listItems());
                        new DocumentRepository().upsert(DOCUMENT_NAME, payload);
                        LOGGER.info("Inventory saved to database document '" + DOCUMENT_NAME + "'.");
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save inventory to database", e);
                }

        }

        /**
         * Loads inventory items from the database.
         *
         * @param companyDirectory unused but preserved for backwards compatibility
         * @throws IOException if the database read fails
         */
        public void loadItems(File companyDirectory) throws IOException
        {
                Map<String, InventoryItem> loaded = new HashMap<>();

                try
                {
                        Optional<String> payload = new DocumentRepository().find(DOCUMENT_NAME);

                        if (payload.isPresent())
                        {
                                try
                                {
                                        List<InventoryItem> decoded = MAPPER.readValue(payload.get(), LIST_TYPE);

                                        for (InventoryItem item : decoded)
                                        {
                                                if (item.getId() != null)
                                                {
                                                        loaded.put(item.getId(), item);
                                                }
                                        }

                                        LOGGER.info("Inventory loaded from database document '" + DOCUMENT_NAME
                                                + "'.");
                                }
                                catch (IOException ex)
                                {
                                        LOGGER.log(Level.SEVERE,
                                                "Failed to deserialize inventory JSON from database", ex);
                                        return;
                                }
                        }
                        else
                        {
                                this.items.clear();
                                return;
                        }
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load inventory from database", e);
                }

                this.items.clear();
                this.items.putAll(loaded);

        }
	
	/**
	 * Retrieves a list of inventory items, formatted as arrays of strings.
	 * Note: This is a stub implementation and currently returns null.
	 * The intended structure of the string arrays (e.g., which fields are included and in what order)
	 * is not defined by the stub.
	 *
	 * @return A list of string arrays, where each array represents an inventory item's data,
	 *         or null if the implementation is not complete.
	 */
        public static List<String[]> getInventoryItems()
        {
                InventoryService service = new InventoryService();

                if (Database.isInitialized() && service.items.isEmpty())
                {
                        try
                        {
                                service.loadItems(null);
                        }
                        catch (IOException ex)
                        {
                                LOGGER.log(Level.SEVERE, "Failed to load inventory from database", ex);
                        }
                }

                List<String[]> rows = new ArrayList<>();

                for (InventoryItem item : service.listItems())
                {
                        BigDecimal costValue = item.getCost();
                        String cost = costValue == null ? "0.00" :
                                costValue.setScale(2, RoundingMode.HALF_UP).toString();
                        rows.add(new String[]
                        { item.getId(), item.getName(), cost });
                }

                return rows;
        }
	
	// Removed private static inner class InventoryItem
	// Removed old getInventoryItems()
	// Removed old updateInventoryItem(String id, String name, int quantity, double
	// cost)
}
