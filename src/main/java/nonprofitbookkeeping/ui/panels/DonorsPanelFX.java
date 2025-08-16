
package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.File;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.model.Donor;
import java.util.UUID;

/**
 * JavaFX port of {@code DonorsPanel}. Manages donor records (name, email, phone).
 */
public class DonorsPanelFX extends BorderPane
{
	
	/** Service for donor management operations. */
	private final DonorService service;
	/** Directory where donor data should be persisted, may be null. */
	private final File companyDirectory;
	/** ObservableList to hold {@link Donor} objects for display in the table. */
	private final ObservableList<Donor> donors =
		FXCollections.observableArrayList();
	/** TableView to display the list of donors. */
	private final TableView<Donor> table = new TableView<>();
	
	/**
	 * Constructs a new {@code DonorsPanelFX}.
	 * Loads donors from disk if a company directory is provided and builds the UI.
	 *
	 * @param service          the {@link DonorService} to use for donor operations
	 * @param companyDirectory directory where donor data is persisted, may be null
	 */
	public DonorsPanelFX(DonorService service, File companyDirectory)
	{
		this.service = service;
		this.companyDirectory = companyDirectory;
		
		if (this.companyDirectory != null)
		{
			
			try
			{
				this.service.loadDonors(this.companyDirectory);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
		}
		
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buttonBar());
		refresh();
		
	}
	
	/** Convenience constructor when no directory is available. */
	public DonorsPanelFX(DonorService service)
	{
		this(service, null);
		
	}
	
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
	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	private void buildTable()
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
			{
				donorDialog(sel);
			}
			
		});
		del.setOnAction(e -> {
			Donor sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.service.removeDonor(sel.getName());
				refresh();
				save();
			}
			
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
			nameF.setText(existing.getName());
			emailF.setText(existing.getEmail());
			phoneF.setText(existing.getPhone());
		}
		
		dlg.getDialogPane().setContent(new HBox(10,
			new Label("Name:"), nameF,
			new Label("Email:"), emailF,
			new Label("Phone:"), phoneF));
		
		dlg.setResultConverter(
			btn -> btn == okType ? new Donor(existing == null ?
				UUID.randomUUID().toString() :
				existing.getId(), nameF.getText(),
				emailF.getText(), phoneF.getText()) : null);
		
		dlg.showAndWait().ifPresent(d -> {
			
			if (existing == null)
			{
				this.service.addDonor(new Donor(d, null, null, null));
			}
			else
			{
				String oldName = existing.getName();
				existing.setName(d.getName());
				existing.setEmail(d.getEmail());
				existing.setPhone(d.getPhone());
				this.service.editDonor(oldName, existing);
			}
			
			refresh();
			save();
			
		});
		
	}
	
	/** Refreshes the table from the service layer. */
	private void refresh()
	{
		this.donors.setAll(this.service.getAllDonors());
		this.table.refresh();
		
	}
	
	/** Saves donors to disk if a company directory is set. */
	private void save()
	{
		
		if (this.companyDirectory != null)
		{
			
			try
			{
				this.service.saveDonors(this.companyDirectory);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
		}
		
	}
	
}
