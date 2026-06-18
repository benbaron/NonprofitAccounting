
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
public class AssetsRegisterPanel implements AppPanel, AppPanel.SaveAware
{
	static final String NO_SERVICE_DATA_MESSAGE =
		"No service-backed data source is wired for this panel yet.";

	private final BorderPane root = new BorderPane();
	private final TableView<AssetRow> table = new TableView<>();
	private final Label status = new Label(NO_SERVICE_DATA_MESSAGE);
	
	public AssetsRegisterPanel()
	{
		root.setPadding(new Insets(8));
		Label title = new Label("Asset Register");
		title.getStyleClass().add("panel-title");
		
		Button add = new Button("+ Add Asset");
		Button delete = new Button("Delete Selected");
		Button save = new Button("Save");
		Button cancel = new Button("Cancel");
		HBox actions = new HBox(8, add, delete, save, cancel);
		
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
		table.setPlaceholder(new Label(NO_SERVICE_DATA_MESSAGE));
		
		root.setCenter(table);
		root.setBottom(new VBox(new Separator(), status));
		
		add.setOnAction(
			e -> status.setText(NO_SERVICE_DATA_MESSAGE));
		delete.setOnAction(e -> onDelete());
		save.setOnAction(e -> onSave());
		cancel.setOnAction(e -> onCancel());
		
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
		status.setText(NO_SERVICE_DATA_MESSAGE);
		
	}

	@Override
	public void onDelete()
	{
		AssetRow selected = table.getSelectionModel().getSelectedItem();
		if (selected == null)
		{
			status.setText("Select an asset row to delete.");
			return;
		}
		table.getItems().remove(selected);
		status.setText("Deleted selected asset row.");
	}

	@Override
	public void onCancel()
	{
		table.getSelectionModel().clearSelection();
		status.setText("Cancelled asset edit and cleared the selection.");
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
	

    @Override
    public SaveResult save()
    {
        return SaveResult.unsupported("Save is not supported because this panel has no persistence workflow yet.");
    }
}
