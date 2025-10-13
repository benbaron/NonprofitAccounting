
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.InventoryItem; // Correct import
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import nonprofitbookkeeping.persistence.JsonStorageRepository;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	/** Shared in-memory map to store {@link InventoryItem} objects across service instances. */
	private static final Map<String, InventoryItem> SHARED_ITEMS = new HashMap<>();
	
	/** Logger instance for this service. */
	private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());
	
        /** Storage key for inventory payload inside the database. */
        private static final String STORAGE_KEY = "inventory";
	
	/** In-memory map to store {@link InventoryItem} objects, keyed by their unique ID. */
	private final Map<String, InventoryItem> items;
	
	/**
	 * Constructs an {@code InventoryService} and initializes an empty inventory map.
	 * Optionally, sample data can be pre-populated here during development or testing.
	 */
	public InventoryService()
	{
		this.items = SHARED_ITEMS;
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
			
			BigDecimal rate = item.getDepreciationRate();
			BigDecimal yearly;
			
			if (rate != null)
			{
				yearly = item.getCost().multiply(rate);
			}
			else
			{
				yearly = item.getCost().divide(BigDecimal.valueOf(item.getLifeYears()), 2,
					RoundingMode.HALF_UP);
			}
			
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
         * Persists the current inventory items into the shared H2 database.
         *
         * @param companyDirectory legacy parameter retained for compatibility
         * @throws IOException if serialization fails or the database cannot be updated
         */
        public void saveItems(File companyDirectory) throws IOException
        {

                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                try
                {
                        String payload = mapper.writeValueAsString(listItems());
                        new JsonStorageRepository().save(STORAGE_KEY, payload);
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save inventory to H2 database", e);
                }

        }
	
        /**
         * Loads inventory items previously stored in the database.
         * Existing in-memory items are cleared before loading new ones.
         * If no payload is present, the inventory remains empty.
         *
         * @param companyDirectory legacy parameter retained for compatibility
         * @throws IOException if fetching from the database fails
         */
	public void loadItems(File companyDirectory) throws IOException
	{
		this.items.clear();
		
                ObjectMapper mapper = new ObjectMapper();
                CollectionType listType =
                        mapper.getTypeFactory().constructCollectionType(List.class, InventoryItem.class);

                try
                {
                        new JsonStorageRepository().load(STORAGE_KEY)
                                .filter(payload -> !payload.isBlank())
                                .ifPresent(payload -> {
                                        try
                                        {
                                                List<InventoryItem> loaded = mapper.readValue(payload, listType);

                                                for (InventoryItem item : loaded)
                                                {

                                                        if (item.getId() != null)
                                                        {
                                                                this.items.put(item.getId(), item);
                                                        }

                                                }
                                        }
                                        catch (IOException ex)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Failed to parse inventory payload from H2 database.", ex);
                                        }
                                });

                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load inventory from H2 database", e);
                }

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
		List<String[]> rows = new ArrayList<>();
		
		for (InventoryItem item : SHARED_ITEMS.values())
		{
			String cost = item.getCost() == null ? "0.00" :
				item.getCost().setScale(2, RoundingMode.HALF_UP).toString();
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
