package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.service.AssetRecordService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Asset register panel backed by {@link AssetRecordService} data.
 */
public class AssetsRegisterPanel implements AppPanel
{
	private final BorderPane root = new BorderPane();
	private final TableView<AssetRow> table = new TableView<>();
	private final Label status = new Label("Ready");
	private final AssetRecordService assetRecordService;

	public AssetsRegisterPanel()
	{
		this(new AssetRecordService());
	}

	AssetsRegisterPanel(AssetRecordService assetRecordService)
	{
		this.assetRecordService = assetRecordService;
		root.setPadding(new Insets(8));

		Label title = new Label("Asset Register");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Asset");
		Button refresh = new Button("Refresh");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, refresh, save);

		root.setTop(new VBox(6, title, actions, new Separator()));
		configureTable();
		root.setCenter(table);
		root.setBottom(new VBox(new Separator(), status));

		add.setOnAction(e -> table.getItems().add(new AssetRow("asset-" + UUID.randomUUID(), "", "", "", "")));
		refresh.setOnAction(e -> loadFromService());
		save.setOnAction(e -> onSave());
		loadFromService();
	}

	private void configureTable()
	{
		table.setEditable(true);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		table.getColumns().add(col("Asset ID", AssetRow::assetIdProperty));
		table.getColumns().add(col("Acquired", AssetRow::dateAcquiredProperty));
		table.getColumns().add(col("Description", AssetRow::descriptionProperty));
		table.getColumns().add(col("Count", AssetRow::itemCountProperty));
		table.getColumns().add(col("Approx Value", AssetRow::approxValueTotalProperty));
	}

	private TableColumn<AssetRow, String> col(String name,
		java.util.function.Function<AssetRow, SimpleStringProperty> propertyGetter)
	{
		TableColumn<AssetRow, String> col = new TableColumn<>(name);
		col.setCellValueFactory(v -> propertyGetter.apply(v.getValue()));
		col.setCellFactory(TextFieldTableCell.forTableColumn());
		return col;
	}

	private void loadFromService()
	{
		try
		{
			List<AssetRecord> records = assetRecordService.listAll();
			table.setItems(FXCollections.observableArrayList(records.stream().map(AssetRow::fromRecord).toList()));
			status.setText("Loaded " + records.size() + " asset record(s)");
		}
		catch (SQLException ex)
		{
			status.setText("Failed to load asset records: " + ex.getMessage());
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
		return root;
	}

	@Override
	public void onSave()
	{
		try
		{
			int rowNumber = 1;
			for (AssetRow row : table.getItems())
			{
				assetRecordService.save(row.toRecord(rowNumber));
				rowNumber++;
			}
			status.setText("Saved " + table.getItems().size() + " asset record(s)");
		}
		catch (IllegalArgumentException ex)
		{
			status.setText("Validation error: " + ex.getMessage());
		}
		catch (SQLException ex)
		{
			status.setText("Failed to save asset records: " + ex.getMessage());
		}
	}

	public static final class AssetRow
	{
		private final SimpleStringProperty assetId;
		private final SimpleStringProperty dateAcquired;
		private final SimpleStringProperty description;
		private final SimpleStringProperty itemCount;
		private final SimpleStringProperty approxValueTotal;
		private final AssetRecord sourceRecord;

		public AssetRow(String assetId,
			String dateAcquired,
			String description,
			String itemCount,
			String approxValueTotal)
		{
			this(assetId, dateAcquired, description, itemCount, approxValueTotal, null);
		}

		private AssetRow(String assetId,
			String dateAcquired,
			String description,
			String itemCount,
			String approxValueTotal,
			AssetRecord sourceRecord)
		{
			this.assetId = new SimpleStringProperty(assetId);
			this.dateAcquired = new SimpleStringProperty(dateAcquired);
			this.description = new SimpleStringProperty(description);
			this.itemCount = new SimpleStringProperty(itemCount);
			this.approxValueTotal = new SimpleStringProperty(approxValueTotal);
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
				sourceRecord == null ? null : sourceRecord.valuePerItem(),
				sourceRecord == null ? null : sourceRecord.itemType(),
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

		public SimpleStringProperty assetIdProperty() { return assetId; }
		public SimpleStringProperty dateAcquiredProperty() { return dateAcquired; }
		public SimpleStringProperty descriptionProperty() { return description; }
		public SimpleStringProperty itemCountProperty() { return itemCount; }
		public SimpleStringProperty approxValueTotalProperty() { return approxValueTotal; }

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
