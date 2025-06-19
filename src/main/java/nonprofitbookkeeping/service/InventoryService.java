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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
>>>>>>> b1f07f2 Extend SQL support
import java.util.ArrayList;
import java.util.List;

/**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * Service layer for {@link InventoryItem} entities using JPA for persistence.
=======
 * InventoryService manages inventory items for the nonprofit bookkeeping system.
 * Items are persisted using SQL via {@link DatabaseManager}. Methods provide
 * basic CRUD operations and a placeholder for applying depreciation.
>>>>>>> b1f07f2 Extend SQL support
 */
public class InventoryService
{

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
    public InventoryService() {
    }

    /**
     * Retrieves a list of all inventory items currently managed by this service.
     *
     * @return A new {@link ArrayList} containing all {@link InventoryItem} objects.
     *         This is a copy, so modifications to the returned list will not affect internal storage.
     *         Returns an empty list if no items are present.
     */
    public List<InventoryItem> listItems() {
        List<InventoryItem> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT item_id,name,acquired,cost,accum_depreciation,net_value,life_years,depreciation_rate,depreciation_method FROM inventory_item"))
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InventoryItem i = new InventoryItem();
                i.setId(rs.getString(1));
                i.setName(rs.getString(2));
                Date acq = rs.getDate(3);
                if (acq != null) i.setAcquired(acq.toString());
                i.setCost(rs.getBigDecimal(4));
                i.setAccDep(rs.getBigDecimal(5));
                i.setNetValue(rs.getBigDecimal(6));
                i.setLifeYears(rs.getInt(7));
                i.setDepreciationRate(rs.getBigDecimal(8));
                i.setDepreciationMethod(rs.getString(9));
                list.add(i);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading inventory", e);
        }
        return list;
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
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE inventory_item SET name=?, acquired=?, cost=?, accum_depreciation=?, net_value=?, life_years=?, depreciation_rate=?, depreciation_method=? WHERE item_id=?"))
            {
                ps.setString(1, item.getName());
                ps.setDate(2, item.getAcquired() == null ? null : Date.valueOf(item.getAcquired()));
                ps.setBigDecimal(3, item.getCost());
                ps.setBigDecimal(4, item.getAccDep());
                ps.setBigDecimal(5, item.getNetValue());
                ps.setInt(6, item.getLifeYears());
                ps.setBigDecimal(7, item.getDepreciationRate());
                ps.setString(8, item.getDepreciationMethod());
                ps.setString(9, item.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating inventory item", e);
            }
>>>>>>> b1f07f2 Extend SQL support
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
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_item WHERE item_id=?"))
            {
                ps.setString(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting inventory item", e);
            }
>>>>>>> b1f07f2 Extend SQL support
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
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_item"))
        {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing inventory", e);
        }
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

