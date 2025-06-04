package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SkeletonJournalPanel extends BorderPane {

    public SkeletonJournalPanel() {
        setPadding(new Insets(15)); // Overall padding for the panel

        // Filter Controls (Top)
        HBox filterControlsBox = new HBox();
        filterControlsBox.setPadding(new Insets(0, 0, 10, 0)); // Bottom padding for separation
        filterControlsBox.setSpacing(10);
        filterControlsBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter:");
        TextField searchField = new TextField();
        searchField.setPromptText("Search description/account...");
        searchField.setPrefWidth(200);
        TextField dateFilterField = new TextField();
        dateFilterField.setPromptText("Date (YYYY-MM-DD)");
        dateFilterField.setPrefWidth(150);
        Button applyFilterButton = new Button("Apply Filter");

        filterControlsBox.getChildren().addAll(filterLabel, searchField, dateFilterField, applyFilterButton);
        this.setTop(filterControlsBox);

        // Journal Entries Table (Center)
        TableView<JournalEntryData> journalTable = new TableView<>();
        journalTable.setPlaceholder(new Label("No journal entries to display."));

        TableColumn<JournalEntryData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<JournalEntryData, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<JournalEntryData, String> accountCol = new TableColumn<>("Account");
        accountCol.setCellValueFactory(new PropertyValueFactory<>("account"));
        accountCol.setPrefWidth(150);

        TableColumn<JournalEntryData, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(250);

        TableColumn<JournalEntryData, String> debitCol = new TableColumn<>("Debit");
        debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
        debitCol.setPrefWidth(100);
        debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<JournalEntryData, String> creditCol = new TableColumn<>("Credit");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        creditCol.setPrefWidth(100);
        creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        journalTable.getColumns().addAll(dateCol, idCol, accountCol, descriptionCol, debitCol, creditCol);

        ObservableList<JournalEntryData> data = FXCollections.observableArrayList(
                new JournalEntryData("2023-10-26", "JE001", "Office Supplies", "Pens and Paper", "75.50", ""),
                new JournalEntryData("2023-10-26", "JE001", "Cash", "Pens and Paper", "", "75.50"),
                new JournalEntryData("2023-10-25", "JE002", "Grant Revenue", "Grant Received - Project A", "", "5000.00"),
                new JournalEntryData("2023-10-25", "JE002", "Cash", "Grant Received - Project A", "5000.00", ""),
                new JournalEntryData("2023-10-24", "JE003", "Rent Expense", "Monthly Rent", "1200.00", ""),
                new JournalEntryData("2023-10-24", "JE003", "Cash", "Monthly Rent", "", "1200.00")
        );
        journalTable.setItems(data);
        this.setCenter(journalTable);

        // Action Buttons (Bottom)
        HBox actionButtonsBox = new HBox();
        actionButtonsBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding
        actionButtonsBox.setSpacing(10);
        actionButtonsBox.setAlignment(Pos.CENTER_LEFT);

        Button newEntryButton = new Button("New Entry");
        Button editEntryButton = new Button("Edit Entry");
        Button deleteEntryButton = new Button("Delete Entry");

        actionButtonsBox.getChildren().addAll(newEntryButton, editEntryButton, deleteEntryButton);
        this.setBottom(actionButtonsBox);
    }

    public static class JournalEntryData {
        private final SimpleStringProperty date;
        private final SimpleStringProperty id;
        private final SimpleStringProperty account;
        private final SimpleStringProperty description;
        private final SimpleStringProperty debit;
        private final SimpleStringProperty credit;

        public JournalEntryData(String date, String id, String account, String description, String debit, String credit) {
            this.date = new SimpleStringProperty(date);
            this.id = new SimpleStringProperty(id);
            this.account = new SimpleStringProperty(account);
            this.description = new SimpleStringProperty(description);
            this.debit = new SimpleStringProperty(debit);
            this.credit = new SimpleStringProperty(credit);
        }

        public String getDate() { return date.get(); }
        public SimpleStringProperty dateProperty() { return date; }

        public String getId() { return id.get(); }
        public SimpleStringProperty idProperty() { return id; }

        public String getAccount() { return account.get(); }
        public SimpleStringProperty accountProperty() { return account; }

        public String getDescription() { return description.get(); }
        public SimpleStringProperty descriptionProperty() { return description; }

        public String getDebit() { return debit.get(); }
        public SimpleStringProperty debitProperty() { return debit; }

        public String getCredit() { return credit.get(); }
        public SimpleStringProperty creditProperty() { return credit; }
    }
}
