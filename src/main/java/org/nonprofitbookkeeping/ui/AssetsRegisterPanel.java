
package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Represents the AssetsRegisterPanel component in the nonprofit bookkeeping application.
 */
public class AssetsRegisterPanel implements AppPanel
{
	private final BorderPane root = new BorderPane();
	private final TableView<AssetRow> table = new TableView<>();
	private final Label status = new Label("Ready");
	
	public AssetsRegisterPanel()
	{
		root.setPadding(new Insets(8));
		Label title = new Label("Asset Register");
		title.getStyleClass().add("panel-title");
		
		Button add = new Button("+ Add Asset");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, save);
		
		root.setTop(new VBox(6, title, actions, new Separator()));
		
		table.setEditable(true);
		table.setColumnResizePolicy(
			TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		table.getColumns().add(col("Asset", AssetRow::assetProperty));
		table.getColumns().add(col("Category", AssetRow::categoryProperty));
		table.getColumns().add(col("Acquired", AssetRow::acquiredProperty));
		table.getColumns().add(col("Cost", AssetRow::costProperty));
		table.getColumns()
			.add(col("Depreciation %", AssetRow::depreciationRateProperty));
		table.getColumns()
			.add(col("Useful Life (yrs)", AssetRow::usefulLifeYearsProperty));
		table.getColumns().add(col("Book Value", AssetRow::bookValueProperty));
		table.getItems().addAll(
			new AssetRow("Laptop Fleet", "Equipment", "2025-03-01", "4500.00",
				"20", "5", "3600.00"),
			new AssetRow("Office Furniture", "Furniture", "2024-05-15",
				"2800.00", "10", "10", "2400.00"));
		
		root.setCenter(table);
		root.setBottom(new VBox(new Separator(), status));
		
		add.setOnAction(
			e -> table.getItems().add(
				new AssetRow("New Asset", "", "", "0.00", "0", "0", "0.00")));
		save.setOnAction(e -> onSave());
		
	}
	
	private TableColumn<AssetRow, String> col(String name,
		java.util.function.Function<AssetRow, SimpleStringProperty> prop)
	{
		TableColumn<AssetRow, String> c = new TableColumn<>(name);
		c.setCellValueFactory(v -> prop.apply(v.getValue()));
		c.setCellFactory(TextFieldTableCell.forTableColumn());
		return c;
		
	}
	
	@Override
	public String title()
	{
		return "Asset Register";
		
	}
	
	@Override
	public Node root()
	{
		return root;
		
	}
	
	@Override
	public void onSave()
	{
		status.setText("Saved " + table.getItems().size() +
			" asset row(s) with inline depreciation fields");
		
	}
	
	public static final class AssetRow
	{
		private final SimpleStringProperty asset;
		private final SimpleStringProperty category;
		private final SimpleStringProperty acquired;
		private final SimpleStringProperty cost;
		private final SimpleStringProperty depreciationRate;
		private final SimpleStringProperty usefulLifeYears;
		private final SimpleStringProperty bookValue;
		
		public AssetRow(String asset, String category, String acquired,
			String cost, String depreciationRate,
			String usefulLifeYears, String bookValue)
		{
			this.asset = new SimpleStringProperty(asset);
			this.category = new SimpleStringProperty(category);
			this.acquired = new SimpleStringProperty(acquired);
			this.cost = new SimpleStringProperty(cost);
			this.depreciationRate = new SimpleStringProperty(depreciationRate);
			this.usefulLifeYears = new SimpleStringProperty(usefulLifeYears);
			this.bookValue = new SimpleStringProperty(bookValue);
			
		}
		
		public SimpleStringProperty assetProperty()
		{
			return asset;
			
		}
		
		public SimpleStringProperty categoryProperty()
		{
			return category;
			
		}
		
		public SimpleStringProperty acquiredProperty()
		{
			return acquired;
			
		}
		
		public SimpleStringProperty costProperty()
		{
			return cost;
			
		}
		
		public SimpleStringProperty depreciationRateProperty()
		{
			return depreciationRate;
			
		}
		
		public SimpleStringProperty usefulLifeYearsProperty()
		{
			return usefulLifeYears;
			
		}
		
		public SimpleStringProperty bookValueProperty()
		{
			return bookValue;
			
		}
		
	}
	
}
