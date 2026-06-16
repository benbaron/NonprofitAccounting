package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.service.AssetRecordService;
import org.nonprofitbookkeeping.ui.AppPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Asset register panel backed by {@link AssetRecordService} data.
 */
public class AssetsRegisterPanel implements AppPanel
{
	private static final Logger LOG = LoggerFactory.getLogger(AssetsRegisterPanel.class);

	private final BorderPane root = new BorderPane();
	private final TableView<AssetRow> table = new TableView<>();
	private final Label status = new Label("Ready");
	private final AssetRecordService assetRecordService;

	public AssetsRegisterPanel()
	{
		this(new AssetRecordService());
	}

	public AssetsRegisterPanel(AssetRecordService assetRecordService)
	{
		this.assetRecordService = Objects.requireNonNull(assetRecordService, "assetRecordService");
		this.root.setPadding(new Insets(16));

		Label title = new Label("Asset Register");
		title.getStyleClass().add("journal-entry-heading");

		Button add = new Button("+ Add Asset");
		Button delete = new Button("Delete Selected");
		Button refresh = new Button("Refresh");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, delete, refresh, save);

		this.root.setTop(new VBox(6, title, actions, new Separator()));
		configureTable();
		this.root.setCenter(this.table);
		this.root.setBottom(new VBox(new Separator(), this.status));

