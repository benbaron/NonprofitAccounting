
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

/**
 * JavaFX replacement for the Swing {@code DonationsPanel}. Keeps an in‑memory
 * list of donations (date, donor, fund, amount, memo) and allows adding or
 * deleting rows. In a full application you would wire this to a DonationService
 * that persists to the ledger.
 */
public class DonationsPanelFX extends BorderPane
{
	
	private final ObservableList<Donation> donations = FXCollections.observableArrayList();
	private final TableView<Donation> table = new TableView<>();
	public DonationsPanelFX()
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		
		// demo row
		this.donations.add(new Donation(LocalDate.now(), "Alice", "General", new BigDecimal("150.00"),
			"Online gift"));
	}
	
	/* ------------------------------------------------------------------ */
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
	 * 
	 * @return
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
	 * 
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
		});
		dlg.showAndWait().ifPresent(this.donations::add);
	}
	
	/* ------------------------------------------------------------------ */
	public static class Donation
	{
		private final String id = UUID.randomUUID().toString();
		private final LocalDate date;
		private final String donor;
		private final String fund;
		private final BigDecimal amount;
		private final String memo;
		
		public Donation(LocalDate d, String donor, String fund, BigDecimal amt, String memo)
		{
			this.date = d;
			this.donor = donor;
			this.fund = fund;
			this.amount = amt;
			this.memo = memo;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public String getDate()
		{
			return this.date.toString();
		}
		
		public String getDonor()
		{
			return this.donor;
		}
		
		public String getFund()
		{
			return this.fund;
		}
		
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		
		public String getMemo()
		{
			return this.memo;
		}
		
	}
	
}
