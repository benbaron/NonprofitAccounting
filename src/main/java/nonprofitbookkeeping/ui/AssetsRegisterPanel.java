package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.service.AssetRecordService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Asset register panel backed by {@link AssetRecordService} data.
 */
public class AssetsRegisterPanel implements AppPanel
{
	private final GenericRecordEditorPanel delegate;

	public AssetsRegisterPanel()
	{
			this(new GenericRecordEditorPanel(
				"Asset Register",
				"imported_asset_record",
				"asset_id",
				() -> "asset-" + UUID.randomUUID(),
				Set.of("extensions_json")));
	}

	public AssetsRegisterPanel(AssetRecordService assetRecordService)
	{
		this.assetRecordService = assetRecordService;
		root.setPadding(new Insets(8));

		Label title = new Label("Asset Register");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Asset");
		Button delete = new Button("Delete Selected");
		Button refresh = new Button("Refresh");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, delete, refresh, save);

		root.setTop(new VBox(6, title, actions, new Separator()));
		configureTable();
		root.setCenter(table);
		root.setBottom(new VBox(new Separator(), status));

		add.setOnAction(e -> table.getItems().add(new AssetRow("asset-" + UUID.randomUUID(), "", "", "", "", null, "")));
		delete.setOnAction(e -> onDeleteSelected());
		refresh.setOnAction(e -> loadFromService());
		save.setOnAction(e -> onSave());
		loadFromService();
	}

	private void configureTable()
	{
		table.setEditable(true);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		table.getColumns().add(col("Asset ID", AssetRow::assetIdProperty, AssetRow::setAssetId));
		table.getColumns().add(col("Acquired", AssetRow::dateAcquiredProperty, AssetRow::setDateAcquired));
		table.getColumns().add(col("Description", AssetRow::descriptionProperty, AssetRow::setDescription));
		table.getColumns().add(col("Count", AssetRow::itemCountProperty, AssetRow::setItemCount));
		table.getColumns().add(col("Approx Value", AssetRow::approxValueTotalProperty, AssetRow::setApproxValueTotal));
		table.getColumns().add(itemTypeCol("Item Type"));
		table.getColumns().add(col("Accum Depreciation", AssetRow::accumulatedDepreciationProperty, AssetRow::setAccumulatedDepreciation));
	}

	private TableColumn<AssetRow, String> col(String name,
		java.util.function.Function<AssetRow, SimpleStringProperty> propertyGetter,
		java.util.function.BiConsumer<AssetRow, String> setter)
	{
		TableColumn<AssetRow, String> col = new TableColumn<>(name);
		col.setCellValueFactory(v -> propertyGetter.apply(v.getValue()));
		col.setCellFactory(c -> new FocusCommitTextFieldTableCell<>());
		col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
		return col;
	}

	private TableColumn<AssetRow, AssetItemType> itemTypeCol(String name)
	{
		TableColumn<AssetRow, AssetItemType> col = new TableColumn<>(name);
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
		this.delegate = delegate;
	}

	@Override
	public String title()
	{
		return delegate.title();
	}

	@Override
	public Node root()
	{
		return delegate.root();
	}

	@Override
	public void onSave()
	{
		delegate.onSave();
	}

	private void onDeleteSelected()
	{
		AssetRow selected = table.getSelectionModel().getSelectedItem();
		if (selected == null)
		{
			status.setText("Select a row to delete.");
			return;
		}
		try
		{
			int deleted = assetRecordService.delete(selected.deleteKey());
			table.getItems().remove(selected);
			status.setText(deleted > 0 ? "Deleted asset " + selected.deleteKey() : "Removed unsaved row.");
		}
		catch (SQLException | RuntimeException ex)
		{
			LOG.warn("Asset register delete failed", ex);
			status.setText("Failed to delete asset: " + ex.getMessage());
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
			String assetIdValue = nonBlankOrThrow(assetId.get(), "assetId", rowNumber);
			return new AssetRecord(
				assetIdValue,
				parseLocalDate(dateAcquired.get(), rowNumber),
				nullIfBlank(description.get()),
				parseInteger(itemCount.get(), rowNumber),
				parseBigDecimal(approxValueTotal.get(), rowNumber),
				parseBigDecimal(accumulatedDepreciation.get(), rowNumber),
				sourceRecord == null ? null : sourceRecord.valuePerItem(),
				itemType.get(),
				sourceRecord == null ? null : sourceRecord.usedFor(),
				sourceRecord == null ? null : sourceRecord.lotPaidTotal(),
				sourceRecord == null ? null : sourceRecord.lotItemCount(),
				sourceRecord == null ? null : sourceRecord.currentGuardian(),
				sourceRecord == null ? null : sourceRecord.guardianshipDetails(),
				sourceRecord == null ? null : sourceRecord.removalDetails(),
				sourceRecord == null ? Map.of() : sourceRecord.extensions());
		}

		public String getAssetId() { return assetId.get(); }
		public String getDateAcquired() { return dateAcquired.get(); }
		public String getDescription() { return description.get(); }
		public String getItemCount() { return itemCount.get(); }
		public String getApproxValueTotal() { return approxValueTotal.get(); }
		public AssetItemType getItemType() { return itemType.get(); }
		public String getAccumulatedDepreciation() { return accumulatedDepreciation.get(); }

		public SimpleStringProperty assetIdProperty() { return assetId; }
		public SimpleStringProperty dateAcquiredProperty() { return dateAcquired; }
		public SimpleStringProperty descriptionProperty() { return description; }
		public SimpleStringProperty itemCountProperty() { return itemCount; }
		public SimpleStringProperty approxValueTotalProperty() { return approxValueTotal; }
		public SimpleObjectProperty<AssetItemType> itemTypeProperty() { return itemType; }
		public SimpleStringProperty accumulatedDepreciationProperty() { return accumulatedDepreciation; }

		public void setAssetId(String value) { assetId.set(value == null ? "" : value); }
		public void setDateAcquired(String value) { dateAcquired.set(value == null ? "" : value); }
		public void setDescription(String value) { description.set(value == null ? "" : value); }
		public void setItemCount(String value) { itemCount.set(value == null ? "" : value); }
		public void setApproxValueTotal(String value) { approxValueTotal.set(value == null ? "" : value); }
		public void setItemType(AssetItemType value) { itemType.set(value); }
		public void setAccumulatedDepreciation(String value) { accumulatedDepreciation.set(value == null ? "" : value); }

		private String deleteKey()
		{
			return sourceRecord != null ? sourceRecord.assetId() : getAssetId();
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

		private static BigDecimal parseBigDecimal(String raw, int rowNumber)
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
				throw new IllegalArgumentException("Row " + rowNumber + " has invalid approx value: '" + raw + "'.");
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