		add.setOnAction(e -> this.table.getItems().add(new AssetRow("asset-" + UUID.randomUUID(), "", "", "", "", null, "")));
		delete.setOnAction(e -> onDeleteSelected());
		refresh.setOnAction(e -> loadFromService());
		save.setOnAction(e -> onSave());
		loadFromService();
	}

	private void configureTable()
	{
		this.table.setEditable(true);
		this.table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		this.table.getColumns().setAll(
			col("Asset ID", AssetRow::assetIdProperty, AssetRow::setAssetId),
			col("Acquired", AssetRow::dateAcquiredProperty, AssetRow::setDateAcquired),
			col("Description", AssetRow::descriptionProperty, AssetRow::setDescription),
			col("Count", AssetRow::itemCountProperty, AssetRow::setItemCount),
			col("Approx Value", AssetRow::approxValueTotalProperty, AssetRow::setApproxValueTotal),
			itemTypeCol("Item Type"),
			col("Accum Depreciation", AssetRow::accumulatedDepreciationProperty, AssetRow::setAccumulatedDepreciation));
	}

	private TableColumn<AssetRow, String> col(String name,
		java.util.function.Function<AssetRow, SimpleStringProperty> propertyGetter,
		java.util.function.BiConsumer<AssetRow, String> setter)
	{
		TableColumn<AssetRow, String> col = new TableColumn<>(name);
		col.setPrefWidth(defaultColumnWidth(name));
		col.setMinWidth(90);
		col.setCellValueFactory(v -> propertyGetter.apply(v.getValue()));
		col.setCellFactory(c -> new FocusCommitTextFieldTableCell<>());
		col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
		return col;
	}

	private double defaultColumnWidth(String name)
	{
		return switch (name)
		{
			case "Asset ID" -> 220;
			case "Acquired" -> 150;
			case "Description" -> 360;
			case "Count" -> 120;
			case "Approx Value" -> 180;
			case "Accum Depreciation" -> 220;
			default -> 160;
		};
	}

	private TableColumn<AssetRow, AssetItemType> itemTypeCol(String name)
	{
		TableColumn<AssetRow, AssetItemType> col = new TableColumn<>(name);
		col.setPrefWidth(220);
		col.setMinWidth(90);
		col.setCellValueFactory(v -> v.getValue().itemTypeProperty());
		col.setCellFactory(column -> {
			ComboBoxTableCell<AssetRow, AssetItemType> cell = new ComboBoxTableCell<>();
			cell.getItems().setAll(AssetItemType.values());
			cell.setConverter(new javafx.util.StringConverter<>()
			{
				@Override
				public String toString(AssetItemType value)
				{
					return value == null ? "" : value.displayName();
				}

				@Override
				public AssetItemType fromString(String text)
				{
					return AssetItemType.fromStorageValue(text);
				}
			});
			return cell;
		});
		col.setOnEditCommit(event -> event.getRowValue().setItemType(event.getNewValue()));
		return col;
	}

	private void loadFromService()
	{
		if (!Database.isInitialized())
		{
			this.table.getItems().clear();
			this.status.setText("Database not initialized yet. Open or create a company to load assets.");
			return;
		}
		try
		{
			this.table.setItems(FXCollections.observableArrayList(
				this.assetRecordService.listAll().stream().map(AssetRow::fromRecord).toList()));
			this.table.getSelectionModel().clearSelection();
			this.status.setText("Loaded " + this.table.getItems().size() + " asset row(s).");
		}
		catch (SQLException | RuntimeException ex)
		{
			LOG.warn("Failed to load asset rows", ex);
			this.table.getItems().clear();
			this.status.setText("Failed to load asset rows: " + ex.getMessage());
		}
	}

	@Override
	public String title()
	{
		return "Asset Register";
	}

	@Override
	public Node root()
	{
		return this.root;
	}

	@Override
	public void onSave()
	{
		if (!Database.isInitialized())
		{
			this.status.setText("Open or create a company before saving assets.");
			return;
		}
		try
		{
			List<AssetRecord> recordsToSave = new ArrayList<>();
			for (int i = 0; i < this.table.getItems().size(); i++)
			{
				recordsToSave.add(this.table.getItems().get(i).toRecord(i + 1));
			}

			for (AssetRecord record : recordsToSave)
			{
				this.assetRecordService.save(record);
			}

			this.table.setItems(FXCollections.observableArrayList(
				recordsToSave.stream().map(AssetRow::fromRecord).toList()));
			this.status.setText("Saved " + recordsToSave.size() + " asset row(s).");
		}
		catch (SQLException | RuntimeException ex)
		{
			LOG.warn("Asset register save failed", ex);
			this.status.setText("Failed to save assets: " + ex.getMessage());
		}
	}

	@Override
	public void onDelete()
	{
		onDeleteSelected();
	}

	@Override
	public void onCancel()
	{
		loadFromService();
	}

	private void onDeleteSelected()
	{
		AssetRow selected = this.table.getSelectionModel().getSelectedItem();
		if (selected == null)
		{
			this.status.setText("Select a row to delete.");
			return;
		}
		if (!Database.isInitialized())
		{
			this.table.getItems().remove(selected);
			this.status.setText("Removed unsaved row.");
			return;
		}
		try
		{
			int deleted = this.assetRecordService.delete(selected.deleteKey());
			this.table.getItems().remove(selected);
			this.status.setText(deleted > 0 ? "Deleted asset " + selected.deleteKey() : "Removed unsaved row.");
		}
		catch (SQLException | RuntimeException ex)
		{
			LOG.warn("Asset register delete failed", ex);
			this.status.setText("Failed to delete asset: " + ex.getMessage());
		}
	}

	public static final class AssetRow
	{
		private final SimpleStringProperty assetId;
		private final SimpleStringProperty dateAcquired;
		private final SimpleStringProperty description;
		private final SimpleStringProperty itemCount;
		private final SimpleStringProperty approxValueTotal;
		private final SimpleObjectProperty<AssetItemType> itemType;
		private final SimpleStringProperty accumulatedDepreciation;
		private final AssetRecord sourceRecord;

		public AssetRow(String assetId,
			String dateAcquired,
			String description,
			String itemCount,
			String approxValueTotal,
			AssetItemType itemType,
			String accumulatedDepreciation)
		{
			this(assetId, dateAcquired, description, itemCount, approxValueTotal, itemType, accumulatedDepreciation, null);
		}

		private AssetRow(String assetId,
			String dateAcquired,
			String description,
			String itemCount,
			String approxValueTotal,
			AssetItemType itemType,
			String accumulatedDepreciation,
			AssetRecord sourceRecord)
		{
			this.assetId = new SimpleStringProperty(assetId);
			this.dateAcquired = new SimpleStringProperty(dateAcquired);
			this.description = new SimpleStringProperty(description);
			this.itemCount = new SimpleStringProperty(itemCount);
			this.approxValueTotal = new SimpleStringProperty(approxValueTotal);
			this.itemType = new SimpleObjectProperty<>(itemType);
			this.accumulatedDepreciation = new SimpleStringProperty(accumulatedDepreciation);
			this.sourceRecord = sourceRecord;
		}

		public static AssetRow fromRecord(AssetRecord record)
		{
			return new AssetRow(
				record.assetId(),
				record.dateAcquired() == null ? "" : record.dateAcquired().toString(),
				record.description() == null ? "" : record.description(),
				record.itemCount() == null ? "" : record.itemCount().toString(),
				record.approxValueTotal() == null ? "" : record.approxValueTotal().toPlainString(),
				record.itemType(),
				record.accumulatedDepreciation() == null ? "" : record.accumulatedDepreciation().toPlainString(),
				record);
		}

		public AssetRecord toRecord(int rowNumber)
		{
			String assetIdValue = nonBlankOrThrow(this.assetId.get(), "assetId", rowNumber);
			return new AssetRecord(
				assetIdValue,
				parseLocalDate(this.dateAcquired.get(), rowNumber),
				nullIfBlank(this.description.get()),
				parseInteger(this.itemCount.get(), rowNumber),
				parseBigDecimal(this.approxValueTotal.get(), rowNumber, "approx value"),
				parseBigDecimal(this.accumulatedDepreciation.get(), rowNumber, "accumulated depreciation"),
				this.sourceRecord == null ? null : this.sourceRecord.valuePerItem(),
				this.itemType.get(),
				this.sourceRecord == null ? null : this.sourceRecord.usedFor(),
				this.sourceRecord == null ? null : this.sourceRecord.lotPaidTotal(),
				this.sourceRecord == null ? null : this.sourceRecord.lotItemCount(),
				this.sourceRecord == null ? null : this.sourceRecord.currentGuardian(),
				this.sourceRecord == null ? null : this.sourceRecord.guardianshipDetails(),
				this.sourceRecord == null ? null : this.sourceRecord.removalDetails(),
				this.sourceRecord == null ? Map.of() : this.sourceRecord.extensions());
		}

		public String getAssetId() { return this.assetId.get(); }
		public String getDateAcquired() { return this.dateAcquired.get(); }
		public String getDescription() { return this.description.get(); }
		public String getItemCount() { return this.itemCount.get(); }
		public String getApproxValueTotal() { return this.approxValueTotal.get(); }
		public AssetItemType getItemType() { return this.itemType.get(); }
		public String getAccumulatedDepreciation() { return this.accumulatedDepreciation.get(); }

		public SimpleStringProperty assetIdProperty() { return this.assetId; }
		public SimpleStringProperty dateAcquiredProperty() { return this.dateAcquired; }
		public SimpleStringProperty descriptionProperty() { return this.description; }
		public SimpleStringProperty itemCountProperty() { return this.itemCount; }
		public SimpleStringProperty approxValueTotalProperty() { return this.approxValueTotal; }
		public SimpleObjectProperty<AssetItemType> itemTypeProperty() { return this.itemType; }
		public SimpleStringProperty accumulatedDepreciationProperty() { return this.accumulatedDepreciation; }

		public void setAssetId(String value) { this.assetId.set(value == null ? "" : value); }
		public void setDateAcquired(String value) { this.dateAcquired.set(value == null ? "" : value); }
		public void setDescription(String value) { this.description.set(value == null ? "" : value); }
		public void setItemCount(String value) { this.itemCount.set(value == null ? "" : value); }
		public void setApproxValueTotal(String value) { this.approxValueTotal.set(value == null ? "" : value); }
		public void setItemType(AssetItemType value) { this.itemType.set(value); }
		public void setAccumulatedDepreciation(String value) { this.accumulatedDepreciation.set(value == null ? "" : value); }

		private String deleteKey()
		{
			String currentAssetId = getAssetId();
			if (currentAssetId != null && !currentAssetId.isBlank())
			{
				return currentAssetId.trim();
			}
			return this.sourceRecord != null ? this.sourceRecord.assetId() : "";
		}

		private static String nonBlankOrThrow(String raw, String field, int rowNumber)
		{
			if (raw == null || raw.isBlank())
			{
				throw new IllegalArgumentException("Row " + rowNumber + " has blank " + field + ".");
			}
			return raw.trim();
		}

		private static LocalDate parseLocalDate(String raw, int rowNumber)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			try
			{
				return LocalDate.parse(raw.trim());
			}
			catch (DateTimeParseException ex)
			{
				throw new IllegalArgumentException("Row " + rowNumber + " has invalid date: '" + raw + "'. Use YYYY-MM-DD.");
			}
		}

		private static Integer parseInteger(String raw, int rowNumber)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			try
			{
				return Integer.parseInt(raw.trim());
			}
			catch (NumberFormatException ex)
			{
				throw new IllegalArgumentException("Row " + rowNumber + " has invalid item count: '" + raw + "'.");
			}
		}

		private static BigDecimal parseBigDecimal(String raw, int rowNumber, String fieldName)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			try
			{
				return new BigDecimal(raw.trim());
			}
			catch (NumberFormatException ex)
			{
				throw new IllegalArgumentException("Row " + rowNumber + " has invalid " + fieldName + ": '" + raw + "'.");
			}
		}

		private static String nullIfBlank(String raw)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			return raw.trim();
		}
	}
}
