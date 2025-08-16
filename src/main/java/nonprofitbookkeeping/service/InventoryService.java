
package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.repository.InventoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service layer for {@link InventoryItem} entities using JPA for persistence.
 */
public class InventoryService
{
	
	private final InventoryRepository repository;
	
	public InventoryService()
	{
		EntityManagerFactory emf =
			Persistence.createEntityManagerFactory("nonprofitPU");
		EntityManager em = emf.createEntityManager();
		this.repository = new InventoryRepository(em);
		
	}
	
	/** List all inventory items. */
	public List<InventoryItem> listItems()
	{
		return repository.findAll();
		
	}
	
	/** Add a new item. */
	public void addItem(InventoryItem item)
	{
		repository.save(item);
		
	}
	
	/** Update an existing item. */
	public void updateItem(InventoryItem item)
	{
		repository.save(item);
		
	}
	
	/** Delete an item by id. */
	public void deleteItem(String id)
	{
		repository.delete(id);
		
	}
	
	/** Apply yearly depreciation to all items and persist the updated values. */
	public void applyYearlyDepreciation()
	{
		List<InventoryItem> items = repository.findAll();
		
		for (InventoryItem item : items)
		{
			
			if (item.getCost() == null || item.getLifeYears() <= 0)
			{
				continue;
			}
			
			BigDecimal rate = item.getDepreciationRate();
			BigDecimal yearly;
			
			if (rate != null)
			{
				yearly = item.getCost().multiply(rate);
			}
			else
			{
				yearly = item.getCost().divide(
					BigDecimal.valueOf(item.getLifeYears()), 2,
					RoundingMode.HALF_UP);
			}
			
			BigDecimal current = item.getAccumulatedDepreciation();
			
			if (current == null)
			{
				current = BigDecimal.ZERO;
			}
			
			item.withAccumDep(current.add(yearly));
			repository.save(item);
		}
		
	}
	
	/** Remove all items from the inventory. */
	public void clearInventory()
	{
		
		for (InventoryItem item : repository.findAll())
		{
			repository.delete(item.getId());
		}
		
	}
	
	/** Compatibility stub: explicit save to JSON is no longer required. */
	public void saveItems(java.io.File companyDirectory)
	{
		
		// no-op
	}
	
	/** Compatibility stub: loading from JSON is no longer required. */
	public void loadItems(java.io.File companyDirectory)
	{
		
		// no-op
	}
	
	/**
	 * @return
	 */
	public static List<String[]> getInventoryItems()
	{
		// TODO Auto-generated method stub
		return null;
		
	}
	
}

