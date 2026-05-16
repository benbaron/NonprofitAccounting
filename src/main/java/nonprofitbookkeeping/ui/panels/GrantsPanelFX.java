
package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import nonprofitbookkeeping.util.FormatUtils;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import nonprofitbookkeeping.model.GrantTraceabilityRow;
import nonprofitbookkeeping.service.GrantTraceabilityService;
import nonprofitbookkeeping.service.GrantsService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * JavaFX port of {@code GrantsPanel}. Displays grant records in a table with a
 * Refresh button backed by the database-centric {@link GrantsService}.
 */
public class GrantsPanelFX extends BorderPane
{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(GrantsPanelFX.class);
	
	/** Service layer for grant data operations. */
	private final GrantsService grantsService;
	/** Traceability reporting service. */
	private final GrantTraceabilityService traceabilityService;
	/** ObservableList to hold {@link GrantRow} objects for display in the table. */
	private final ObservableList<GrantRow> rows =
		FXCollections.observableArrayList();
	/** TableView to display the list of grants. */
	private final TableView<GrantRow> table = new TableView<>();
	
	/**
	 * Constructs a new {@code GrantsPanelFX}.
	 * Initializes the panel with a {@link GrantsService} instance, a table to display grant information,
	 * and a "Refresh" button to reload grant data.
	 *
	 * @param service the service
	 */
	public GrantsPanelFX(GrantsService service)
	{
		this.grantsService = service != null ? service : new GrantsService();
		this.traceabilityService = new GrantTraceabilityService();
		
		setPadding(PanelChrome.PANEL_PADDING);
		setTop(PanelChrome.topSection("Grants"));
		buildTable();
		this.table.setPlaceholder(new Label("No grants recorded."));
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
		Button alerts = new Button("Compliance Alerts");
		
		refresh.setTooltip(new Tooltip("Reload grants from storage."));
		add.setTooltip(new Tooltip("Create a new grant record."));
		edit.setTooltip(new Tooltip("Modify the selected grant."));
		del.setTooltip(new Tooltip("Remove the selected grant."));
		alerts.setTooltip(new Tooltip("Show grants with at-risk/late compliance or overdue reports."));
		
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
		alerts.setOnAction(e -> showComplianceAlerts());
		
		setBottom(
			new ToolBar(refresh, alerts, new Separator(), new HBox(5, add, edit, del)));
		
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
	{ "unchecked", "deprecation" })
	private void buildTable()
	{
		TableColumn<GrantRow, String> idCol = col("Grant ID", "grantId");
		TableColumn<GrantRow, String> grantorCol = col("Grantor", "grantor");
		TableColumn<GrantRow, String> amtCol = col("Amount", "amount");
		TableColumn<GrantRow, String> dateCol =
			col("Date Awarded", "dateAwarded");
		TableColumn<GrantRow, String> purpCol = col("Purpose", "purpose");
		TableColumn<GrantRow, String> statusCol = col("Status", "status");
		TableColumn<GrantRow, String> restrictionCol =
			col("Restriction", "restrictionClass");
		TableColumn<GrantRow, String> complianceCol =
			col("Compliance", "complianceStatus");
		TableColumn<GrantRow, String> dueCol =
			col("Next Report Due", "nextReportDue");
		this.table.getColumns().addAll(idCol, grantorCol, amtCol, dateCol,
			purpCol, statusCol, restrictionCol, complianceCol, dueCol);
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
		
		try
		{
			this.grantsService.loadGrants();
		}
		catch (IOException ex)
		{
			LOGGER.error("Failed to load grants from database", ex);
			this.rows.clear();
			return;
		}
		
		this.rows.clear();
		List<Grant> list = this.grantsService.getAllGrants();
		
		for (var g : list)
		{
			this.rows.add(new GrantRow(g));
		}
		
	}
	
	/**
	 * Displays a dialog for adding or editing a grant.
	 *
	 * @param existing the existing
	 */
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
		ComboBox<String> restrictionF = new ComboBox<>(
			FXCollections.observableArrayList(
				"RESTRICTED", "UNRESTRICTED", "BOARD_DESIGNATED"));
		ComboBox<String> complianceF = new ComboBox<>(
			FXCollections.observableArrayList(
				"IN_GOOD_STANDING", "LATE_REPORT", "AT_RISK", "SUSPENDED", "CLOSED"));
		TextField nextDueF = new TextField();
		restrictionF.setEditable(false);
		complianceF.setEditable(false);
		restrictionF.getSelectionModel().select("RESTRICTED");
		complianceF.getSelectionModel().select("IN_GOOD_STANDING");
		
		if (existing != null)
		{
			grantorF.setText(existing.getGrantor());
			amountF
				.setText(existing.getAmount() != null ?
					FormatUtils.formatCurrency(existing.getAmount()) : "");
			dateF.setText(existing.getDateAwarded());
			purposeF.setText(existing.getPurpose());
			statusF.setText(existing.getStatus());
			restrictionF.getSelectionModel()
				.select(existing.getRestrictionClass() == null || existing.getRestrictionClass().isBlank()
					? "RESTRICTED"
					: existing.getRestrictionClass());
			complianceF.getSelectionModel()
				.select(existing.getComplianceStatus() == null || existing.getComplianceStatus().isBlank()
					? "IN_GOOD_STANDING"
					: existing.getComplianceStatus());
			nextDueF.setText(existing.getNextReportDue());
		}
		
