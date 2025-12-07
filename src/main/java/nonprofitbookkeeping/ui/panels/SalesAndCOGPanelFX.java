
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.io.File;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import nonprofitbookkeeping.model.SaleRecord;
import nonprofitbookkeeping.service.SalesService;
import nonprofitbookkeeping.util.FormatUtils;

/**
 * JavaFX port of {@code SalesAndCOGPanel}. Maintains an in‑memory table of sale
 * transactions with calculated margin (Price – Cost).  In a full application
 * this would be wired to an Inventory / Sales service.
 */
public class SalesAndCOGPanelFX extends BorderPane
{
	
	/** ObservableList to hold {@link SaleRow} objects for display in the table. */
	private final ObservableList<SaleRow> rows = FXCollections.observableArrayList();
	/** TableView to display the list of sale transactions. */
	private final TableView<SaleRow> table = new TableView<>();
	/** Label to display the calculated total gross profit from all sales. */
       private final Label totalLbl = new Label("Gross Profit: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
	/** Service for persisting sales. */
	private final SalesService service;
	/** Directory used for persistence, may be null. */
	private final File companyDirectory;
	
	/**
	 * Constructs a new {@code SalesAndCOGPanelFX}.
	 * Initializes the panel with a table to display sale transactions and buttons for managing sales.
	 */
	public SalesAndCOGPanelFX(SalesService service, File companyDirectory)
	{
                this.service = service == null ? new SalesService() : service;
                this.companyDirectory = companyDirectory;

                try
                {
                        this.service.loadSales(this.companyDirectory);
                }
                catch (Exception ex)
                {
                        ex.printStackTrace();
                }
		
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		
		for (SaleRecord r : this.service.listSales())
			this.rows.add(new SaleRow(r));
		updateTotals();
	}
	
	/** Convenience constructor when no directory is available. */
	public SalesAndCOGPanelFX(SalesService service)
	{
		this(service, null);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying sale transactions.
	 * It defines columns for Date, Item, Quantity, Price, Cost, and Margin,
	 * using {@link PropertyValueFactory} (via the {@link #col(String, String)} helper for String columns)
	 * to bind them to the properties of the {@link SaleRow} class.
	 * The table is bound to the {@link #rows} observable list and a column resize policy is set.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * uses reflection and can lead to type safety warnings. "deprecation" might relate to older patterns.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<SaleRow, String> dateCol = col("Date", "date");
		TableColumn<SaleRow, String> itemCol = col("Item", "item");
		TableColumn<SaleRow, Integer> qtyCol = new TableColumn<>("Qty");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
		TableColumn<SaleRow, BigDecimal> priceCol = new TableColumn<>("Price");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		TableColumn<SaleRow, BigDecimal> costCol = new TableColumn<>("Cost");
		costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
		TableColumn<SaleRow, BigDecimal> marginCol = new TableColumn<>("Margin");
		marginCol.setCellValueFactory(new PropertyValueFactory<>("margin"));
		this.table.getColumns().addAll(dateCol, itemCol, qtyCol, priceCol, costCol, marginCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.rows);
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for displaying String properties in the sales table.
	 *
	 * @param t The title of the column for the table header.
	 * @param p The name of the property in {@link SaleRow} to bind this column to (e.g., "item" for getItem()).
	 * @return A configured {@link TableColumn} for displaying String data from a {@link SaleRow}.
	 */
	private static TableColumn<SaleRow, String> col(String t, String p)
	{
		TableColumn<SaleRow, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
	/**
	 * Builds and returns an {@link HBox} containing "Add Sale" and "Delete" buttons,
	 * along with the {@link #totalLbl} for displaying gross profit.
	 * These buttons provide functionality to manage sale records in the table.
	 * "Add Sale" opens the {@link #saleDialog(SaleRow)}.
	 * "Delete" removes the selected sale from the table and updates totals.
	 *
	 * @return A configured {@link HBox} with action buttons and the total gross profit label.
	 */
	private HBox buildButtons()
	{
		Button add = new Button("Add Sale");
		Button del = new Button("Delete");
		add.setOnAction(e -> saleDialog(null));
		del.setOnAction(e -> {
			SaleRow sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.rows.remove(sel);
				updateTotals();
				syncServiceFromRows();
				save();
			}
			
		});
		HBox box = new HBox(10, add, del, this.totalLbl);
		box.setPadding(new Insets(8));
		return box;
	}
	
	/**
	 * Displays a dialog for adding a new sale or editing an existing one.
	 * If {@code existing} is null, the dialog is configured for adding a new sale.
	 * Otherwise, the dialog fields (Date, Item, Quantity, Price, Cost) are pre-populated
	 * with the data from the {@code existing} sale row.
	 * Upon confirmation (OK button) and valid input, a new {@link SaleRow} object is created
	 * (or the existing one updated) and added to/refreshed in the {@link #rows} list and table.
	 * Totals are updated after adding or editing a sale.
	 * An error alert is shown for invalid input.
	 *
	 * @param existing The {@link SaleRow} representing the sale to edit. If null, the dialog will facilitate creating a new sale.
	 */
	private void saleDialog(SaleRow existing)
	{
		Dialog<SaleRow> dlg = new Dialog<>();
		dlg.setTitle(existing == null ? "Add Sale" : "Edit Sale");
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		DatePicker dateP = new DatePicker(
			existing == null ? LocalDate.now() : LocalDate.parse(existing.getDate())); // String →
																						// LocalDate
		
		TextField itemF = new TextField(existing == null ? "" : existing.getItem());
		
		TextField qtyF = new TextField(existing == null ? "1" : String.valueOf(existing.getQty())); // Integer
																									// →
																									// String
		
               TextField priceF =
                       new TextField(existing == null ? FormatUtils.formatCurrency(BigDecimal.ZERO) :
                                       FormatUtils.formatCurrency(existing.getPrice()));
																							// →
																							// String
		
               TextField costF =
                       new TextField(existing == null ? FormatUtils.formatCurrency(BigDecimal.ZERO) :
                                       FormatUtils.formatCurrency(existing.getCost()));
																							// →
																							// String
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(8);
		g.setPadding(new Insets(10));
		g.addRow(0, new Label("Date:"), dateP);
		g.addRow(1, new Label("Item:"), itemF);
		g.addRow(2, new Label("Quantity:"), qtyF);
		g.addRow(3, new Label("Price:"), priceF);
		g.addRow(4, new Label("Cost:"), costF);
		dlg.getDialogPane().setContent(g);
		dlg.setResultConverter(btn -> {
			
			if (btn == ButtonType.OK)
			{
				
				try
				{
					int qty = Integer.parseInt(qtyF.getText().trim());
					BigDecimal pr = new BigDecimal(priceF.getText().trim());
					BigDecimal cs = new BigDecimal(costF.getText().trim());
					return new SaleRow(dateP.getValue(), itemF.getText(), qty, pr, cs);
				}
				catch (@SuppressWarnings("unused") Exception ex)
				{
					new Alert(Alert.AlertType.ERROR, "Invalid input").showAndWait();
				}
				
			}
			
			return null;
		});
		dlg.showAndWait().ifPresent(row -> {
			
			if (existing == null)
				this.rows.add(row);
			else
			{
				existing.copyFrom(row);
				this.table.refresh();
			}
			
			updateTotals();
			syncServiceFromRows();
			save();
		});
	}
	
	/**
	 * Calculates and updates the total gross profit displayed in {@link #totalLbl}.
	 * The gross profit is calculated by summing the 'margin' from all {@link SaleRow} objects
	 * currently in the {@link #rows} list.
	 */
	private void updateTotals()
	{
		BigDecimal profit =
			this.rows.stream().map(r -> r.margin.get()).reduce(BigDecimal.ZERO, BigDecimal::add);
		this.totalLbl.setText("Gross Profit: " + profit);
	}
	
	/** Synchronizes the service with the current table rows. */
	private void syncServiceFromRows()
	{
		this.service.clear();
		
		for (SaleRow r : this.rows)
		{
			this.service.addSale(r.toRecord());
		}
		
	}
	
	/** Saves data to disk if a directory is provided. */
	private void save()
	{
		
                try
                {
                        this.service.saveSales(this.companyDirectory);
                }
                catch (Exception ex)
                {
                        ex.printStackTrace();
                }

        }
	
	/* ------------------------------------------------------------------ */
	/**
	 * Represents a single sale transaction row in the table.
	 * This class uses JavaFX properties for data binding with {@link TableView} columns.
	 * It includes details such as date, item, quantity, price, cost, and calculated margin.
	 * Each sale row is assigned a unique ID.
	 */
	public static class SaleRow
	{
		/** A unique identifier for the sale transaction. */
		final String id;
		/** The date of the sale, as a {@link SimpleStringProperty}. */
		final SimpleStringProperty date = new SimpleStringProperty();
		/** The name or description of the item sold, as a {@link SimpleStringProperty}. */
		final SimpleStringProperty item = new SimpleStringProperty();
		/** The quantity of items sold, as a {@link SimpleObjectProperty} of {@link Integer}. */
		final SimpleObjectProperty<Integer> qty = new SimpleObjectProperty<>();
		/** The unit price of the item sold, as a {@link SimpleObjectProperty} of {@link BigDecimal}. */
		final SimpleObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
		/** The unit cost of the item sold, as a {@link SimpleObjectProperty} of {@link BigDecimal}. */
		final SimpleObjectProperty<BigDecimal> cost = new SimpleObjectProperty<>();
		/** The calculated margin for this sale (Quantity * (Price - Cost)), as a {@link SimpleObjectProperty} of {@link BigDecimal}. */
		final SimpleObjectProperty<BigDecimal> margin = new SimpleObjectProperty<>();
		
		/**
		 * Constructs a new {@code SaleRow} with auto-generated ID.
		 */
		SaleRow(LocalDate d, String it, int q, BigDecimal p, BigDecimal c)
		{
			this(UUID.randomUUID().toString(), d, it, q, p, c);
		}
		
		/** Construct from an existing {@link SaleRecord}. */
		SaleRow(SaleRecord rec)
		{
			this(rec.getId(), LocalDate.parse(rec.getDate()), rec.getItem(), rec.getQty(),
				rec.getPrice(), rec.getCost());
		}
		
		/**
		 * Constructs a new {@code SaleRow} with explicit ID.
		 */
		SaleRow(String id, LocalDate d, String it, int q, BigDecimal p, BigDecimal c)
		{
			this.id = id;
			this.date.set(d.toString());
			this.item.set(it);
			this.qty.set(q);
			this.price.set(p);
			this.cost.set(c);
			calcMargin();
		}
		
		/**
		 * Calculates the margin for this sale entry (Quantity * (Price - Cost))
		 * and updates the {@link #margin} property.
		 */
		void calcMargin()
		{
			this.margin.set(this.price.get().subtract(this.cost.get())
				.multiply(new BigDecimal(this.qty.get())));
		} // extended margin = qty*(price-cost)
		
		/**
		 * Copies data from another {@link SaleRow} object into this one.
		 * This is used when updating an existing row after an edit operation.
		 * After copying, the margin is recalculated.
		 *
		 * @param other The {@link SaleRow} from which to copy data. Must not be null.
		 */
		void copyFrom(SaleRow other)
		{
			this.date.set(other.date.get());
			this.item.set(other.item.get());
			this.qty.set(other.qty.get());
			this.price.set(other.price.get());
			this.cost.set(other.cost.get());
			calcMargin();
		}
		
		/**
		 * Gets the date of the sale.
		 * @return The sale date as a String.
		 */
		public String getDate()
		{
			return this.date.get();
		}
		
		/**
		 * Gets the name/description of the item sold.
		 * @return The item name/description.
		 */
		public String getItem()
		{
			return this.item.get();
		}
		
		/**
		 * Gets the quantity of items sold.
		 * @return The quantity sold.
		 */
		public int getQty()
		{
			return this.qty.get();
		}
		
		/**
		 * Gets the unit price of the item sold.
		 * @return The unit price as a {@link BigDecimal}.
		 */
		public BigDecimal getPrice()
		{
			return this.price.get();
		}
		
		/**
		 * Gets the unit cost of the item sold.
		 * @return The unit cost as a {@link BigDecimal}.
		 */
		public BigDecimal getCost()
		{
			return this.cost.get();
		}
		
		/**
		 * Gets the calculated margin for this sale.
		 * @return The margin as a {@link BigDecimal}.
		 */
		public BigDecimal getMargin()
		{
			return this.margin.get();
		}
		
		/** Convert this row to a {@link SaleRecord} for persistence. */
		SaleRecord toRecord()
		{
			return new SaleRecord(this.id, getDate(), getItem(), getQty(), getPrice(), getCost());
		}
		
		// Note: The 'id' field does not have a public getter, implies it's for internal
		// use or PropertyValueFactory access.
	}
	
}
