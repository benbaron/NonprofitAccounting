package org.nonprofitbookkeeping.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;

import java.time.LocalDate;
import java.util.List;

/** Native alternate workspace for company administration. */
public class AlternateCompanyAdminPanel implements AppPanel
{
    private final VBox root = new VBox(10);
    private final AlternateCompanyAdminService service;
    private final ListView<CompanyRecord> companies = new ListView<>();
    private final TextArea status = new TextArea();

    public AlternateCompanyAdminPanel(UiServiceProvider provider)
    {
        this(new AlternateCompanyAdminService(provider));
    }

    AlternateCompanyAdminPanel(AlternateCompanyAdminService service)
    {
        this.service = service;
        build();
        refreshCompanies();
    }

    @Override public String title() { return "Company Administration"; }
    @Override public Node root() { return this.root; }

    private void build()
    {
        this.root.setPadding(new Insets(12));
        this.root.getStyleClass().add("alternate-content-card");
        Label title = new Label("Company Administration");
        title.getStyleClass().add("alternate-panel-title");
        Label safety = new Label(AlternateCompanyAdminService.DELETE_DEFINITION + " " + AlternateCompanyAdminService.BACKUP_GUIDANCE);
        safety.setWrapText(true);
        safety.getStyleClass().add("alternate-panel-subtitle");
        this.companies.setCellFactory(view -> new javafx.scene.control.ListCell<>()
        {
            @Override protected void updateItem(CompanyRecord item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (ID " + item.id() + ")");
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refreshCompanies());
        Button open = new Button("Open / Switch");
        open.setOnAction(e -> withSelected(record -> { this.service.openCompany(record.id()); return "Opened " + record.name(); }));
        Button close = new Button("Close Active Company");
        close.setOnAction(e -> { this.service.closeActiveCompany(); setStatus("Active company closed safely."); });
        Button populate = new Button("Populate Starter Chart");
        populate.setOnAction(e -> withSelected(record -> this.service.populateCompany(record.id()).message()));
        Button sample = new Button("Create Deterministic Sample Company");
        sample.setOnAction(e -> run(() -> "Created sample company ID " + this.service.createSampleCompany()));

        GridPane create = createCompanyForm();
        GridPane delete = deleteForm();
        this.status.setEditable(false);
        this.status.setPrefRowCount(6);
        this.root.getChildren().setAll(title, safety, this.companies, new HBox(8, refresh, open, close, populate, sample), create, delete, this.status);
    }

    private GridPane createCompanyForm()
    {
        GridPane grid = section("Create Company");
        TextField name = field("Organization name");
        TextField legal = field("Legal structure");
        TextField fiscal = field("Fiscal year start MM-DD");
        fiscal.setText("01-01");
        TextField currency = field("Base currency");
        currency.setText("USD");
        TextField start = field("Starting balance date YYYY-MM-DD");
        start.setText(LocalDate.now().toString());
        CheckBox fund = new CheckBox("Enable fund accounting");
        fund.setSelected(true);
        Button create = new Button("Create and Open");
        create.setOnAction(e -> run(() -> {
            long id = this.service.createCompany(new AlternateCompanyAdminService.CreateCompanyRequest(name.getText(), legal.getText(), fiscal.getText(), currency.getText(), start.getText(), fund.isSelected()));
            refreshCompanies();
            return "Created and opened company ID " + id;
        }));
        grid.addRow(1, new Label("Organization"), name, new Label("Legal"), legal);
        grid.addRow(2, new Label("Fiscal"), fiscal, new Label("Currency"), currency);
        grid.addRow(3, new Label("Start date"), start, fund, create);
        return grid;
    }

    private GridPane deleteForm()
    {
        GridPane grid = section("Destroy / Delete Company");
        TextField typedName = field("Type selected company name exactly");
        CheckBox backup = new CheckBox("I exported/backed up the database and understand deletion removes the selected company row.");
        Button delete = new Button("Delete Selected Company");
        delete.setOnAction(e -> withSelected(record -> {
            this.service.deleteCompany(record.id(), typedName.getText(), backup.isSelected());
            refreshCompanies();
            return "Deleted company " + record.name();
        }));
        grid.add(new Label("Confirmation"), 0, 1);
        grid.add(typedName, 1, 1);
        grid.add(backup, 1, 2);
        grid.add(delete, 2, 1);
        return grid;
    }

    private GridPane section(String heading)
    {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        Label label = new Label(heading);
        label.getStyleClass().add("alternate-section-title");
        grid.add(label, 0, 0, 4, 1);
        return grid;
    }

    private TextField field(String prompt) { TextField f = new TextField(); f.setPromptText(prompt); return f; }

    private void refreshCompanies()
    {
        run(() -> {
            List<CompanyRecord> records = this.service.listCompanies();
            this.companies.setItems(FXCollections.observableArrayList(records));
            return records.isEmpty() ? "No companies in the active database." : "Loaded " + records.size() + " companies.";
        });
    }

    private void withSelected(ThrowingAction action)
    {
        CompanyRecord record = this.companies.getSelectionModel().getSelectedItem();
        if (record == null) { setStatus("Select a company first."); return; }
        run(() -> action.run(record));
    }

    private void run(ThrowingSupplier supplier)
    {
        try { setStatus(supplier.get()); }
        catch (Exception ex) { setStatus("Company administration failed: " + ex.getMessage()); }
    }

    private void setStatus(String message) { this.status.setText(message == null ? "" : message); }

    @FunctionalInterface private interface ThrowingSupplier { String get() throws Exception; }
    @FunctionalInterface private interface ThrowingAction { String run(CompanyRecord record) throws Exception; }
}
