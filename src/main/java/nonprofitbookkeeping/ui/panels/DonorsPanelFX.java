
package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.UUID;

/**
 * JavaFX port of {@code DonorsPanel}. Manages donor records (name, email, phone).
 */
public class DonorsPanelFX extends BorderPane
{
	
	/** ObservableList to hold {@link Donor} objects for display in the table. */
	private final ObservableList<Donor> donors = FXCollections.observableArrayList();
	/** TableView to display the list of donors. */
	private final TableView<Donor> table = new TableView<>();
	
	/**
	 * Constructs a new {@code DonorsPanelFX}.
	 * Initializes the panel with a table to display donor information and buttons
	 * for adding, editing, and deleting donors. Demo data is added for illustrative purposes.
	 *
	 * @param primaryStage The primary stage of the application. This parameter is currently not used within the constructor.
	 */
	public DonorsPanelFX(Stage primaryStage)
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buttonBar());
		// demo data
		this.donors.add(new Donor("Alice", "alice@example.com", "555‑1234"));
		this.donors.add(new Donor("Bob", "bob@example.com", "555‑5678"));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying donor records.
	 * It defines columns for ID, Name, Email, and Phone, using {@link PropertyValueFactory}
	 * to bind them to the properties of the {@link Donor} class.
	 * The table is bound to the {@link #donors} observable list and a column resize policy is set.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * uses reflection and can lead to type safety warnings if property names don't strictly match
	 * Java bean conventions or if raw types are inferred. "deprecation" might relate to older patterns
	 * of using PropertyValueFactory.
	 */
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<Donor, String> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		TableColumn<Donor, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Donor, String> emailCol = new TableColumn<>("Email");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
		TableColumn<Donor, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		this.table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.donors);
	}
	
	/**
	 * Builds and returns an {@link HBox} containing "Add Donor", "Edit", and "Delete" buttons.
	 * These buttons provide functionality to manage donor records in the table.
	 * "Add Donor" and "Edit" open the {@link #donorDialog(Donor)}.
	 * "Delete" removes the selected donor from the table.
	 *
	 * @return A configured {@link HBox} with action buttons for donor management.
	 */
	private HBox buttonBar()
	{
		Button add = new Button("Add Donor");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		
		add.setOnAction(e -> donorDialog(null));
		edit.setOnAction(e -> {
			Donor sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
				donorDialog(sel);
		});
		del.setOnAction(e -> {
			Donor sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
				this.donors.remove(sel);
		});
		HBox box = new HBox(10, add, edit, del);
		box.setPadding(new Insets(8));
		return box;
	}
	
	/**
	 * Displays a dialog for adding a new donor or editing an existing one.
	 * If {@code existing} is null, the dialog is configured for adding a new donor.
	 * Otherwise, the dialog fields are pre-populated with the data from the {@code existing} donor.
	 * The dialog includes fields for Name, Email, and Phone.
	 * Upon confirmation (OK button), a new {@link Donor} object is created (or the existing one updated)
	 * and added to/refreshed in the {@link #donors} list and table.
	 *
	 * @param existing The {@link Donor} object to edit. If null, the dialog will create a new donor.
	 */
	private void donorDialog(Donor existing)
	{
		Dialog<Donor> dlg = new Dialog<>();
		dlg.setTitle(existing == null ? "Add Donor" : "Edit Donor");
		ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);
		
		TextField nameF = new TextField();
		TextField emailF = new TextField();
		TextField phoneF = new TextField();
		
		if (existing != null)
		{
			nameF.setText(existing.name);
			emailF.setText(existing.email);
			phoneF.setText(existing.phone);
		}
		
		dlg.getDialogPane().setContent(new HBox(10,
			new Label("Name:"), nameF,
			new Label("Email:"), emailF,
			new Label("Phone:"), phoneF));
		dlg.setResultConverter(
			btn -> btn == okType ? new Donor(existing == null ? null : existing.id, nameF.getText(),
				emailF.getText(), phoneF.getText()) : null);
		dlg.showAndWait().ifPresent(d -> {
			
			if (existing == null)
				this.donors.add(d);
			else
			{
				existing.name = d.name;
				existing.email = d.email;
				existing.phone = d.phone;
				this.table.refresh();
			}
			
		});
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Represents a single donor record.
	 * This class holds information about a donor, including their ID, name, email, and phone number.
	 * Note: Fields are directly accessible for modification by the {@code donorDialog} in this panel,
	 * which is a simpler approach for this UI context but less encapsulated.
	 */
	public static class Donor
	{
		/** The unique identifier for the donor. */
		private String id;
		/** The name of the donor. */
		private String name;
		/** The email address of the donor. */
		private String email;
		/** The phone number of the donor. */
		private String phone;
		
		/**
		 * Constructs a new {@code Donor} with a randomly generated ID.
		 *
		 * @param name The name of the donor.
		 * @param email The email address of the donor.
		 * @param phone The phone number of the donor.
		 */
		public Donor(String name, String email, String phone)
		{
			this(UUID.randomUUID().toString(), name, email, phone);
		}
		
		/**
		 * Constructs a new {@code Donor} with a specified ID.
		 *
		 * @param id The unique identifier for the donor.
		 * @param n The name of the donor.
		 * @param e The email address of the donor.
		 * @param p The phone number of the donor.
		 */
		public Donor(String id, String n, String e, String p)
		{
			this.id = id;
			this.name = n;
			this.email = e;
			this.phone = p;
		}
		
		/**
		 * Gets the unique ID of this donor.
		 * @return The unique ID string.
		 */
		public String getId()
		{
			return this.id;
		}
		
		/**
		 * Gets the name of the donor.
		 * @return The donor's name.
		 */
		public String getName()
		{
			return this.name;
		}
		
		/**
		 * Gets the email address of the donor.
		 * @return The donor's email address.
		 */
		public String getEmail()
		{
			return this.email;
		}
		
		/**
		 * Gets the phone number of the donor.
		 * @return The donor's phone number.
		 */
		public String getPhone()
		{
			return this.phone;
		}
		
	}
	
}
