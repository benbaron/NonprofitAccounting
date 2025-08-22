package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.InventoryRepository;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for {@link InventoryItem} entities using JPA for persistence.
 */
public class InventoryService
{

        /** List all inventory items. */
        public List<InventoryItem> listItems()
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        return repository.findAll();
                }

        }

        /** Add a new item. */
        public void addItem(InventoryItem item)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        repository.save(item);
                }

        }

        /** Update an existing item. */
        public void updateItem(InventoryItem item)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        repository.save(item);
                }

        }

        /** Delete an item by id. */
        public void deleteItem(String id)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        repository.delete(id);
                }

        }

        /** Apply yearly depreciation to all items and persist the updated values. */
        public void applyYearlyDepreciation()
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
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

        }

        /** Remove all items from the inventory. */
        public void clearInventory()
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        for (InventoryItem item : repository.findAll())
                        {
                                repository.delete(item.getId());
                        }
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
         * Returns all inventory items formatted as string arrays for legacy callers.
         * Each array contains the item's id, name, and cost formatted with two
         * decimal places. A null cost is represented as {@code 0.00}.
         *
         * @return list of inventory items in string array format
         */
        public static List<String[]> getInventoryItems()
        {
                InventoryService service = new InventoryService();
                List<String[]> rows = new ArrayList<>();

                for (InventoryItem item : service.listItems())
                {
                        String cost = (item.getCost() != null)
                                ? item.getCost().setScale(2, RoundingMode.HALF_UP).toString()
                                : "0.00";
                        rows.add(new String[] { item.getId(), item.getName(), cost });
                }

                return rows;

        }
	
}

