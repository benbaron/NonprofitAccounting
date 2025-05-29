
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Note: nonprofitbookkeeping.model.InventoryItem is implicitly imported by usage because it's in the same package (oops, no it's not, it's in .model, so it would be imported)
// For clarity, explicit import is better if not in same package. Assuming it's handled by IDE or build tools.
// import nonprofitbookkeeping.model.InventoryItem; 

/**
 * Manages inventory items for the nonprofit bookkeeping system.
 * This service provides an in-memory storage solution for inventory items
 * and includes methods for item retrieval, addition, modification, deletion,
 * and depreciation.
 * 
 * The inventory is stored as a map where keys are item IDs (String) and
 * values are {@link nonprofitbookkeeping.model.InventoryItem} objects.
 */
public class InventoryService
{
	
	/**
	 * Internal in-memory map to store inventory items.
	 * Key: String - The unique identifier of the inventory item.
	 * Value: {@link nonprofitbookkeeping.model.InventoryItem} - The inventory item object.
	 */
	private static Map<String, nonprofitbookkeeping.model.InventoryItem> inventory;
	
	/**
	 * Constructs a new InventoryService, initializing an empty inventory map.
	 * This setup ensures that the service is ready to manage items upon instantiation.
	 */
	public InventoryService()
	{
		// Initialize map for nonprofitbookkeeping.model.InventoryItem
		// Ensures that if multiple InventoryService instances are created, they share the same static map.
		// The map is initialized here if not already done by a previous instance or cleared.
		if (InventoryService.inventory == null) {
			InventoryService.inventory = new HashMap<String, nonprofitbookkeeping.model.InventoryItem>();
		}
		
		// Sample data would need to be of type nonprofitbookkeeping.model.InventoryItem
//		inventory.put("I001", new nonprofitbookkeeping.model.InventoryItem("I001", "Item A", BigDecimal.valueOf(2.50), "someDate", 5));
//		inventory.put("I002", new nonprofitbookkeeping.model.InventoryItem("I002", "Item B", BigDecimal.valueOf(7.99), "anotherDate", 3));
	}
	
	/**
	 * Retrieves a list of all inventory items, formatted as String arrays for display purposes.
	 * Each String array contains the item's ID, name, and formatted original cost.
	 * The cost is formatted to two decimal places (e.g., "123.45"). If the cost is null,
	 * it defaults to "0.00".
	 *
	 * @return A {@code List<String[]>} where each array represents an inventory item
	 *         with elements: {@code [ID, Name, FormattedCost]}. Returns an empty list
	 *         if the inventory is null or empty.
	 */
	public static List<String[]> getInventoryItems()
	{
		List<String[]> list = new ArrayList<>();
		if (inventory == null) {
			return list; // Or Collections.emptyList();
		}
		for (nonprofitbookkeeping.model.InventoryItem item : inventory.values())
		{
			String costStr = "0.00";
			if (item.getCost() != null) {
				costStr = String.format("%.2f", item.getCost());
			}
			list.add(new String[]
			{
				item.getId(),
				item.getName(),
				costStr
			});
		}
		
		return list;
	}
	
	/**
	/**
	 * Deletes an inventory item from the system based on its ID.
	 * If the provided ID is null or the inventory map is not initialized,
	 * this method does nothing. If no item with the given ID is found,
	 * the inventory remains unchanged.
	 *
	 * @param id The unique identifier (String) of the inventory item to be deleted.
	 */
	public void deleteItem(String id)
	{
		if (id != null && inventory != null) {
			inventory.remove(id);
		}
	}

