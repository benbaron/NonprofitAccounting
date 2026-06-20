package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.DonorReceiptWorkflowService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

/** Native alternate UI donor workspace. */
public class DonorManagementPanel implements AppPanel, AppPanel.SaveAware
{
    private final DonorService donorService;
    private final DonorReceiptWorkflowService receiptWorkflowService;
    private final AlternatePanelScaffold scaffold = new AlternatePanelScaffold("Donors");
    private final ObservableList<DonorContact> donors = FXCollections.observableArrayList();
    private final ObservableList<DonationRecord> donations = FXCollections.observableArrayList();
    private final ObservableList<AnnualTotalRow> annualTotals = FXCollections.observableArrayList();
    private final TableView<DonorContact> donorTable = new TableView<>();
    private final TableView<DonationRecord> donationTable = new TableView<>();
    private final TableView<AnnualTotalRow> annualTotalTable = new TableView<>();
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label importExportNote = new Label("Donor import/export is deferred to a future Import/Export extension; the current donor services only support in-app records.");

    public DonorManagementPanel()
    {
        this(new DonorService(), new DonorReceiptWorkflowService());
    }

    DonorManagementPanel(DonorService donorService,
        DonorReceiptWorkflowService receiptWorkflowService)
    {
        this.donorService = donorService;
        this.receiptWorkflowService = receiptWorkflowService;
        buildUi();
        refreshDonors();
    }

    @Override
    public String title()
    {
        return "Donors";
    }

    @Override
    public Node root()
    {
        return scaffold;
    }

    @Override
    public void onNew()
    {
        donorTable.getSelectionModel().clearSelection();
        clearEditor();
    }

    @Override
    public void onDelete()
    {
        deactivateSelectedDonor();
    }

    @Override
    public SaveResult save()
    {
        DonorContact saved = saveEditor();
        return saved == null ? SaveResult.noChanges("No donor selected or entered.") :
            SaveResult.saved("Saved donor " + saved.getName() + ".");
    }

    private void buildUi()
    {
        scaffold.setSubtitle("Manage donor contact records and review linked donation transactions.");
        Button add = new Button("New Donor");
        add.setOnAction(e -> onNew());
        Button save = new Button("Save Donor");
        save.setOnAction(e -> save());
        Button deactivate = new Button("Deactivate Donor");
        deactivate.setOnAction(e -> deactivateSelectedDonor());
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refreshDonors());
        scaffold.setPrimaryActions(List.of(add, save, deactivate, refresh));
        importExportNote.setWrapText(true);
        scaffold.setWarningBanner(importExportNote.getText());

        configureDonorTable();
        configureDonationTable();
        donorTable.getSelectionModel().selectedItemProperty().addListener((obs, old, donor) -> loadDonor(donor));

