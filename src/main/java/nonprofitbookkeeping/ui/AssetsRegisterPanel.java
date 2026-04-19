package nonprofitbookkeeping.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.List;
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

	/**
	 * Creates the asset register panel.
	 */
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
		table.getColumns().add(col("Asset ID", "assetId"));
		table.getColumns().add(col("Acquired", "dateAcquired"));
		table.getColumns().add(col("Description", "description"));
		table.getColumns().add(col("Count", "itemCount"));
		table.getColumns().add(col("Approx Value", "approxValueTotal"));
	}

	private TableColumn<AssetRow, String> col(String name, String property)
	{
		TableColumn<AssetRow, String> col = new TableColumn<>(name);
		col.setCellValueFactory(new PropertyValueFactory<>(property));
		col.setCellFactory(TextFieldTableCell.forTableColumn());
		col.setOnEditCommit(event -> event.getRowValue().setValue(property, event.getNewValue()));
		return col;
	}

	private void loadFromService()
	{
		try
		{
			List<AssetRecord> records = assetRecordService.listAll();
			table.setItems(FXCollections.observableArrayList(records.stream()
				.map(AssetRow::fromRecord)
				.toList()));
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
			for (AssetRow row : table.getItems())
			{
				assetRecordService.save(row.toRecord());
			}
			status.setText("Saved " + table.getItems().size() + " asset record(s)");
		}
		catch (SQLException | RuntimeException ex)
		{
			status.setText("Failed to save asset records: " + ex.getMessage());
		}
	}

	public static final class AssetRow
	{
		private String assetId;
		private String dateAcquired;
		private String description;
		private String itemCount;
		private String approxValueTotal;

		public AssetRow(String assetId, String dateAcquired, String description,
			String itemCount, String approxValueTotal)
		{
			this.assetId = assetId;
			this.dateAcquired = dateAcquired;
			this.description = description;
			this.itemCount = itemCount;
			this.approxValueTotal = approxValueTotal;
		}

		public static AssetRow fromRecord(AssetRecord record)
		{
			return new AssetRow(
				record.assetId(),
				record.dateAcquired() == null ? "" : record.dateAcquired().toString(),
				record.description(),
				record.itemCount() == null ? "" : record.itemCount().toString(),
				record.approxValueTotal() == null ? "" : record.approxValueTotal().toPlainString());
		}

		public AssetRecord toRecord()
		{
			return new AssetRecord(
				assetId,
				parseLocalDate(dateAcquired),
				description,
				parseInteger(itemCount),
				parseBigDecimal(approxValueTotal),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				java.util.Map.of());
		}

		public String getAssetId()
		{
			return assetId;
		}

		public String getDateAcquired()
		{
			return dateAcquired;
		}

		public String getDescription()
		{
			return description;
		}

		public String getItemCount()
		{
			return itemCount;
		}

		public String getApproxValueTotal()
		{
			return approxValueTotal;
		}

		public void setValue(String property, String value)
		{
			switch (property)
			{
				case "assetId" -> assetId = value;
				case "dateAcquired" -> dateAcquired = value;
				case "description" -> description = value;
				case "itemCount" -> itemCount = value;
				case "approxValueTotal" -> approxValueTotal = value;
				default -> {
				}
			}
		}

		private static LocalDate parseLocalDate(String raw)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			return LocalDate.parse(raw);
		}

		private static Integer parseInteger(String raw)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			return Integer.parseInt(raw);
		}

		private static BigDecimal parseBigDecimal(String raw)
		{
			if (raw == null || raw.isBlank())
			{
				return null;
			}
			return new BigDecimal(raw);
		}
	}
}
