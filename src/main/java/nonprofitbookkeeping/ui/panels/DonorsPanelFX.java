
package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.UUID;

/**
 * JavaFX port of {@code DonorsPanel}. Manages donor records (name, email, phone).
 */
public class DonorsPanelFX extends BorderPane
{
	
	private final ObservableList<Donor> donors = FXCollections.observableArrayList();
	private final TableView<Donor> table = new TableView<>();
	
	public DonorsPanelFX()
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
	public static class Donor
	{
		private String id;
		private String name;
		private String email;
		private String phone;
		
		public Donor(String name, String email, String phone)
		{
			this(UUID.randomUUID().toString(), name, email, phone);
		}
		
		public Donor(String id, String n, String e, String p)
		{
			this.id = id;
			this.name = n;
			this.email = e;
			this.phone = p;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public String getEmail()
		{
			return this.email;
		}
		
		public String getPhone()
		{
			return this.phone;
		}
		
	}
	
}
