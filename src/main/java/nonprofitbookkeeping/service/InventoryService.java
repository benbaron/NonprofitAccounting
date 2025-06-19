package nonprofitbookkeeping.service;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.InventoryRepository;


import java.math.BigDecimal;
import java.math.RoundingMode;
=======
import nonprofitbookkeeping.model.InventoryItem; // Correct import
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
>>>>>>> b1f07f2 Extend SQL support
=======
>>>>>>> 6159d55 Revert service changes
import java.util.ArrayList;
import java.util.List;

/**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * Service layer for {@link InventoryItem} entities using JPA for persistence.
=======
 * InventoryService manages inventory items for the nonprofit bookkeeping system.
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * Items are persisted using SQL via {@link DatabaseManager}. Methods provide
 * basic CRUD operations and a placeholder for applying depreciation.
>>>>>>> b1f07f2 Extend SQL support
=======
 * It maintains an in-memory map of inventory items, keyed by their unique ID,
 * and provides methods to list, add, update, delete, and manage these items,
 * including a placeholder for applying depreciation.
>>>>>>> 6159d55 Revert service changes
 */
public class InventoryService
{

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /** List all inventory items. */
        public List<InventoryItem> listItems()
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        return repository.findAll();
                }

=======
    /** Constructs an {@code InventoryService}. */
=======
    /** In-memory map to store {@link InventoryItem} objects, keyed by their unique ID. */
    private final Map<String, InventoryItem> items;

    /**
     * Constructs an {@code InventoryService} and initializes an empty inventory map.
     * Optionally, sample data can be pre-populated here during development or testing.
     */
>>>>>>> 6159d55 Revert service changes
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
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "MERGE INTO inventory_item(item_id,name,acquired,cost,accum_depreciation,net_value,life_years,depreciation_rate,depreciation_method) KEY(item_id) VALUES(?,?,?,?,?,?,?,?,?)"))
            {
                ps.setString(1, item.getId());
                ps.setString(2, item.getName());
                if (item.getAcquired() != null) {
                    ps.setDate(3, Date.valueOf(item.getAcquired()));
                } else {
                    ps.setDate(3, null);
                }
                ps.setBigDecimal(4, item.getCost());
                ps.setBigDecimal(5, item.getAccDep());
                ps.setBigDecimal(6, item.getNetValue());
                ps.setInt(7, item.getLifeYears());
                ps.setBigDecimal(8, item.getDepreciationRate());
                ps.setString(9, item.getDepreciationMethod());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error adding inventory item", e);
            }
>>>>>>> b1f07f2 Extend SQL support
=======
            this.items.put(item.getId(), item);
>>>>>>> 6159d55 Revert service changes
        }

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /** Add a new item. */
        public void addItem(InventoryItem item)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        repository.save(item);
                }

=======
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
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
>>>>>>> b1f07f2 Extend SQL support
=======
            // Else: Consider logging or throwing an exception if item to update is not found,
            // depending on desired behavior.
>>>>>>> 6159d55 Revert service changes
        }

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /** Update an existing item. */
        public void updateItem(InventoryItem item)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        repository.save(item);
                }

=======
    /**
     * Deletes an inventory item from the collection based on its ID.
     * If the provided {@code id} is null, or if no item with that ID exists, no action is taken.
     *
     * @param id The unique identifier (ID) of the inventory item to delete.
     */
    public void deleteItem(String id) {
        if (id != null) {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_item WHERE item_id=?"))
            {
                ps.setString(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting inventory item", e);
            }
>>>>>>> b1f07f2 Extend SQL support
=======
            this.items.remove(id);
>>>>>>> 6159d55 Revert service changes
        }

        /** Delete an item by id. */
        public void deleteItem(String id)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        InventoryRepository repository = new InventoryRepository(em);
                        repository.delete(id);
                }

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        }
=======
    /**
     * Clears all items from the inventory managed by this service instance.
     * After this operation, the inventory will be empty.
     */
    public void clearInventory() {
        this.items.clear();
        System.out.println("Inventory cleared."); // Consider using a logger
    }
>>>>>>> b1f07f2 Extend SQL support

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