		dlg.getDialogPane()
			.setContent(
				new VBox(8, new HBox(5, new Label("Grantor:"), grantorF),
					new HBox(5, new Label("Amount:"), amountF),
					new HBox(5, new Label("Date:"), dateF),
					new HBox(5, new Label("Purpose:"), purposeF),
					new HBox(5, new Label("Status:"), statusF),
					new HBox(5, new Label("Restriction:"), restrictionF),
					new HBox(5, new Label("Compliance:"), complianceF),
					new HBox(5, new Label("Next Report Due (YYYY-MM-DD):"),
						nextDueF)));
		
		dlg.setResultConverter(btn -> {
			
				if (btn != okType)
				{
					return null;
				}
			
			BigDecimal amount = FormatUtils.parseCurrency(amountF.getText());
			
			if (amount == null)
			{
				
				try
				{
					amount = new BigDecimal(amountF.getText().trim());
				}
				catch (NumberFormatException ex)
				{
					AlertBox.showError(
						dlg.getDialogPane().getScene().getWindow(),
						"Please enter a valid amount.");
					return null;
				}
				
				}

				if (!isIsoDateOrBlank(dateF.getText()))
				{
					AlertBox.showError(
						dlg.getDialogPane().getScene().getWindow(),
						"Date must be blank or YYYY-MM-DD.");
					return null;
				}
				if (!isIsoDateOrBlank(nextDueF.getText()))
				{
					AlertBox.showError(
						dlg.getDialogPane().getScene().getWindow(),
						"Next report due must be blank or YYYY-MM-DD.");
					return null;
				}
				
				Grant result = new Grant(
				existing == null ? UUID.randomUUID().toString() :
					existing.getGrantId(),
				grantorF.getText(), amount, dateF.getText(), purposeF.getText(),
				statusF.getText());
				result.setRestrictionClass(restrictionF.getValue());
				result.setComplianceStatus(complianceF.getValue());
				result.setNextReportDue(nextDueF.getText());
				return result;
			});
		
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

	private static boolean isIsoDateOrBlank(String value)
	{
		if (value == null || value.trim().isEmpty())
		{
			return true;
		}
		try
		{
			LocalDate.parse(value.trim());
			return true;
		}
		catch (DateTimeParseException ex)
		{
			return false;
		}
	}

	private void showComplianceAlerts()
	{
		try
		{
			List<GrantTraceabilityRow> alerts =
				this.traceabilityService.listComplianceAlerts(LocalDate.now());
			if (alerts.isEmpty())
			{
				AlertBox.showInfo(getScene().getWindow(),
					"No compliance alerts as of today.");
				return;
			}
			StringBuilder summary = new StringBuilder();
			int max = Math.min(alerts.size(), 5);
			for (int i = 0; i < max; i++)
			{
				GrantTraceabilityRow row = alerts.get(i);
				summary.append(row.getGrantId())
					.append(" (")
					.append(row.getComplianceStatus())
					.append(")\n");
			}
			AlertBox.showWarning(getScene().getWindow(),
				"Compliance alerts: " + alerts.size() + "\n" + summary);
		}
		catch (IOException ex)
		{
			LOGGER.error("Failed to load compliance alerts", ex);
			AlertBox.showError(getScene().getWindow(),
				"Unable to load compliance alerts.");
		}
	}
	
	/** Saves grants to the company file if set. */
	private void save()
	{
		
		try
		{
			this.grantsService.saveGrants();
		}
		catch (IOException ex)
		{
			LOGGER.error("Failed to save grants to database", ex);
		}
		
	}
	
	/**
	 * To grant.
	 *
	 * @param row the row
	 * @return the grant
	 */
	private Grant toGrant(GrantRow row)
	{
		BigDecimal amt = FormatUtils.parseCurrency(row.getAmount());
		
		if (amt == null)
		{
			amt = BigDecimal.ZERO;
		}
		
		Grant mapped = new Grant(row.getGrantId(), row.getGrantor(), amt,
			row.getDateAwarded(), row.getPurpose(), row.getStatus());
		mapped.setRestrictionClass(row.getRestrictionClass());
		mapped.setComplianceStatus(row.getComplianceStatus());
		mapped.setNextReportDue(row.getNextReportDue());
		return mapped;
		
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
		/** Restriction class (RESTRICTED, UNRESTRICTED, BOARD_DESIGNATED). */
		private final String restrictionClass;
		/** Compliance status (IN_GOOD_STANDING, LATE_REPORT, ...). */
		private final String complianceStatus;
		/** Next report due date text. */
		private final String nextReportDue;
		
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
			this.restrictionClass = g.getRestrictionClass();
			this.complianceStatus = g.getComplianceStatus();
			this.nextReportDue = g.getNextReportDue();
			
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

		/**
		 * Gets the restriction class.
		 *
		 * @return restriction class value
		 */
		public String getRestrictionClass()
		{
			return this.restrictionClass;
		}

		/**
		 * Gets the compliance status.
		 *
		 * @return compliance status value
		 */
		public String getComplianceStatus()
		{
			return this.complianceStatus;
		}

		/**
		 * Gets the next report due date.
		 *
		 * @return next report due date text
		 */
		public String getNextReportDue()
		{
			return this.nextReportDue;
		}
		
	}
	
	
}
