
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.ui.UiSpacing;
import nonprofitbookkeeping.util.FormatUtils;


/**
 * JavaFX port of {@code InventoryPanel}. Shows capital assets / inventory with
 * add, edit, delete and a simple "Apply Depreciation" button that calls the
 * existing {@link InventoryService#applyYearlyDepreciation()} helper.
 */
public class InventoryPanelFX extends BorderPane
{
	
	/** Service layer for inventory management operations. */
	private final InventoryService service;
	/** Directory of the current company, may be null if none. */
	private final File companyDirectory;
	/** ObservableList to hold {@link InventoryRow} objects for display in the table. */
	private final ObservableList<InventoryRow> rows =
		FXCollections.observableArrayList();
	/** TableView to display the list of inventory items. */
	private final TableView<InventoryRow> table = new TableView<>();
	
	/**
	 * Constructs a new {@code InventoryPanelFX}.
	 * Initializes the panel with the necessary {@link InventoryService}, a table to display inventory items,
	 * and buttons for managing these items (Add, Edit, Delete, Apply Depreciation).
	 *
	 * @param service The {@link InventoryService} to be used for all inventory-related operations. Must not be null.
	 * @param companyDirectory the company directory
	 */
	public InventoryPanelFX(InventoryService service, File companyDirectory)
	{
		this.service = service;
		this.companyDirectory = companyDirectory;
		
		try
		{
			this.service.loadItems(this.companyDirectory);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		setPadding(UiSpacing.pageInsets());
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		refresh();
		
	}
	
	/**
	 * Convenience constructor when no directory is available.
	 *
	 * @param service the service
	 */
	public InventoryPanelFX(InventoryService service)
	{
		this(service, null);
		
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying inventory items.
	 * It defines columns for ID, Item Name, Cost, Acquired Date, Accumulated Depreciation, and Net Book Value.
	 * It uses {@link PropertyValueFactory} (via the {@link #col(String, String)} helper for String columns)
	 * to bind columns to the properties of the {@link InventoryRow} class.
	 * The table is bound to the {@link #rows} observable list and a column resize policy is set.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * uses reflection and can lead to type safety warnings. "deprecation" might relate to older patterns.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	private void buildTable()
	{
		TableColumn<InventoryRow, String> idCol = col("ID", "id");
		TableColumn<InventoryRow, String> nameCol = col("Item", "name");
		TableColumn<InventoryRow, BigDecimal> costCol =
			new TableColumn<>("Cost");
		costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
		costCol.getStyleClass().add("numeric-col");
		TableColumn<InventoryRow, String> dateCol = col("Acquired", "acquired");
		TableColumn<InventoryRow, BigDecimal> depCol =
			new TableColumn<>("Accum. Depr.");
		depCol.setCellValueFactory(new PropertyValueFactory<>("accDep"));
		depCol.getStyleClass().add("numeric-col");
		TableColumn<InventoryRow, BigDecimal> nbvCol =
			new TableColumn<>("Net Book Value");
		nbvCol.setCellValueFactory(new PropertyValueFactory<>("netValue"));
		nbvCol.getStyleClass().add("numeric-col");
		this.table.getColumns().addAll(idCol, nameCol, costCol, dateCol, depCol,
			nbvCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.rows);
		
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for displaying String properties in the inventory table.
	 * This method simplifies column creation by taking a title and the property name
	 * from {@link InventoryRow} that the column should bind to.
	 *
	 * @param title The title of the column for the table header.
	 * @param prop The name of the property in {@link InventoryRow} to bind this column to (e.g., "name" for getName()).
	 * @return A configured {@link TableColumn} for displaying String data from an {@link InventoryRow}.
	 */
	private TableColumn<InventoryRow, String> col(String title, String prop)
	{
		TableColumn<InventoryRow, String> c = new TableColumn<>(title);
		c.setCellValueFactory(new PropertyValueFactory<>(prop));
		return c;
		
	}
	
	/**
	 * Builds and returns a {@link ToolBar} containing buttons for managing inventory items:
	 * "Add Item", "Edit", "Delete", and "Apply Depreciation".
	 * These buttons trigger corresponding actions like opening an item dialog ({@link #itemDialog(InventoryRow)}),
	 * deleting items, or applying yearly depreciation via the {@link #service}.
	 *
	 * @return A configured {@link ToolBar} with action buttons for inventory management.
	 */
	private ToolBar buildButtons()
	{
		Button add = new Button("Add Item");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		Button depr = new Button("Apply Depreciation");
		add.setOnAction(e -> itemDialog(null));
		edit.setOnAction(e -> {
			InventoryRow sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
				itemDialog(sel);
		});
		del.setOnAction(e -> {
			InventoryRow sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.service.deleteItem(sel.id);
				refresh();
				save();
			}
			
		});
		depr.setOnAction(e -> {
			this.service.applyYearlyDepreciation();
			refresh();
			save();
		});
		return new ToolBar(add, edit, del, new Separator(), depr);
		
	}
	
	/**
	 * Displays a dialog for adding a new inventory item or editing an existing one.
	 * If {@code existing} is null, the dialog is configured for adding a new item.
	 * Otherwise, the dialog fields (Name, Cost, Acquired Date, Life (years)) are pre-populated
	 * with the data from the {@code existing} item.
	 * Upon confirmation (OK button) and valid input, a new {@link InventoryItem} is created or updated
	 * via the {@link #service}, and the table is refreshed.
	 * An error alert is shown for invalid input.
	 *
	 * @param existing The {@link InventoryRow} representing the item to edit. If null, the dialog will facilitate creating a new item.
	 */
	private void itemDialog(InventoryRow existing)
	{
		Dialog<InventoryRow> dlg = new Dialog<>();
		dlg.setTitle(existing == null ? "Add Item" : "Edit Item");
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK,
			ButtonType.CANCEL);
		TextField nameF = new TextField();
		TextField costF = new TextField();
		DatePicker dateP = new DatePicker(LocalDate.now());
		TextField lifeF = new TextField("5");
		
		if (existing != null)
		{
			nameF.setText(existing.name);
			costF.setText(FormatUtils.formatCurrency(existing.cost));
			dateP.setValue(LocalDate.parse(existing.acquired));
			lifeF.setText(existing.lifeYears + "");
		}
		
		GridPane g = new GridPane();
		g.setHgap(UiSpacing.GRID_H_GAP);
		g.setVgap(UiSpacing.GRID_V_GAP);
		g.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		g.addRow(0, new Label("Name:"), nameF);
		g.addRow(1, new Label("Cost:"), costF);
		g.addRow(2, new Label("Acquired:"), dateP);
		g.addRow(3, new Label("Life (years):"), lifeF);
		dlg.getDialogPane().setContent(g);
		dlg.setResultConverter(btn -> {
			
			if (btn == ButtonType.OK)
			{
				
				try
				{
					BigDecimal cost = new BigDecimal(costF.getText().trim());
					int life = Integer.parseInt(lifeF.getText().trim());
					InventoryItem item = new InventoryItem(
						existing == null ? UUID.randomUUID().toString() :
							existing.id,
						nameF.getText(), cost, dateP.getValue().toString(),
						life);
					return new InventoryRow(item);
				}
				catch (Exception ex)
				{
					new Alert(Alert.AlertType.ERROR, "Invalid input")
						.showAndWait();
				}
				
			}
			
			return null;
		});
		dlg.showAndWait().ifPresent(row -> {
			if (existing == null)
				this.service.addItem(row.toItem());
			else
				this.service.updateItem(row.toItem());
			refresh();
			save();
		});
		
	}
	
	/**
	 * Refreshes the data displayed in the inventory {@link #table}.
	 * It clears any existing rows, fetches the current list of all inventory items from the {@link #service},
	 * converts each {@link InventoryItem} into an {@link InventoryRow}, and adds them to the
	 * {@link #rows} observable list, which updates the table view.
	 */
	private void refresh()
	{
		this.rows.clear();
		List<InventoryItem> list = this.service.listItems();
		list.forEach(i -> this.rows.add(new InventoryRow(i)));
		
	}
	
	/** Saves current inventory items to disk if a company directory is set. */
	private void save()
	{
		
		try
		{
			this.service.saveItems(this.companyDirectory);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * A data class (POJO) representing a row in the inventory {@link TableView}.
	 * It wraps an {@link InventoryItem} and provides properties suitable for JavaFX table binding.
	 * Note: Fields are package-private for direct access from {@code InventoryPanelFX} methods,
	 * which is a simpler approach for this UI context but less encapsulated.
	 */
	public static class InventoryRow
	{
		/** The unique ID of the inventory item. */
		String id;
		/** The name or description of the inventory item. */
		String name;
		/** The date the item was acquired, as a string (e.g., "YYYY-MM-DD"). */
		String acquired;
		/** The original cost of the inventory item. */
		BigDecimal cost;
		/** The accumulated depreciation for the item. */
		BigDecimal accDep;
		/** The net book value of the item (cost - accumulated depreciation). */
		BigDecimal netValue;
		/** The expected useful life of the item in years, for depreciation purposes. */
		int lifeYears;
		
		/**
		 * Constructs an {@code InventoryRow} from an {@link InventoryItem} object.
		 * Initializes all fields by extracting data from the given item.
		 *
		 * @param i The {@link InventoryItem} from which to create the row data. Must not be null.
		 */
		InventoryRow(InventoryItem i)
		{
			this.id = i.getId();
			this.name = i.getName();
			this.acquired = i.getAcquiredDate();
			this.cost = i.getCost();
			this.accDep = i.getAccumulatedDepreciation();
			this.netValue = i.getNetBookValue();
			this.lifeYears = i.getLifeYears();
			
		}
		
		/**
		 * Gets the ID of the inventory item.
		 * @return The ID string.
		 */
		public String getId()
		{
			return this.id;
			
		}
		
		/**
		 * Gets the name of the inventory item.
		 * @return The item's name.
		 */
		public String getName()
		{
			return this.name;
			
		}
		
		/**
		 * Gets the acquired date of the inventory item.
		 * @return The acquired date as a string.
		 */
		public String getAcquired()
		{
			return this.acquired;
			
		}
		
		/**
		 * Gets the cost of the inventory item.
		 * @return The cost as a {@link BigDecimal}.
		 */
		public BigDecimal getCost()
		{
			return this.cost;
			
		}
		
		/**
		 * Gets the accumulated depreciation of the inventory item.
		 * @return The accumulated depreciation as a {@link BigDecimal}.
		 */
		public BigDecimal getAccDep()
		{
			return this.accDep;
			
		}
		
		/**
		 * Gets the net book value of the inventory item.
		 * @return The net book value as a {@link BigDecimal}.
		 */
		public BigDecimal getNetValue()
		{
			return this.netValue;
			
		}
		
		// lifeYears is not exposed via a getter, but used in toItem()
		
		/**
		 * Converts this {@code InventoryRow} back into an {@link InventoryItem} object.
		 * This is useful for passing data back to the service layer after editing in the UI.
		 * The accumulated depreciation from the row is preserved in the new item.
		 *
		 * @return A new {@link InventoryItem} instance populated with data from this row.
		 */
		InventoryItem toItem()
		{
			return new InventoryItem(this.id, this.name, this.cost,
				this.acquired, this.lifeYears)
				.withAccumDep(this.accDep);
			
		}
		
	}
	
}
