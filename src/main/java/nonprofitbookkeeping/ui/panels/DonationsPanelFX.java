
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.ui.panels.DonationsPanelFX.Donation;

/**
 * JavaFX replacement for the Swing {@code DonationsPanel}. Keeps an in‑memory
 * list of donations (date, donor, fund, amount, memo) and allows adding or
 * deleting rows. In a full application you would wire this to a DonationService
 * that persists to the ledger.
 */
public class DonationsPanelFX extends BorderPane
{
	
	/** ObservableList to hold {@link Donation} objects for display in the table. */
	private final ObservableList<Donation> donations = FXCollections.observableArrayList();
	/** TableView to display the list of donations. */
	private final TableView<Donation> table = new TableView<>();

	/**
	 * Constructs a new {@code DonationsPanelFX}.
	 * Initializes the panel with a table to display donations and buttons to add/delete donations.
	 * A demo donation is added for illustrative purposes.
	 *
	 * @param primaryStage The primary stage of the application. This parameter is currently not used within the constructor.
	 */
	public DonationsPanelFX(Stage primaryStage)
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		
		// demo row
		this.donations.add(new Donation(LocalDate.now(), 
			"Alice", "General", 
			new BigDecimal("150.00"),
			"Online gift"));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying donations.
	 * It defines columns for Date, Donor, Fund, Amount, and Memo, using {@link PropertyValueFactory}
	 * to bind them to the properties of the {@link Donation} class.
	 * The table is bound to the {@link #donations} observable list and a column resize policy is set.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * uses reflection and can lead to type safety warnings if property names don't strictly match
	 * Java bean conventions or if raw types are inferred. "deprecation" might relate to older patterns
	 * of using PropertyValueFactory.
	 */
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<Donation, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		TableColumn<Donation, String> donorCol = new TableColumn<>("Donor");
		donorCol.setCellValueFactory(new PropertyValueFactory<>("donor"));
		TableColumn<Donation, String> fundCol = new TableColumn<>("Fund");
		fundCol.setCellValueFactory(new PropertyValueFactory<>("fund"));
		TableColumn<Donation, BigDecimal> amtCol = new TableColumn<>("Amount");
		amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
		TableColumn<Donation, String> memoCol = new TableColumn<>("Memo");
		memoCol.setCellValueFactory(new PropertyValueFactory<>("memo"));
		this.table.getColumns().addAll(dateCol, donorCol, fundCol, amtCol, memoCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.donations);
	}
	
	/**
	 * Builds and returns a {@link ToolBar} containing "Add Donation" and "Delete Selected" buttons.
	 * The "Add Donation" button opens a dialog ({@link #addDonationDialog()}) to input new donation details.
	 * The "Delete Selected" button removes the currently selected donation from the table and the underlying list.
	 * 
	 * @return A configured {@link ToolBar} with action buttons.
	 */
	private ToolBar buildButtons()
	{
		Button add = new Button("Add Donation");
		Button del = new Button("Delete Selected");
		add.setOnAction(e -> addDonationDialog());
		del.setOnAction(e -> {
			Donation sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
				this.donations.remove(sel);
		});
		return new ToolBar(add, del);
	}
	
	/**
	 * Displays a dialog for adding a new donation.
	 * The dialog includes fields for Date (using {@link DatePicker}), Donor, Fund, Amount, and Memo.
	 * Upon successful confirmation (OK button) and valid numeric input for the amount,
	 * a new {@link Donation} object is created and added to the {@link #donations} list,
	 * which updates the table. If the amount is not numeric, an error alert is shown.
	 */
	private void addDonationDialog()
	{
		Dialog<Donation> dlg = new Dialog<>();
		dlg.setTitle("Add Donation");
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		DatePicker datePicker = new DatePicker(LocalDate.now());
		TextField donorField = new TextField();
		TextField fundField = new TextField("General");
		TextField amtField = new TextField();
		TextField memoField = new TextField();
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.setPadding(new Insets(10));
		grid.addRow(0, new Label("Date:"), datePicker);
		grid.addRow(1, new Label("Donor:"), donorField);
		grid.addRow(2, new Label("Fund:"), fundField);
		grid.addRow(3, new Label("Amount:"), amtField);
		grid.addRow(4, new Label("Memo:"), memoField);
		dlg.getDialogPane().setContent(grid);
		
		dlg.setResultConverter(btn -> {	
			return resultConverterCallback(datePicker, donorField, 
				fundField, amtField, memoField,
				btn);
		});
		dlg.showAndWait().ifPresent(this.donations::add);
	}

