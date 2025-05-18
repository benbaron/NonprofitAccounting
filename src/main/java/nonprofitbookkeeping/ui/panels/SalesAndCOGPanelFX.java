
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
import javafx.stage.Stage;

/**
 * JavaFX port of {@code SalesAndCOGPanel}. Maintains an in‑memory table of sale
 * transactions with calculated margin (Price – Cost).  In a full application
 * this would be wired to an Inventory / Sales service.
 */
public class SalesAndCOGPanelFX extends BorderPane
{
	
	private final ObservableList<SaleRow> rows = FXCollections.observableArrayList();
	private final TableView<SaleRow> table = new TableView<>();
	private final Label totalLbl = new Label("Gross Profit: 0.00");
	
	public SalesAndCOGPanelFX(@SuppressWarnings("unused") Stage primaryStage)
	{ // kept signature compatible with caller
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		// demo row
		this.rows.add(new SaleRow(LocalDate.now(), "Book", 10, new BigDecimal("25.00"),
			new BigDecimal("10.00")));
		updateTotals();
	}
	
	/* ------------------------------------------------------------------ */
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
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
	
	private static TableColumn<SaleRow, String> col(String t, String p)
	{
		TableColumn<SaleRow, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
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
			}
			
		});
		HBox box = new HBox(10, add, del, this.totalLbl);
		box.setPadding(new Insets(8));
		return box;
	}
	
	private void saleDialog(SaleRow existing)
	{
		Dialog<SaleRow> dlg = new Dialog<>();
		dlg.setTitle(existing == null ? "Add Sale" : "Edit Sale");
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		DatePicker dateP = new DatePicker(
	        existing == null ? LocalDate.now()
	                         : LocalDate.parse(existing.getDate()));   // String → LocalDate

	TextField itemF  = new TextField(
	        existing == null ? ""
	                         : existing.getItem());

	TextField qtyF   = new TextField(
	        existing == null ? "1"
	                         : String.valueOf(existing.getQty()));     // Integer → String

	TextField priceF = new TextField(
	        existing == null ? "0.00"
	                         : existing.getPrice().toPlainString());  // BigDecimal → String

	TextField costF  = new TextField(
	        existing == null ? "0.00"
	                         : existing.getCost().toPlainString());   // BigDecimal → String
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
		});
	}
	
	private void updateTotals()
	{
		BigDecimal profit =
			this.rows.stream().map(r -> r.margin.get()).reduce(BigDecimal.ZERO, BigDecimal::add);
		this.totalLbl.setText("Gross Profit: " + profit);
	}
	
	/* ------------------------------------------------------------------ */
	public static class SaleRow
	{
		final String id = UUID.randomUUID().toString();
		final SimpleStringProperty date = new SimpleStringProperty();
		final SimpleStringProperty item = new SimpleStringProperty();
		final SimpleObjectProperty<Integer> qty = new SimpleObjectProperty<>();
		final SimpleObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
		final SimpleObjectProperty<BigDecimal> cost = new SimpleObjectProperty<>();
		final SimpleObjectProperty<BigDecimal> margin = new SimpleObjectProperty<>();
		
		SaleRow(LocalDate d, String it, int q, BigDecimal p, BigDecimal c)
		{
			this.date.set(d.toString());
			this.item.set(it);
			this.qty.set(q);
			this.price.set(p);
			this.cost.set(c);
			calcMargin();
		}
		
		void calcMargin()
		{
			this.margin.set(this.price.get().subtract(this.cost.get()).multiply(new BigDecimal(this.qty.get())));
		} // extended margin = qty*(price-cost)
		
		void copyFrom(SaleRow other)
		{
			this.date.set(other.date.get());
			this.item.set(other.item.get());
			this.qty.set(other.qty.get());
			this.price.set(other.price.get());
			this.cost.set(other.cost.get());
			calcMargin();
		}
		
		public String getDate()
		{
			return this.date.get();
		}
		
		public String getItem()
		{
			return this.item.get();
		}
		
		public int getQty()
		{
			return this.qty.get();
		}
		
		public BigDecimal getPrice()
		{
			return this.price.get();
		}
		
		public BigDecimal getCost()
		{
			return this.cost.get();
		}
		
		public BigDecimal getMargin()
		{
			return this.margin.get();
		}
		
	}
	
}