        SplitPane split = new SplitPane(donorTable, editorAndHistory());
        split.setDividerPositions(0.45);
        scaffold.setContent(split);
    }

    private void configureDonorTable()
    {
        TableColumn<DonorContact, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        id.setPrefWidth(180);
        TableColumn<DonorContact, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setPrefWidth(180);
        TableColumn<DonorContact, String> email = new TableColumn<>("Email");
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        email.setPrefWidth(220);
        TableColumn<DonorContact, String> phone = new TableColumn<>("Phone");
        phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phone.setPrefWidth(140);
        donorTable.getColumns().setAll(id, name, email, phone);
        donorTable.setItems(donors);
    }

    private void configureDonationTable()
    {
        TableColumn<DonationRecord, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getDonationDate() == null ? "" : row.getValue().getDonationDate().toString()));
        TableColumn<DonationRecord, String> amount = new TableColumn<>("Amount");
        amount.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getAmount() == null ? "" : row.getValue().getAmount().toPlainString()));
        TableColumn<DonationRecord, String> memo = new TableColumn<>("Memo");
        memo.setCellValueFactory(new PropertyValueFactory<>("memo"));
        TableColumn<DonationRecord, String> txn = new TableColumn<>("Journal Txn");
        txn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getJournalTxnId() == null ? "" : row.getValue().getJournalTxnId().toString()));
        TableColumn<DonationRecord, String> receipt = new TableColumn<>("Receipt Status");
        receipt.setCellValueFactory(row -> new SimpleStringProperty(receiptStatus(row.getValue())));
        donationTable.getColumns().setAll(date, amount, memo, txn, receipt);
        donationTable.setItems(donations);

        TableColumn<AnnualTotalRow, String> year = new TableColumn<>("Year");
        year.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().year()));
        TableColumn<AnnualTotalRow, String> total = new TableColumn<>("Annual Total");
        total.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().total()));
        annualTotalTable.getColumns().setAll(year, total);
        annualTotalTable.setItems(annualTotals);
    }

    private Node editorAndHistory()
    {
        idField.setPromptText("Auto-generated when blank");
        idField.setEditable(true);
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Donor ID"), idField);
        form.addRow(1, new Label("Name"), nameField);
        form.addRow(2, new Label("Email"), emailField);
        form.addRow(3, new Label("Phone"), phoneField);
        Button receiptRequired = new Button("Receipt Required");
        receiptRequired.setOnAction(e -> updateSelectedDonationReceiptRequired(true));
        Button receiptNotRequired = new Button("No Receipt Required");
        receiptNotRequired.setOnAction(e -> updateSelectedDonationReceiptRequired(false));
        Button receiptSent = new Button("Mark Receipt Sent");
        receiptSent.setOnAction(e -> markSelectedDonationReceiptSent());
        HBox receiptActions = new HBox(8, receiptRequired, receiptNotRequired, receiptSent);
        VBox box = new VBox(10, form, new Label("Donation History"), donationTable,
            receiptActions, new Label("Annual Totals"), annualTotalTable);
        VBox.setVgrow(donationTable, Priority.ALWAYS);
        return box;
    }

    private void refreshDonors()
    {
        try
        {
            donorService.loadDonors(null);
            donors.setAll(donorService.getAllDonors());
            scaffold.showContent();
            scaffold.setStatus(donors.size() + " active donor(s). Deactivate keeps linked donation history intact.");
            if (donors.isEmpty())
            {
                donations.clear();
                annualTotals.clear();
            }
        }
        catch (Exception ex)
        {
            scaffold.showError("Unable to load donors: " + ex.getMessage());
        }
    }

    private void loadDonor(DonorContact donor)
    {
        if (donor == null)
        {
            clearEditor();
            return;
        }
        idField.setText(donor.getId());
        idField.setEditable(false);
        nameField.setText(donor.getName());
        emailField.setText(donor.getEmail());
        phoneField.setText(donor.getPhone());
        refreshDonationHistory(donor.getId());
    }

    private void refreshDonationHistory(String donorId)
    {
        try
        {
            DonorReceiptWorkflowService.DonorDetail detail =
                receiptWorkflowService.detailForDonor(donorId);
            donations.setAll(detail.donationHistory());
            annualTotals.setAll(detail.annualTotals().entrySet().stream()
                .map(e -> new AnnualTotalRow(e.getKey(), e.getValue()))
                .toList());
        }
        catch (SQLException ex)
        {
            donations.clear();
            annualTotals.clear();
            scaffold.setStatus("Unable to load donation history: " + ex.getMessage());
        }
    }

    private DonorContact saveEditor()
    {
        String name = text(nameField);
        if (name.isBlank())
        {
            scaffold.setStatus("Name is required before saving a donor.");
            return null;
        }
        DonorContact selected = donorTable.getSelectionModel().getSelectedItem();
        String donorId = selected == null ? text(idField) : selected.getId();
        DonorContact donor = new DonorContact(donorId, name, text(emailField), text(phoneField));
        if (selected == null)
        {
            donorService.addDonor(donor);
        }
        else
        {
            donorService.editDonor(selected.getId(), donor);
        }
        refreshDonors();
        selectDonor(donor.getId());
        return donor;
    }

    private void deactivateSelectedDonor()
    {
        DonorContact selected = donorTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            scaffold.setStatus("Select a donor to deactivate.");
            return;
        }
        donorService.removeDonor(selected.getId());
        refreshDonors();
        clearEditor();
    }

    private void selectDonor(String donorId)
    {
        donors.stream().filter(d -> donorId != null && donorId.equals(d.getId())).findFirst()
            .ifPresent(d -> donorTable.getSelectionModel().select(d));
    }

    private void clearEditor()
    {
        idField.clear();
        idField.setEditable(true);
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        donations.clear();
        annualTotals.clear();
    }

    private void updateSelectedDonationReceiptRequired(boolean required)
    {
        DonationRecord selected = donationTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            scaffold.setStatus("Select a donation before changing receipt status.");
            return;
        }
        try
        {
            receiptWorkflowService.updateReceiptRequired(selected.getDonationId(), required);
            refreshDonationHistory(selected.getDonorExternalId());
            scaffold.setStatus(required ? "Receipt required." : "Receipt not required.");
        }
        catch (SQLException ex)
        {
            scaffold.setStatus("Unable to update receipt status: " + ex.getMessage());
        }
    }

    private void markSelectedDonationReceiptSent()
    {
        DonationRecord selected = donationTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            scaffold.setStatus("Select a donation before marking a receipt sent.");
            return;
        }
        try
        {
            receiptWorkflowService.markReceiptSent(selected.getDonationId(), LocalDateTime.now());
            refreshDonationHistory(selected.getDonorExternalId());
            scaffold.setStatus("Receipt marked sent.");
        }
        catch (SQLException ex)
        {
            scaffold.setStatus("Unable to update receipt status: " + ex.getMessage());
        }
    }

    private static String receiptStatus(DonationRecord donation)
    {
        if (!donation.isReceiptRequired())
        {
            return "Not required";
        }
        if (donation.getReceiptSentAt() != null)
        {
            return "Sent " + donation.getReceiptSentAt().toLocalDate();
        }
        return "Required";
    }

    private record AnnualTotalRow(Year yearValue, BigDecimal totalValue)
    {
        public String year()
        {
            return yearValue == null ? "" : yearValue.toString();
        }

        public String total()
        {
            return totalValue == null ? "" : totalValue.toPlainString();
        }
    }

    private static String text(TextField field)
    {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
