
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import nonprofitbookkeeping.util.FormatUtils;
import java.util.List;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.service.GrantsService;

/**
 * JavaFX port of {@code GrantsPanel}. Displays grant records in a table with a
 * Refresh button. Grants are now persisted via database services rather than
 * directly manipulating company files.
 */
public class GrantsPanelFX extends BorderPane
{
	
	/** Service layer for grant data operations. */
	private final GrantsService grantsService;
        /** Identifier for the company whose grants are being managed. */
        private String companyId;
	/** ObservableList to hold {@link GrantRow} objects for display in the table. */
	private final ObservableList<GrantRow> rows = FXCollections.observableArrayList();
	/** TableView to display the list of grants. */
	private final TableView<GrantRow> table = new TableView<>();
	
	/**
	 * Constructs a new {@code GrantsPanelFX}.
	 * Initializes the panel with a {@link GrantsService} instance, a table to display grant information,
	 * and a "Refresh" button to reload grant data.
	    */
        public GrantsPanelFX(GrantsService service, String companyId)
        {
                this.grantsService = service != null ? service : new GrantsService();
                this.companyId = companyId;
		
		setPadding(new Insets(10));
		buildTable();
		setCenter(new TitledPane("Grant List", this.table)
		{
			{
				setCollapsible(false);
			}
			
		});
		Button refresh = new Button("Refresh");
		Button add = new Button("Add Grant");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		
		refresh.setOnAction(e -> loadGrantData());
		add.setOnAction(e -> grantDialog(null));
		edit.setOnAction(e -> {
			GrantRow sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
				grantDialog(toGrant(sel));
		});
		del.setOnAction(e -> {
			GrantRow sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.grantsService.removeGrant(sel.getGrantId());
				refresh();
				save();
			}
			
		});
		
		setBottom(new ToolBar(refresh, new Separator(), new HBox(5, add, edit, del)));
		
                if (this.companyId != null)
                {

                        try
                        {
                                this.grantsService.loadGrantsFromDatabase(this.companyId);
                        }
                        catch (Exception ex)
                        {
                                ex.printStackTrace();
                        }

                }
		
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
	
	/** Displays a dialog for adding or editing a grant. */
	private void grantDialog(Grant existing)
	{
		Dialog<Grant> dlg = new Dialog<>();
		dlg.setTitle(existing == null ? "Add Grant" : "Edit Grant");
		ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);
		
		TextField grantorF = new TextField();
		TextField amountF = new TextField();
		TextField dateF = new TextField();
		TextField purposeF = new TextField();
		TextField statusF = new TextField();
		
		if (existing != null)
		{
			grantorF.setText(existing.getGrantor());
                       amountF
                               .setText(existing.getAmount() != null ? FormatUtils.formatCurrency(existing.getAmount()) : "");
			dateF.setText(existing.getDateAwarded());
			purposeF.setText(existing.getPurpose());
			statusF.setText(existing.getStatus());
		}
		
		dlg.getDialogPane()
			.setContent(new VBox(8, new HBox(5, new Label("Grantor:"), grantorF),
				new HBox(5, new Label("Amount:"), amountF), new HBox(5, new Label("Date:"), dateF),
				new HBox(5, new Label("Purpose:"), purposeF),
				new HBox(5, new Label("Status:"), statusF)));
		
		dlg.setResultConverter(btn -> btn == okType ?
			new Grant(existing == null ? UUID.randomUUID().toString() : existing.getGrantId(),
				grantorF.getText(),
				new BigDecimal(amountF.getText().isBlank() ? "0" : amountF.getText()),
				dateF.getText(), purposeF.getText(), statusF.getText()) :
			null);
		
		dlg.showAndWait().ifPresent(g -> {
			
			if (existing == null)
				this.grantsService.addGrant(g);
			else
			{
				this.grantsService.removeGrant(existing.getGrantId());
				this.grantsService.addGrant(g);
			}
			
			refresh();
			save();
		});
	}
	
	/** Refreshes the table from the service layer. */
	private void refresh()
	{
		loadGrantData();
		this.table.refresh();
	}
	
	/** Saves grants to the company file if set. */
	private void save()
	{
		
                if (this.companyId != null)
                {

                        try
                        {
                                this.grantsService.saveGrantsToDatabase(this.companyId);
                        }
                        catch (Exception ex)
                        {
                                ex.printStackTrace();
                        }

                }
		
	}
	
	private Grant toGrant(GrantRow row)
	{
		String amt = row.amount.replace("$", "").replace(",", "");
		return new Grant(row.getGrantId(), row.getGrantor(), new BigDecimal(amt),
			row.getDateAwarded(), row.getPurpose(), row.getStatus());
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
                       this.amount = FormatUtils.formatCurrency(g.getAmount());
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
