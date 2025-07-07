
package nonprofitbookkeeping.ui.panels;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.service.GrantsService;

/**
 * JavaFX port of {@code GrantsPanel}. Displays grant records in a table with a
 * Refresh button. Uses the same FileBasedGrantsService for data.
 */
public class GrantsPanelFX extends BorderPane
{
	
	/** Service layer for grant data operations. */
	private final GrantsService grantsService;
	/** ObservableList to hold {@link GrantRow} objects for display in the table. */
	private final ObservableList<GrantRow> rows = FXCollections.observableArrayList();
	/** TableView to display the list of grants. */
	private final TableView<GrantRow> table = new TableView<>();
	
	/**
	 * Constructs a new {@code GrantsPanelFX}.
	 * Initializes the panel with a {@link GrantsService} instance, a table to display grant information,
	 * and a "Refresh" button to reload grant data.
	 *
	 * @param primaryStage The primary stage of the application. This parameter is currently not used within the constructor.
	 */
	public GrantsPanelFX(Stage primaryStage)
	{
		this.grantsService = new GrantsService();
		setPadding(new Insets(10));
		buildTable();
		setCenter(new TitledPane("Grant List", this.table)
		{
			{
				setCollapsible(false);
			}
			
		});
		Button refresh = new Button("Refresh");
		refresh.setOnAction(e -> loadGrantData());
		setBottom(new ToolBar(refresh));
		loadGrantData();
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying grant records.
	 * It defines columns for Grant ID, Grantor, Amount, Date Awarded, Purpose, and Status,
	 * using the {@link #col(String, String)} utility method which internally uses {@link PropertyValueFactory}.
	 * The table is bound to the {@link #rows} observable list and a column resize policy is set.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * (used in the {@code col} method) uses reflection and can lead to type safety warnings. "deprecation" might
	 * relate to older patterns of using PropertyValueFactory.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<GrantRow, String> idCol = col("Grant ID", "grantId");
		TableColumn<GrantRow, String> grantorCol = col("Grantor", "grantor");
		TableColumn<GrantRow, String> amtCol = col("Amount", "amount");
		TableColumn<GrantRow, String> dateCol = col("Date Awarded", "dateAwarded");
		TableColumn<GrantRow, String> purpCol = col("Purpose", "purpose");
		TableColumn<GrantRow, String> statusCol = col("Status", "status");
		this.table.getColumns().addAll(idCol, grantorCol, amtCol, dateCol, purpCol, statusCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.rows);
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for the grants table.
	 * This method simplifies column creation by taking a title and the property name
	 * from {@link GrantRow} that the column should bind to.
	 *
	 * @param title The title of the column for the table header.
	 * @param prop The name of the property in {@link GrantRow} to bind this column to (must match a getter, e.g., "grantId" for getGrantId()).
	 * @return A configured {@link TableColumn} for displaying String data from a {@link GrantRow}.
	 */
	private static TableColumn<GrantRow, String> col(String title, String prop)
	{
		TableColumn<GrantRow, String> c = new TableColumn<>(title);
		c.setCellValueFactory(new PropertyValueFactory<>(prop));
		return c;
	}
	
	/**
	 * Loads grant data from the {@link #grantsService} and populates the table.
	 * It clears any existing rows in the table, retrieves all grants from the service,
	 * converts each {@link Grant} object into a {@link GrantRow}, and adds them to the
	 * {@link #rows} observable list, which updates the table view.
	 */
	private void loadGrantData()
	{
		this.rows.clear();
		List<Grant> list = this.grantsService.getAllGrants();
		for (var g : list)
			this.rows.add(new GrantRow(g));
	}
	
	/**
	 * A simple data class (POJO) used to represent a row in the grants {@link TableView}.
	 * It wraps a {@link Grant} object, formatting some fields (like amount) as Strings for display.
	 */
	public static class GrantRow
	{
		/** The ID of the grant. */
		private final String grantId;
		/** The name of the entity that awarded the grant. */
		private final String grantor;
		/** The monetary amount of the grant, formatted as a currency string. */
		private final String amount;
		/** The date the grant was awarded, as a string. */
		private final String dateAwarded;
		/** The purpose or description of the grant. */
		private final String purpose;
		/** The current status of the grant (e.g., "Awarded", "Pending", "Closed"). */
		private final String status;
		
		/**
		 * Constructs a {@code GrantRow} from a {@link Grant} object.
		 * Initializes all fields by extracting and formatting data from the given grant.
		 *
		 * @param g The {@link Grant} object from which to create the row data. Must not be null.
		 */
		GrantRow(Grant g)
		{
			this.grantId = g.getGrantId();
			this.grantor = g.getGrantor();
			this.amount = String.format("$%.2f", g.getAmount());
			this.dateAwarded = g.getDateAwarded();
			this.purpose = g.getPurpose();
			this.status = g.getStatus();
		}
		
		/**
		 * Gets the grant ID.
		 * @return The grant ID string.
		 */
		public String getGrantId()
		{
			return this.grantId;
		}
		
		/**
		 * Gets the name of the grantor.
		 * @return The grantor's name.
		 */
		public String getGrantor()
		{
			return this.grantor;
		}
		
		/**
		 * Gets the formatted amount of the grant.
		 * @return The grant amount as a currency-formatted string (e.g., "$1,000.00").
		 */
		public String getAmount()
		{
			return this.amount;
		}
		
		/**
		 * Gets the date the grant was awarded.
		 * @return The date awarded, as a string.
		 */
		public String getDateAwarded()
		{
			return this.dateAwarded;
		}
		
		/**
		 * Gets the purpose of the grant.
		 * @return The purpose string.
		 */
		public String getPurpose()
		{
			return this.purpose;
		}
		
		/**
		 * Gets the status of the grant.
		 * @return The status string.
		 */
		public String getStatus()
		{
			return this.status;
		}
		
	}
	
	
}
