
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.InventoryItem; // Correct import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryService manages inventory items for the nonprofit bookkeeping system.
 * It maintains an in-memory map of inventory items and provides methods to retrieve and update them.
 */
public class InventoryService
{
	
	// Instance field to store inventory items, keyed by their unique ID.
	private final Map<String, InventoryItem> items;
	
	/**
	 * Constructs an InventoryService, initializing an empty inventory.
	 */
	public InventoryService()
	{
		this.items = new HashMap<>();
		// Optionally pre-populate with sample data:
		// addItem(new InventoryItem("I001", "Item A", 100, 2.50, "Some Category",
		// "2023-01-01", 0.1));
		// addItem(new InventoryItem("I002", "Item B", 50, 7.99, "Another Category",
		// "2023-02-01", 0.05));
	}
	
	/**
	 * Retrieves a list of all inventory items.
	 *
	 * @return a list of InventoryItem objects.
	 */
	public List<InventoryItem> listItems()
	{
		return new ArrayList<>(this.items.values());
	}
	
	/**
	 * Adds a new inventory item to the collection.
	 * If the item is null or its ID is null, the item is not added.
	 *
	 * @param item the InventoryItem to add.
	 */
	public void addItem(InventoryItem item)
	{
		
		if (item != null && item.getId() != null)
		{
			this.items.put(item.getId(), item);
		}
		
	}
	
	/**
	 * Updates an existing inventory item.
	 * If the item or its ID is null, or if the item does not exist in the inventory,
	 * no action is taken.
	 *
	 * @param item the InventoryItem to update.
	 */
	public void updateItem(InventoryItem item)
	{
		
		if (item != null && item.getId() != null)
		{
			
			if (this.items.containsKey(item.getId()))
			{
				this.items.put(item.getId(), item);
			}
			
			// Else: Could throw an exception or log if item to update is not found.
		}
		
	}
	
	/**
	 * Deletes an inventory item based on its ID.
	 * If the ID is null, no action is taken.
	 *
	 * @param id the unique identifier of the inventory item to delete.
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
	 * (This is a stub and needs actual implementation based on depreciation rules).
	 */
	public void applyYearlyDepreciation()
	{
		// TODO: Implement depreciation logic for items that are depreciable.
		// This might involve iterating through items, checking their depreciation rate,
		// and updating their current value or accumulated depreciation.
		System.out.println("applyYearlyDepreciation() called - Placeholder");
	}
	
	/**
	 * Clears all items from the inventory.
	 * (This is a stub, confirm if static access is intended or instance method)
	 * For now, making it an instance method to clear this service's items.
	 */
	public void clearInventory()
	{
		this.items.clear();
		System.out.println("Inventory cleared.");
	}
	
	/**
	 * @return
	 */
	public List<String[]> getInventoryItems()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	// Removed private static inner class InventoryItem
	// Removed old getInventoryItems()
	// Removed old updateInventoryItem(String id, String name, int quantity, double
	// cost)
}
