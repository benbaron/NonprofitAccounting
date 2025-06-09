package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.InventoryItem; // Correct import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryService manages inventory items for the nonprofit bookkeeping system.
 * It maintains an in-memory map of inventory items, keyed by their unique ID,
 * and provides methods to list, add, update, delete, and manage these items,
 * including a placeholder for applying depreciation.
 */
public class InventoryService {

    /** In-memory map to store {@link InventoryItem} objects, keyed by their unique ID. */
    private final Map<String, InventoryItem> items;

    /**
     * Constructs an {@code InventoryService} and initializes an empty inventory map.
     * Optionally, sample data can be pre-populated here during development or testing.
     */
    public InventoryService() {
        this.items = new HashMap<>();
        // Optionally pre-populate with sample data:
        // addItem(new InventoryItem("I001", "Item A", new BigDecimal("100"), "2023-01-01", 5)); // Example with BigDecimal
        // addItem(new InventoryItem("I002", "Item B", new BigDecimal("50"), "2023-02-01", 3));
    }

    /**
     * Retrieves a list of all inventory items currently managed by this service.
     *
     * @return A new {@link ArrayList} containing all {@link InventoryItem} objects.
     *         This is a copy, so modifications to the returned list will not affect internal storage.
     *         Returns an empty list if no items are present.
     */
    public List<InventoryItem> listItems() {
        return new ArrayList<>(this.items.values());
    }

    /**
     * Adds a new inventory item to the service's collection.
     * If the provided {@code item} is null or its ID (retrieved by {@link InventoryItem#getId()}) is null,
     * the item is not added. If an item with the same ID already exists, it will be overwritten.
     *
     * @param item The {@link InventoryItem} to add.
     */
    public void addItem(InventoryItem item) {
        if (item != null && item.getId() != null) {
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
    public void updateItem(InventoryItem item) {
        if (item != null && item.getId() != null) {
            if (this.items.containsKey(item.getId())) {
                this.items.put(item.getId(), item);
            }
            // Else: Consider logging or throwing an exception if item to update is not found,
            // depending on desired behavior.
        }
    }

    /**
     * Deletes an inventory item from the collection based on its ID.
     * If the provided {@code id} is null, or if no item with that ID exists, no action is taken.
     *
     * @param id The unique identifier (ID) of the inventory item to delete.
     */
    public void deleteItem(String id) {
        if (id != null) {
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
    public void applyYearlyDepreciation() {
        // TODO: Implement depreciation logic for items that are depreciable.
        // This might involve iterating through items, checking their depreciation rate,
        // and updating their current value or accumulated depreciation.
        System.out.println("applyYearlyDepreciation() called - Placeholder");
    }

    /**
     * Clears all items from the inventory managed by this service instance.
     * After this operation, the inventory will be empty.
     */
    public void clearInventory() {
        this.items.clear();
        System.out.println("Inventory cleared."); // Consider using a logger
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
		// TODO Auto-generated method stub
		// This method should be implemented to fetch inventory items
		// (perhaps from the 'items' map if this method were non-static, or another source)
		// and format them as List<String[]>.
		return null;
	}

    // Removed private static inner class InventoryItem
    // Removed old getInventoryItems()
    // Removed old updateInventoryItem(String id, String name, int quantity, double cost)
}
