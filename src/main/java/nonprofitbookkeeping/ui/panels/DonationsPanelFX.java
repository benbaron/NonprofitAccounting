package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.persistence.DonationRecordRepository;
import nonprofitbookkeeping.service.DonationPostingService;
import nonprofitbookkeeping.service.SettingsService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Donations panel backed by DonationRecord persistence and DonationPostingService.
 */
public class DonationsPanelFX extends BorderPane
{
	private final ObservableList<DonationRecord> donations =
		FXCollections.observableArrayList();
	private final TableView<DonationRecord> table = new TableView<>();
	private final DonationRecordRepository donationRecordRepository;
	private final DonationPostingService donationPostingService;
	private final Label status = new Label();

	public DonationsPanelFX(Stage primaryStage)
	{
		this(primaryStage, new DonationRecordRepository(),
			new DonationPostingService());
	}

	DonationsPanelFX(Stage primaryStage,
		DonationRecordRepository donationRecordRepository,
		DonationPostingService donationPostingService)
	{
		this.donationRecordRepository = donationRecordRepository;
		this.donationPostingService = donationPostingService;
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setTop(buildButtons());
		setBottom(this.status);
		refresh();
	}

	private void buildTable()
	{
		TableColumn<DonationRecord, String> idCol = new TableColumn<>("Donation ID");
		idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
			nvl(d.getValue().getDonationId())));
		TableColumn<DonationRecord, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
			d.getValue().getDonationDate() == null ? "" :
				d.getValue().getDonationDate().toString()));
		TableColumn<DonationRecord, String> donorCol = new TableColumn<>("Donor ID");
		donorCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
			nvl(d.getValue().getDonorExternalId())));
		TableColumn<DonationRecord, String> amtCol = new TableColumn<>("Amount");
		amtCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
			d.getValue().getAmount() == null ? "" :
				d.getValue().getAmount().toPlainString()));
		TableColumn<DonationRecord, String> memoCol = new TableColumn<>("Memo");
		memoCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
			nvl(d.getValue().getMemo())));
		TableColumn<DonationRecord, String> txnCol = new TableColumn<>("Journal Txn");
		txnCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
			d.getValue().getJournalTxnId() == null ? "" :
				String.valueOf(d.getValue().getJournalTxnId())));

		this.table.getColumns().setAll(idCol, dateCol, donorCol, amtCol, memoCol,
			txnCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.donations);
	}

	private ToolBar buildButtons()
	{
		Button add = new Button("Add Donation");
		Button edit = new Button("Edit Selected");
		Button refresh = new Button("Refresh");
		add.setOnAction(e -> openDonationDialog(null));
		edit.setOnAction(e -> openDonationDialog(
			this.table.getSelectionModel().getSelectedItem()));
		refresh.setOnAction(e -> refresh());
		return new ToolBar(add, edit, refresh);
	}

	private void openDonationDialog(DonationRecord selected)
	{
		Dialog<DonationRecord> dlg = new Dialog<>();
		dlg.setTitle(selected == null ? "Add Donation" : "Edit Donation");
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK,
			ButtonType.CANCEL);

		SettingsModel defaults = loadSettingsDefaults();
		DonationRecord working = selected == null ? new DonationRecord() : selected;
		if (working.getDonationId() == null || working.getDonationId().isBlank())
		{
			working.setDonationId(UUID.randomUUID().toString());
		}

		TextField idField = new TextField(working.getDonationId());
		idField.setEditable(selected == null);
		DatePicker datePicker = new DatePicker(working.getDonationDate() == null ?
			LocalDate.now() : working.getDonationDate());
		TextField donorField = new TextField(nvl(working.getDonorExternalId()));
		TextField amountField = new TextField(working.getAmount() == null ? "" :
			working.getAmount().toPlainString());
		TextField memoField = new TextField(nvl(working.getMemo()));
		TextField cashField = new TextField(
			defaultIfBlank(working.getCashAccountNumber(),
				defaults == null ? null : defaults.getDefaultExpenseAccount()));
		TextField revenueField = new TextField(
			defaultIfBlank(working.getRevenueAccountNumber(),
				defaults == null ? null : defaults.getDefaultIncomeAccount()));
		TextField fundField = new TextField(nvl(working.getFundNumber()));

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.setPadding(new Insets(10));
		grid.addRow(0, new Label("Donation ID:"), idField);
		grid.addRow(1, new Label("Date:"), datePicker);
		grid.addRow(2, new Label("Donor External ID:"), donorField);
		grid.addRow(3, new Label("Amount:"), amountField);
		grid.addRow(4, new Label("Memo:"), memoField);
		grid.addRow(5, new Label("Cash Account #:"), cashField);
		grid.addRow(6, new Label("Revenue Account #:"), revenueField);
		grid.addRow(7, new Label("Fund:"), fundField);
		dlg.getDialogPane().setContent(grid);

		dlg.setResultConverter(btn -> {
			if (btn != ButtonType.OK)
			{
				return null;
			}
			try
			{
				return toDonationRecord(idField, datePicker, donorField, amountField,
					memoField, cashField, revenueField, fundField);
			}
			catch (IllegalArgumentException ex)
			{
				new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
				return null;
			}
		});

		dlg.showAndWait().ifPresent(this::saveDonation);
	}

	private static DonationRecord toDonationRecord(TextField idField,
		DatePicker datePicker, TextField donorField, TextField amountField,
		TextField memoField, TextField cashField, TextField revenueField,
		TextField fundField)
	{
		String donationId =
			idField.getText() == null ? "" : idField.getText().trim();
		if (donationId.isBlank())
		{
			throw new IllegalArgumentException("Donation ID is required.");
		}
		BigDecimal amount = parseAmount(amountField.getText());
		if (datePicker.getValue() == null)
		{
			throw new IllegalArgumentException("Donation date is required.");
		}
		DonationRecord out = new DonationRecord();
		out.setDonationId(donationId);
		out.setDonationDate(datePicker.getValue());
		out.setDonorExternalId(donorField.getText() == null ? "" :
			donorField.getText().trim());
		out.setAmount(amount);
		out.setMemo(memoField.getText());
		out.setCashAccountNumber(cashField.getText() == null ? "" :
			cashField.getText().trim());
		out.setRevenueAccountNumber(revenueField.getText() == null ? "" :
			revenueField.getText().trim());
		out.setFundNumber(fundField.getText());
		return out;
	}

	private static BigDecimal parseAmount(String value)
	{
		if (value == null || value.isBlank())
		{
			throw new IllegalArgumentException("Amount is required.");
		}
		try
		{
			BigDecimal parsed = new BigDecimal(value.trim());
			if (parsed.signum() <= 0)
			{
				throw new IllegalArgumentException(
					"Amount must be greater than zero.");
			}
			return parsed;
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException(
				"Amount must be a valid decimal number.");
		}
	}

	private void saveDonation(DonationRecord donation)
	{
		try
		{
			this.donationPostingService.postDonation(donation);
			this.status.setText("Saved donation " + donation.getDonationId() +
				" (journal txn " + donation.getJournalTxnId() + ")");
			refresh();
		}
		catch (IllegalArgumentException | SQLException ex)
		{
			new Alert(Alert.AlertType.ERROR,
				"Failed to save donation: " + ex.getMessage()).showAndWait();
		}
	}

	private void refresh()
	{
		try
		{
			this.donations.setAll(this.donationRecordRepository.listAll());
			if (this.status.getText() == null || this.status.getText().isBlank())
			{
				this.status.setText("Loaded " + this.donations.size() +
					" donation(s).");
			}
		}
		catch (SQLException ex)
		{
			this.status.setText("Failed to load donations: " + ex.getMessage());
		}
	}

	private static String nvl(String value)
	{
		return value == null ? "" : value;
	}

	private static String defaultIfBlank(String value, String fallback)
	{
		if (value != null && !value.isBlank())
		{
			return value;
		}
		return fallback == null ? "" : fallback;
	}

	private static SettingsModel loadSettingsDefaults()
	{
		SettingsService settingsService = new SettingsService();
		try
		{
			settingsService.loadSettings(null);
			return settingsService.getSettings();
		}
		catch (IOException ex)
		{
			return new SettingsModel();
		}
	}
}