	/**
	 * resultConverterCallback
	 * @param datePicker
	 * @param donorField
	 * @param fundField
	 * @param amtField
	 * @param memoField
	 * @param btn
	 * @return
	 */
	static Donation resultConverterCallback(DatePicker datePicker, TextField donorField,
	                                        TextField fundField, TextField amtField,
	                                        TextField memoField, ButtonType btn)
	{
		
		if (btn == ButtonType.OK)
		{
			
			try
			{
				BigDecimal amt = new BigDecimal(amtField.getText().trim());
				return new Donation(datePicker.getValue(), donorField.getText(),
					fundField.getText(), amt, memoField.getText());
			}
			catch (@SuppressWarnings("unused") NumberFormatException ex)
			{
				new Alert(Alert.AlertType.ERROR, "Amount must be numeric").showAndWait();
			}
			
		}
		
		return null;
		
	}
	
	/**
	 * Represents a single donation record.
	 * This class holds details about a donation such as its date, donor, fund,
	 * amount, and an optional memo. Each donation is assigned a unique ID.
	 */
	public static class Donation
	{
		/** A unique identifier for the donation. */
		private final String id = UUID.randomUUID().toString();
		/** The date the donation was made or recorded. */
		private final LocalDate date;
		/** The name or identifier of the donor. */
		private final String donor;
		/** The fund to which the donation is allocated (e.g., "General", "Building Fund"). */
		private final String fund;
		/** The monetary amount of the donation. */
		private final BigDecimal amount;
		/** An optional memo or note associated with the donation. */
		private final String memo;
		
		/**
		 * Constructs a new {@code Donation} object.
		 *
		 * @param d The date of the donation.
		 * @param donor The name of the donor.
		 * @param fund The fund to which the donation is designated.
		 * @param amt The amount of the donation.
		 * @param memo A memo or description for the donation.
		 */
		public Donation(LocalDate d, String donor, String fund, BigDecimal amt, String memo)
		{
			this.date = d;
			this.donor = donor;
			this.fund = fund;
			this.amount = amt;
			this.memo = memo;
		}
		
		/**
		 * Gets the unique ID of this donation.
		 * @return The unique ID string.
		 */
		public String getId()
		{
			return this.id;
		}
		
		/**
		 * Gets the date of the donation as a String.
		 * Note: For direct use with {@link PropertyValueFactory} expecting a String.
		 * Consider returning {@link LocalDate} if more specific type handling is used in TableColumn.
		 * @return The date of the donation, formatted as a string (e.g., "YYYY-MM-DD").
		 */
		public String getDate()
		{
			return this.date.toString();
		}
		
		/**
		 * Gets the name of the donor.
		 * @return The donor's name.
		 */
		public String getDonor()
		{
			return this.donor;
		}
		
		/**
		 * Gets the fund to which this donation is allocated.
		 * @return The name of the fund.
		 */
		public String getFund()
		{
			return this.fund;
		}
		
		/**
		 * Gets the amount of the donation.
		 * @return The donation amount as a {@link BigDecimal}.
		 */
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		
		/**
		 * Gets the memo associated with this donation.
		 * @return The memo string.
		 */
		public String getMemo()
		{
			return this.memo;
		}
		
	}
	
}
