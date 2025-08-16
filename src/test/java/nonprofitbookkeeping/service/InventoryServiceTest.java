
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.InventoryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest
{
	
	private InventoryService service;
	
	@BeforeEach
	void setUp()
	{
		this.service.clearInventory(); // Clear the static map
		this.service = new InventoryService(); // Re-initialize service, which
												// ensures map is ready
		
	}
	
	// --- addItem Tests ---
	@Test
	@DisplayName("Test adding a new item")
	void testAddNewItem()
	{
		InventoryItem item =
			new InventoryItem("I001", "Test Item 1", BigDecimal.valueOf(100),
				"2023-01-01", 5);
		this.service.addItem(item);
		assertEquals(1, this.service.listItems().size());
		assertEquals("Test Item 1", this.service.listItems().get(0).getName());
		
	}
	
	@Test
	@DisplayName("Test adding an item with an existing ID should overwrite")
	void testAddItemOverwrite()
	{
		InventoryItem item1 =
			new InventoryItem("I001", "Original Item", BigDecimal.valueOf(100),
				"2023-01-01", 5);
		this.service.addItem(item1);
		InventoryItem item2 =
			new InventoryItem("I001", "Updated Item", BigDecimal.valueOf(120),
				"2023-02-01", 4);
		this.service.addItem(item2);
		assertEquals(1, this.service.listItems().size());
		assertEquals("Updated Item", this.service.listItems().get(0).getName());
		assertEquals(0,
			BigDecimal.valueOf(120)
				.compareTo(this.service.listItems().get(0).getCost()));
		
	}
	
	@Test
	@DisplayName("Test adding a null item")
	void testAddNullItem()
	{
		this.service.addItem(null);
		assertEquals(0, this.service.listItems().size());
		
	}
	
	@Test
	@DisplayName("Test adding an item with a null ID")
	void testAddItemNullId()
	{
		InventoryItem item =
			new InventoryItem(null, "Test Item No ID", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.addItem(item);
		assertEquals(0, this.service.listItems().size());
		
	}
	
	// --- listItems Tests ---
	@Test
	@DisplayName("Test listItems on an empty inventory")
	void testListItemsEmpty()
	{
		assertTrue(this.service.listItems().isEmpty());
		
	}
	
	@Test
	@DisplayName("Test listItems after adding items")
	void testListItemsWithItems()
	{
		InventoryItem item1 =
			new InventoryItem("I001", "Item A", BigDecimal.valueOf(10),
				"2023-01-01", 5);
		InventoryItem item2 =
			new InventoryItem("I002", "Item B", BigDecimal.valueOf(20),
				"2023-01-01", 5);
		this.service.addItem(item1);
		this.service.addItem(item2);
		List<InventoryItem> items = this.service.listItems();
		assertEquals(2, items.size());
		assertTrue(items.stream().anyMatch(i -> "Item A".equals(i.getName())));
		assertTrue(items.stream().anyMatch(i -> "Item B".equals(i.getName())));
		
	}
	
	// --- deleteItem Tests ---
	@Test
	@DisplayName("Test deleting an existing item")
	void testDeleteItemExisting()
	{
		InventoryItem item1 =
			new InventoryItem("I001", "To Delete", BigDecimal.valueOf(10),
				"2023-01-01", 5);
		this.service.addItem(item1);
		assertEquals(1, this.service.listItems().size());
		this.service.deleteItem("I001");
		assertTrue(this.service.listItems().isEmpty());
		
	}
	
	@Test
	@DisplayName("Test deleting a non-existent item")
	void testDeleteItemNonExistent()
	{
		InventoryItem item1 =
			new InventoryItem("I001", "Item", BigDecimal.valueOf(10),
				"2023-01-01", 5);
		this.service.addItem(item1);
		this.service.deleteItem("I002"); // Non-existent ID
		assertEquals(1, this.service.listItems().size());
		
	}
	
	@Test
	@DisplayName("Test deleting with a null ID")
	void testDeleteItemNullId()
	{
		InventoryItem item1 =
			new InventoryItem("I001", "Item", BigDecimal.valueOf(10),
				"2023-01-01", 5);
		this.service.addItem(item1);
		this.service.deleteItem(null);
		assertEquals(1, this.service.listItems().size());
		
	}
	
	// --- updateItem Tests ---
	@Test
	@DisplayName("Test updating an existing item")
	void testUpdateItemExisting()
	{
		InventoryItem originalItem =
			new InventoryItem("U001", "Original Name", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.addItem(originalItem);
		
		InventoryItem updatedItemInfo =
			new InventoryItem("U001", "Updated Name", BigDecimal.valueOf(75),
				"2023-02-02", 4);
		// Ensure all fields are set for the update, as it replaces the object
		updatedItemInfo.setAccumulatedDepreciation(BigDecimal.valueOf(10));
		updatedItemInfo.setDepreciationRate(BigDecimal.valueOf(0.15));
		
		this.service.updateItem(updatedItemInfo);
		
		List<InventoryItem> items = this.service.listItems();
		assertEquals(1, items.size());
		InventoryItem itemAfterUpdate = items.get(0);
		assertEquals("Updated Name", itemAfterUpdate.getName());
		assertEquals(0,
			BigDecimal.valueOf(75).compareTo(itemAfterUpdate.getCost()));
		assertEquals("2023-02-02", itemAfterUpdate.getAcquiredDate());
		assertEquals(4, itemAfterUpdate.getLifeYears());
		assertEquals(0,
			BigDecimal.valueOf(10)
				.compareTo(itemAfterUpdate.getAccumulatedDepreciation()));
		assertEquals(0, BigDecimal.valueOf(0.15)
			.compareTo(itemAfterUpdate.getDepreciationRate()));
		
	}
	
	@Test
	@DisplayName("Test updating a non-existent item")
	void testUpdateItemNonExistent()
	{
		InventoryItem itemToUpdate =
			new InventoryItem("U002", "Non Existent", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.updateItem(itemToUpdate);
		assertTrue(this.service.listItems().isEmpty());
		
	}
	
	@Test
	@DisplayName("Test updating with a null item")
	void testUpdateItemNull()
	{
		InventoryItem originalItem =
			new InventoryItem("U001", "Original", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.addItem(originalItem);
		this.service.updateItem(null);
		assertEquals("Original", this.service.listItems().get(0).getName()); // Should
																				// remain
																				// unchanged
		
	}
	
	@Test
	@DisplayName("Test updating with an item having a null ID")
	void testUpdateItemNullId()
	{
		InventoryItem originalItem =
			new InventoryItem("U001", "Original", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.addItem(originalItem);
		InventoryItem itemToUpdate =
			new InventoryItem(null, "Updated?", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.updateItem(itemToUpdate);
		assertEquals("Original", this.service.listItems().get(0).getName()); // Should
																				// remain
																				// unchanged
		
	}
	
	@Test
	@DisplayName("Test updating with an item having a blank ID")
	void testUpdateItemBlankId()
	{
		InventoryItem originalItem =
			new InventoryItem("U001", "Original", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.addItem(originalItem);
		InventoryItem itemToUpdate =
			new InventoryItem(" ", "Updated?", BigDecimal.valueOf(50),
				"2023-01-01", 5);
		this.service.updateItem(itemToUpdate);
		assertEquals("Original", this.service.listItems().get(0).getName()); // Should
																				// remain
																				// unchanged
		
	}
	
	
	// --- getInventoryItems (String array format) Tests ---
	@Test
	@DisplayName("Test getInventoryItems on empty inventory")
	void testGetInventoryItemsEmpty()
	{
		List<String[]> stringArrays = InventoryService.getInventoryItems();
		assertTrue(stringArrays.isEmpty());
		
	}
	
	@Test
	@DisplayName("Test getInventoryItems with items")
	void testGetInventoryItemsWithData()
	{
		InventoryItem item1 =
			new InventoryItem("S001", "String Item 1",
				BigDecimal.valueOf(10.50), "2023-01-01", 5);
		InventoryItem item2 =
			new InventoryItem("S002", "String Item 2", null, "2023-01-01", 5); // Null
																				// cost
		this.service.addItem(item1);
		this.service.addItem(item2);
		
		List<String[]> stringArrays = InventoryService.getInventoryItems();
		assertEquals(2, stringArrays.size());
		
		for (String[] arr : stringArrays)
		{
			
			if ("S001".equals(arr[0]))
			{
				assertEquals("String Item 1", arr[1]);
				assertEquals("10.50", arr[2]);
			}
			else if ("S002".equals(arr[0]))
			{
				assertEquals("String Item 2", arr[1]);
				assertEquals("0.00", arr[2]); // Null cost should format to 0.00
			}
			else
			{
				fail("Unexpected item ID in string array output");
			}
			
		}
		
	}
	
	// --- applyYearlyDepreciation Tests ---
	@Test
	@DisplayName("Test depreciation on item with no rate/method")
	void testDepreciationNoRateMethod()
	{
		InventoryItem item =
			new InventoryItem("D001", "No Dep", BigDecimal.valueOf(1000),
				"2023-01-01", 5);
		// item.setDepreciationRate(null) is default from constructor
		// item.setDepreciationMethod("Straight-Line") is default
		item.setDepreciationMethod(null); // Explicitly set method to null
		this.service.addItem(item);
		
		this.service.applyYearlyDepreciation();
		
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertNull(itemAfter.getAccumulatedDepreciation());
		assertNull(itemAfter.getNetValue());
		assertEquals(0,
			BigDecimal.valueOf(1000).compareTo(itemAfter.getCost()));
		
	}
	
	@Test
	@DisplayName("Test depreciation with Straight-Line, valid rate")
	void testDepreciationStraightLineValidRate()
	{
		InventoryItem item =
			new InventoryItem("D002", "Dep Item", BigDecimal.valueOf(1000),
				"2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(0.10)); // 10% rate
		// item.setDepreciationMethod("Straight-Line"); // Default from
		// constructor
		this.service.addItem(item);
		
		this.service.applyYearlyDepreciation();
		
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertNotNull(itemAfter.getAccumulatedDepreciation());
		assertEquals(0, BigDecimal.valueOf(100)
			.compareTo(itemAfter.getAccumulatedDepreciation())); // 1000
																	// *
																	// 0.10
		assertNotNull(itemAfter.getNetValue());
		assertEquals(0,
			BigDecimal.valueOf(900).compareTo(itemAfter.getNetValue())); // 1000
																			// -
																			// 100
		assertEquals(0,
			BigDecimal.valueOf(1000).compareTo(itemAfter.getCost())); // Original
																		// cost
																		// unchanged
		
	}
	
	@Test
	@DisplayName("Test depreciation with Straight-Line, valid rate, existing AccDep")
	void testDepreciationStraightLineValidRateWithExistingAccDep()
	{
		InventoryItem item =
			new InventoryItem("D002_AD", "Dep Item AD",
				BigDecimal.valueOf(1000), "2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(0.10));
		item.setAccumulatedDepreciation(BigDecimal.valueOf(50)); // Pre-existing
																	// AccDep
		this.service.addItem(item);
		
		this.service.applyYearlyDepreciation();
		
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertEquals(0, BigDecimal.valueOf(150)
			.compareTo(itemAfter.getAccumulatedDepreciation())); // 50
																	// +
																	// (1000
																	// *
																	// 0.10)
		assertEquals(0,
			BigDecimal.valueOf(850).compareTo(itemAfter.getNetValue())); // 1000
																			// -
																			// 150
		
	}
	
	
	@Test
	@DisplayName("Test depreciation with zero rate")
	void testDepreciationZeroRate()
	{
		InventoryItem item =
			new InventoryItem("D003", "Zero Rate Dep", BigDecimal.valueOf(1000),
				"2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.ZERO);
		this.service.addItem(item);
		this.service.applyYearlyDepreciation();
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertNull(itemAfter.getAccumulatedDepreciation()); // Or
															// BigDecimal.ZERO
															// if we want to
															// initialize it
		assertNull(itemAfter.getNetValue());
		
	}
	
	@Test
	@DisplayName("Test depreciation with negative rate")
	void testDepreciationNegativeRate()
	{
		InventoryItem item = new InventoryItem("D004", "Negative Rate Dep",
			BigDecimal.valueOf(1000), "2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(-0.10));
		this.service.addItem(item);
		this.service.applyYearlyDepreciation();
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertNull(itemAfter.getAccumulatedDepreciation());
		assertNull(itemAfter.getNetValue());
		
	}
	
	@Test
	@DisplayName("Test multiple applications of depreciation")
	void testMultipleDepreciationApplications()
	{
		InventoryItem item =
			new InventoryItem("D005", "Multi Dep", BigDecimal.valueOf(1000),
				"2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(0.10));
		this.service.addItem(item);
		
		this.service.applyYearlyDepreciation(); // Year 1
		InventoryItem itemYear1 = this.service.listItems().get(0); // Re-fetch
																	// or ensure
																	// service
																	// returns
																	// modified
																	// instance
		assertEquals(0, BigDecimal.valueOf(100)
			.compareTo(itemYear1.getAccumulatedDepreciation()));
		assertEquals(0,
			BigDecimal.valueOf(900).compareTo(itemYear1.getNetValue()));
		
		// To test on the *same instance* that's in the map, we need to update
		// the item
		// in the map
		// or ensure applyYearlyDepreciation modifies the instance in the map
		// directly
		// (which it does).
		this.service.applyYearlyDepreciation(); // Year 2
		InventoryItem itemYear2 = this.service.listItems().get(0);
		assertEquals(0, BigDecimal.valueOf(200)
			.compareTo(itemYear2.getAccumulatedDepreciation())); // 100
																	// +
																	// (1000
																	// *
																	// 0.10)
		assertEquals(0,
			BigDecimal.valueOf(800).compareTo(itemYear2.getNetValue())); // 1000
																			// -
																			// 200
		
	}
	
	@Test
	@DisplayName("Test depreciation with null original cost")
	void testDepreciationNullOriginalCost()
	{
		InventoryItem item =
			new InventoryItem("D006", "Null Cost Dep", null, "2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(0.10));
		this.service.addItem(item);
		this.service.applyYearlyDepreciation();
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertNull(itemAfter.getAccumulatedDepreciation());
		assertNull(itemAfter.getNetValue());
		
	}
	
	@Test
	@DisplayName("Test depreciation with null initial accumulated depreciation")
	void testDepreciationNullInitialAccDep()
	{
		InventoryItem item =
			new InventoryItem("D007", "Null AccDep", BigDecimal.valueOf(1000),
				"2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(0.10));
		item.setAccumulatedDepreciation(null); // Explicitly set for test
												// clarity, though it's
												// default
		this.service.addItem(item);
		
		this.service.applyYearlyDepreciation();
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertEquals(0, BigDecimal.valueOf(100)
			.compareTo(itemAfter.getAccumulatedDepreciation()));
		assertEquals(0,
			BigDecimal.valueOf(900).compareTo(itemAfter.getNetValue()));
		
	}
	
	@Test
	@DisplayName("Test depreciation with non-Straight-Line method")
	void testDepreciationNonStraightLineMethod()
	{
		InventoryItem item =
			new InventoryItem("D008", "Other Method", BigDecimal.valueOf(1000),
				"2023-01-01", 5);
		item.setDepreciationRate(BigDecimal.valueOf(0.10));
		item.setDepreciationMethod("Declining Balance"); // Different method
		this.service.addItem(item);
		
		this.service.applyYearlyDepreciation();
		InventoryItem itemAfter = this.service.listItems().get(0);
		assertNull(itemAfter.getAccumulatedDepreciation()); // Should not have
															// changed
		assertNull(itemAfter.getNetValue());
		
	}
	
}
