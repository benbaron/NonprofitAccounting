
package nonprofitbookkeeping.service;

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
	
	// Internal map to store inventory items, keyed by their unique ID.
	private static Map<String, InventoryItem> inventory;
	
	/**
	 * Constructs an InventoryService, initializing an empty inventory.
	 * Optionally, you could pre-populate this map with sample items.
	 */
	public InventoryService()
	{
		InventoryService.inventory = new HashMap<>();
		
		// Optionally pre-populate with sample data:
//		inventory.put("I001", new InventoryItem("I001", "Item A", 100, 2.50));
//		inventory.put("I002", new InventoryItem("I002", "Item B", 50, 7.99));
	}
	
	/**
	 * Retrieves a list of inventory items.
	 * Each inventory item is represented as a String array with columns:
	 * "ID", "Name", "Quantity", "Cost".
	 *
	 * @return a list of String arrays representing inventory items.
	 */
	public static List<String[]> getInventoryItems()
	{
		List<String[]> list = new ArrayList<>();
		
		for (InventoryItem item : inventory.values())
		{
			list.add(new String[]
			{
				item.getId(),
				item.getName(),
				String.valueOf(item.getQuantity()),
				String.format("%.2f", item.getCost())
			});
		}
		
		return list;
	}
	
	/**
	 * Updates an inventory item with the provided details. If the item does not exist,
	 * it is created.
	 *
	 * @param id       the unique identifier of the inventory item.
	 * @param name     the new name for the item.
	 * @param quantity the new quantity.
	 * @param cost     the new cost per unit.
	 * @throws IllegalArgumentException if the id is null or empty.
	 */
	public void updateInventoryItem(String id, String name, int quantity, double cost)
	{
		
		if (id == null || id.trim().isEmpty())
		{
			throw new IllegalArgumentException("Inventory item ID must not be blank.");
		}
		
		InventoryItem item = inventory.get(id);
		
		if (item == null)
		{
			// Create a new item if it doesn't exist.
			item = new InventoryItem(id, name, quantity, cost);
			inventory.put(id, item);
		}
		else
		{
			// Otherwise, update the existing item.
			item.setName(name);
			item.setQuantity(quantity);
			item.setCost(cost);
		}
		
	}
	
	/**
	 * Private inner class representing a single inventory item.
	 */
	private static class InventoryItem
	{
		private String id;
		private String name;
		private int quantity;
		private double cost;
		
		public InventoryItem(String id, String name, int quantity, double cost)
		{
			this.id = id;
			this.name = name;
			this.quantity = quantity;
			this.cost = cost;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public int getQuantity()
		{
			return this.quantity;
		}
		
		public double getCost()
		{
			return this.cost;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
		
		public void setQuantity(int quantity)
		{
			this.quantity = quantity;
		}
		
		public void setCost(double cost)
		{
			this.cost = cost;
		}
		
	}

	/**
	 * @param id
	 */
	public void deleteItem(String id)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void applyYearlyDepreciation()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param item
	 */
	public void addItem(nonprofitbookkeeping.model.InventoryItem item)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param item
	 */
	public void updateItem(nonprofitbookkeeping.model.InventoryItem item)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public List<nonprofitbookkeeping.model.InventoryItem> listItems()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
