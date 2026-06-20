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
import nonprofitbookkeeping.persistence.DonationRecordRepository;
import nonprofitbookkeeping.service.DonorService;

import java.sql.SQLException;
import java.util.List;

/** Native alternate UI donor workspace. */
public class DonorManagementPanel implements AppPanel, AppPanel.SaveAware
{
    private final DonorService donorService;
    private final DonationRecordRepository donationRepository;
    private final AlternatePanelScaffold scaffold = new AlternatePanelScaffold("Donors");
    private final ObservableList<DonorContact> donors = FXCollections.observableArrayList();
    private final ObservableList<DonationRecord> donations = FXCollections.observableArrayList();
    private final TableView<DonorContact> donorTable = new TableView<>();
    private final TableView<DonationRecord> donationTable = new TableView<>();
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label importExportNote = new Label("Donor import/export is deferred to a future Import/Export extension; the current donor services only support in-app records.");

    public DonorManagementPanel()
    {
        this(new DonorService(), new DonationRecordRepository());
    }

    DonorManagementPanel(DonorService donorService, DonationRecordRepository donationRepository)
    {
        this.donorService = donorService;
        this.donationRepository = donationRepository;
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
        donationTable.getColumns().setAll(date, amount, memo, txn);
        donationTable.setItems(donations);
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
        VBox box = new VBox(10, form, new Label("Donation History"), donationTable);
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
            donations.setAll(donationRepository.listByDonorExternalId(donorId));
        }
        catch (SQLException ex)
        {
            donations.clear();
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
    }

    private static String text(TextField field)
    {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
