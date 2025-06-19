package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.InventoryItem; // Correct import
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryService manages inventory items for the nonprofit bookkeeping system.
 * Items are persisted using SQL via {@link DatabaseManager}. Methods provide
 * basic CRUD operations and a placeholder for applying depreciation.
 */
public class InventoryService {

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
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_item WHERE item_id=?"))
            {
                ps.setString(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting inventory item", e);
            }
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
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_item"))
        {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing inventory", e);
        }
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