	/**
	 * Applies yearly depreciation to all applicable inventory items.
	 * This method iterates through each item in the inventory and applies depreciation
	 * based on the item's specified {@code depreciationRate} and {@code depreciationMethod}.
	 * 
	 * Currently, only the "Straight-Line" depreciation method is supported.
	 * For an item to be depreciated:
	 * <ul>
	 *   <li>Its {@code depreciationRate} must be non-null and positive.</li>
	 *   <li>Its {@code originalCost} (obtained via {@code getCost()}) must be non-null and positive.</li>
	 *   <li>Its {@code depreciationMethod} must be non-null and case-insensitively equal to "Straight-Line".</li>
	 * </ul>
	 * If these conditions are met, the depreciation amount is calculated as {@code originalCost * depreciationRate}.
	 * This amount is then added to the item's {@code accumulatedDepreciation}.
	 * The item's {@code netValue} is updated to {@code originalCost - accumulatedDepreciation}.
	 * The item's original cost ({@code cost} field) is preserved and not altered by this process.
	 * 
	 * If the inventory map is null, this method does nothing.
	 */
	public void applyYearlyDepreciation()
	{
		if (inventory == null) {
			return;
		}

		for (nonprofitbookkeeping.model.InventoryItem item : inventory.values()) {
			BigDecimal rate = item.getDepreciationRate();
			String method = item.getDepreciationMethod();
			BigDecimal originalCost = item.getCost();

			// Check for valid depreciation parameters
			if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0 &&
			    originalCost != null && originalCost.compareTo(BigDecimal.ZERO) > 0 &&
			    method != null && "Straight-Line".equalsIgnoreCase(method)) {

				BigDecimal depreciationAmount = originalCost.multiply(rate);
				
				BigDecimal currentAccDep = item.getAccumulatedDepreciation();
				if (currentAccDep == null) {
					currentAccDep = BigDecimal.ZERO;
				}
				BigDecimal newAccDep = currentAccDep.add(depreciationAmount);
				
				// Ensure accumulated depreciation does not exceed original cost
				// though standard accounting might allow net value to be <= 0.
				// For simplicity here, we just set it.
				item.setAccumulatedDepreciation(newAccDep);
				
				// Update net book value
				// Cost remains original cost, netValue is originalCost - newAccDep
				item.setNetValue(originalCost.subtract(newAccDep));
				
				// Note: The item.getCost() (original cost) is NOT changed.
			}
			// Else, if parameters are missing or method is not "Straight-Line", do nothing for this item.
		}
	}

	/**
	/**
	 * Adds a new inventory item to the system or overwrites an existing one if an item
	 * with the same ID is already present.
	 * 
	 * If the provided {@code item} or its ID ({@code item.getId()}) is null,
	 * the method does nothing. If the inventory map has not been initialized (which
	 * should not occur if the constructor is used), it will be initialized.
	 *
	 * @param item The {@link nonprofitbookkeeping.model.InventoryItem} to be added or used for overwriting.
	 *             Must not be null and must have a non-null ID.
	 */
	public void addItem(nonprofitbookkeeping.model.InventoryItem item)
	{
		if (item == null || item.getId() == null) {
			// If item or its ID is null, do nothing.
			return;
		}
		if (inventory == null) {
			// Should not happen if constructor is called, but defensive.
			inventory = new HashMap<String, nonprofitbookkeeping.model.InventoryItem>();
		}
		inventory.put(item.getId(), item); // Directly use the model item
	}

	/**
	/**
	 * Updates an existing inventory item in the system using the provided item object.
	 * The item to be updated is identified by {@code item.getId()}.
	 * 
	 * If the provided {@code item}, its ID ({@code item.getId()}), or the ID trimmed is blank,
	 * or if the inventory map is null, the method does nothing.
	 * If no item with the given ID is found in the inventory, this method also does nothing.
	 * Otherwise, the existing item in the map is replaced with the provided {@code item} object,
	 * effectively updating all its fields.
	 *
	 * @param item The {@link nonprofitbookkeeping.model.InventoryItem} containing the updated information.
	 *             Must not be null, and its ID must be non-null and not blank.
	 */
	public void updateItem(nonprofitbookkeeping.model.InventoryItem item)
	{
		if (item == null || item.getId() == null || item.getId().trim().isEmpty() || inventory == null) {
			// If item, its ID (null or blank), or inventory is null, do nothing.
			return;
		}
		
		// Check if the item exists before attempting to update
		if (inventory.containsKey(item.getId())) {
			// Item exists, update it by replacing the old item with the new one.
			inventory.put(item.getId(), item);
		}
		// If item not found, do nothing.
	}

	/**
	 * Retrieves a list of all inventory items currently stored in the system.
	 * 
	 * If the inventory map is null or empty, this method returns an empty list
	 * (specifically, {@code Collections.emptyList()}). Otherwise, it returns a new
	 * {@code ArrayList} containing all {@link nonprofitbookkeeping.model.InventoryItem}
	 * objects from the inventory.
	 *
	 * @return A {@code List<nonprofitbookkeeping.model.InventoryItem>} containing all items,
	 *         or an empty list if no items are present.
	 */
	public List<nonprofitbookkeeping.model.InventoryItem> listItems()
	{
		if (inventory == null || inventory.isEmpty()) {
			return Collections.emptyList();
		}
		return new ArrayList<>(inventory.values());
	}

	/**
	 * Clears all items from the inventory.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * between test executions.
	 */
	public static void clearInventory() {
		if (inventory != null) {
			inventory.clear();
		} else {
			// If inventory was null, initialize it to ensure it's not null for subsequent operations.
			inventory = new HashMap<String, nonprofitbookkeeping.model.InventoryItem>();
		}
	}
	
}
