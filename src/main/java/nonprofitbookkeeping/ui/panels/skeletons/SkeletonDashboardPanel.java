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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class SkeletonDashboardPanel extends BorderPane {

    public SkeletonDashboardPanel() {
        setPadding(new Insets(15)); // Overall padding for the dashboard

        // Key Figures Section (Top)
        GridPane keyFiguresGrid = new GridPane();
        keyFiguresGrid.setPadding(new Insets(10));
        keyFiguresGrid.setHgap(20);
        keyFiguresGrid.setVgap(10);

        keyFiguresGrid.add(new Label("Total Assets:"), 0, 0);
        keyFiguresGrid.add(new Label("$150,000"), 1, 0);
        keyFiguresGrid.add(new Label("Total Liabilities:"), 0, 1);
        keyFiguresGrid.add(new Label("$30,000"), 1, 1);
        keyFiguresGrid.add(new Label("Equity:"), 2, 0);
        keyFiguresGrid.add(new Label("$120,000"), 3, 0);
        keyFiguresGrid.add(new Label("YTD Income:"), 2, 1);
        keyFiguresGrid.add(new Label("$25,000"), 3, 1);

        this.setTop(keyFiguresGrid);

        // Recent Transactions Section (Center)
        TableView<TransactionData> transactionTable = new TableView<>();
        transactionTable.setPlaceholder(new Label("No recent transactions to display."));

        TableColumn<TransactionData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<TransactionData, String> accountCol = new TableColumn<>("Account");
        accountCol.setCellValueFactory(new PropertyValueFactory<>("account"));
        accountCol.setPrefWidth(150);

        TableColumn<TransactionData, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(300);

        TableColumn<TransactionData, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");


        transactionTable.getColumns().addAll(dateCol, accountCol, descriptionCol, amountCol);

        ObservableList<TransactionData> data = FXCollections.observableArrayList(
                new TransactionData("2023-10-26", "Checking", "Office Supplies Purchase", "$ -75.50"),
                new TransactionData("2023-10-25", "Grants Receivable", "Grant Payment Received", "$ 5,000.00"),
                new TransactionData("2023-10-24", "Operating Expenses", "Rent Payment", "$ -1,200.00")
        );
        transactionTable.setItems(data);
        this.setCenter(transactionTable);
        BorderPane.setMargin(transactionTable, new Insets(10, 0, 10, 0)); // Add some vertical margin

        // Action Buttons (Bottom)
        HBox actionButtonsBox = new HBox();
        actionButtonsBox.setPadding(new Insets(10, 0, 0, 0)); // Padding at the top of HBox
        actionButtonsBox.setSpacing(10);
        actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button refreshButton = new Button("Refresh Dashboard");
        actionButtonsBox.getChildren().add(refreshButton);

        this.setBottom(actionButtonsBox);
    }

    public static class TransactionData {
        private final SimpleStringProperty date;
        private final SimpleStringProperty account;
        private final SimpleStringProperty description;
        private final SimpleStringProperty amount;

        public TransactionData(String date, String account, String description, String amount) {
            this.date = new SimpleStringProperty(date);
            this.account = new SimpleStringProperty(account);
            this.description = new SimpleStringProperty(description);
            this.amount = new SimpleStringProperty(amount);
        }

        public String getDate() { return date.get(); }
        public void setDate(String val) { date.set(val); }
        public SimpleStringProperty dateProperty() { return date; }

        public String getAccount() { return account.get(); }
        public void setAccount(String val) { account.set(val); }
        public SimpleStringProperty accountProperty() { return account; }

        public String getDescription() { return description.get(); }
        public void setDescription(String val) { description.set(val); }
        public SimpleStringProperty descriptionProperty() { return description; }

        public String getAmount() { return amount.get(); }
        public void setAmount(String val) { amount.set(val); }
        public SimpleStringProperty amountProperty() { return amount; }
    }
}
