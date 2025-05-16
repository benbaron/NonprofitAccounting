
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import nonprofitbookkeeping.model.InventoryItem;
import nonprofitbookkeeping.service.InventoryService;

/**
 * JavaFX port of {@code InventoryPanel}. Shows capital assets / inventory with
 * add, edit, delete and a simple "Apply Depreciation" button that calls the
 * existing {@link InventoryService#applyYearlyDepreciation()} helper.
 */
public class InventoryPanelFX extends BorderPane
{
	
	private final InventoryService service;
	private final ObservableList<InventoryRow> rows = FXCollections.observableArrayList();
	private final TableView<InventoryRow> table = new TableView<>();
	
	public InventoryPanelFX(InventoryService service)
	{
		this.service = service;
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		refresh();
	}
	
	/* ------------------------------------------------------------------ */
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<InventoryRow, String> idCol = col("ID", "id");
		TableColumn<InventoryRow, String> nameCol = col("Item", "name");
		TableColumn<InventoryRow, BigDecimal> costCol = new TableColumn<>("Cost");
		costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
		TableColumn<InventoryRow, String> dateCol = col("Acquired", "acquired");
		TableColumn<InventoryRow, BigDecimal> depCol = new TableColumn<>("Accum. Depr.");
		depCol.setCellValueFactory(new PropertyValueFactory<>("accDep"));
		TableColumn<InventoryRow, BigDecimal> nbvCol = new TableColumn<>("Net Book Value");
		nbvCol.setCellValueFactory(new PropertyValueFactory<>("netValue"));
		this.table.getColumns().addAll(idCol, nameCol, costCol, dateCol, depCol, nbvCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.rows);
	}
	
	private TableColumn<InventoryRow, String> col(String title, String prop)
	{
		TableColumn<InventoryRow, String> c = new TableColumn<>(title);
		c.setCellValueFactory(new PropertyValueFactory<>(prop));
		return c;
	}
	
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
			}
			
		});
		depr.setOnAction(e -> {
			this.service.applyYearlyDepreciation();
			refresh();
		});
		return new ToolBar(add, edit, del, new Separator(), depr);
	}
	
	private void itemDialog(InventoryRow existing)
	{
		Dialog<InventoryRow> dlg = new Dialog<>();
		dlg.setTitle(existing == null ? "Add Item" : "Edit Item");
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		TextField nameF = new TextField();
		TextField costF = new TextField();
		DatePicker dateP = new DatePicker(LocalDate.now());
		TextField lifeF = new TextField("5");
		
		if (existing != null)
		{
			nameF.setText(existing.name);
			costF.setText(existing.cost.toPlainString());
			dateP.setValue(LocalDate.parse(existing.acquired));
			lifeF.setText(existing.lifeYears + "");
		}
		
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(8);
		g.setPadding(new Insets(10));
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
						existing == null ? UUID.randomUUID().toString() : existing.id,
						nameF.getText(), cost, dateP.getValue().toString(), life);
					return new InventoryRow(item);
				}
				catch (Exception ex)
				{
					new Alert(Alert.AlertType.ERROR, "Invalid input").showAndWait();
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
		});
	}
	
	private void refresh()
	{
		this.rows.clear();
		List<InventoryItem> list = this.service.listItems();
		list.forEach(i -> this.rows.add(new InventoryRow(i)));
	}
	
	/* Wrapper row */
	public static class InventoryRow
	{
		String id, name, acquired;
		BigDecimal cost, accDep, netValue;
		int lifeYears;
		
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
		
		public String getId()
		{
			return this.id;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public String getAcquired()
		{
			return this.acquired;
		}
		
		public BigDecimal getCost()
		{
			return this.cost;
		}
		
		public BigDecimal getAccDep()
		{
			return this.accDep;
		}
		
		public BigDecimal getNetValue()
		{
			return this.netValue;
		}
		
		InventoryItem toItem()
		{
			return new InventoryItem(this.id, 
				this.name, 
				this.cost, 
				this.acquired, 
				this.lifeYears).withAccumDep(this.accDep);
		}
		
	}
	
}
