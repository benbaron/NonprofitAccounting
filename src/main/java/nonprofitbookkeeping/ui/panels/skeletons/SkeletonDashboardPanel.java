package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
// PropertyValueFactory is not strictly needed if using lambdas for all columns
// import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Ledger;
import java.util.List;
import java.util.ArrayList; // Not strictly needed if using subList directly
import java.math.BigDecimal; // For amount formatting
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;


public class SkeletonDashboardPanel extends BorderPane {

    private final TableView<AccountingTransaction> recentTransactionsTable = new TableView<>();
    private final ObservableList<AccountingTransaction> transactionDataList = FXCollections.observableArrayList();

    // Labels for key figures - made fields to be potentially updatable later
    private Label totalAssetsValueLabel = new Label("$0.00");
    private Label totalLiabilitiesValueLabel = new Label("$0.00");
    private Label equityValueLabel = new Label("$0.00");
    private Label ytdIncomeValueLabel = new Label("$0.00");

    private final CompanyChangeListener companyChangeListener = new CompanyChangeListener() {
        @Override
        public void companyChange(boolean companyNowOpen) {
            loadData();
        }
    };

    public SkeletonDashboardPanel() {
        setPadding(new Insets(15)); // Overall padding for the dashboard

        // Key Figures Section (Top)
        GridPane keyFiguresGrid = new GridPane();
        keyFiguresGrid.setPadding(new Insets(10));
        keyFiguresGrid.setHgap(20);
        keyFiguresGrid.setVgap(10);

        keyFiguresGrid.add(new Label("Total Assets:"), 0, 0);
        keyFiguresGrid.add(totalAssetsValueLabel, 1, 0);
        keyFiguresGrid.add(new Label("Total Liabilities:"), 0, 1);
        keyFiguresGrid.add(totalLiabilitiesValueLabel, 1, 1);
        keyFiguresGrid.add(new Label("Equity:"), 2, 0);
        keyFiguresGrid.add(equityValueLabel, 3, 0);
        keyFiguresGrid.add(new Label("YTD Income:"), 2, 1);
        keyFiguresGrid.add(ytdIncomeValueLabel, 3, 1);

        ScrollPane keyFiguresScrollPane = new ScrollPane();
        keyFiguresScrollPane.setContent(keyFiguresGrid);
        keyFiguresScrollPane.setFitToWidth(true);
        keyFiguresScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        keyFiguresScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setTop(keyFiguresScrollPane);

        // Recent Transactions Section (Center)
        recentTransactionsTable.setPlaceholder(new Label("No recent transactions to display."));
        recentTransactionsTable.setItems(transactionDataList); // Link data list

        TableColumn<AccountingTransaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        dateCol.setPrefWidth(100);

        TableColumn<AccountingTransaction, String> accountCol = new TableColumn<>("Account");
        // Displaying primary affected account or a summary. For simplicity, using getAccountName()
        // which might need refinement based on how AccountingTransaction represents multiple accounts.
        accountCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));
        accountCol.setPrefWidth(150);

        TableColumn<AccountingTransaction, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : cellData.getValue().getMemo()
        ));
        descriptionCol.setPrefWidth(300);

        TableColumn<AccountingTransaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> {
            BigDecimal totalAmount = cellData.getValue().getTotalAmount(); // This is sum of debits
            return new SimpleStringProperty(totalAmount != null ? totalAmount.toPlainString() : "0.00");
        });
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        recentTransactionsTable.getColumns().addAll(dateCol, accountCol, descriptionCol, amountCol);
        this.setCenter(recentTransactionsTable);
        BorderPane.setMargin(recentTransactionsTable, new Insets(10, 0, 10, 0));

        // Action Buttons (Bottom)
        HBox actionButtonsBox = new HBox();
        actionButtonsBox.setPadding(new Insets(10, 0, 0, 0));
        actionButtonsBox.setSpacing(10);
        actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button refreshButton = new Button("Refresh Dashboard");
        refreshButton.setOnAction(e -> loadData());
        actionButtonsBox.getChildren().add(refreshButton);

        this.setBottom(actionButtonsBox);

        // Initial data load and listener registration
        loadData();
        CurrentCompany.CompanyListener.addCompanyListener(companyChangeListener);
        // TODO: Consider removing listener if panel is destroyed:
        // this.sceneProperty().addListener((obs, oldScene, newScene) -> {
        // if (newScene == null) { CurrentCompany.CompanyListener.removeCompanyListener(companyChangeListener); }
        // });
    }

    private void loadData() {
        transactionDataList.clear();
        Company company = CurrentCompany.getCompany();

        if (company != null && company.getLedger() != null) {
            Ledger ledger = company.getLedger();
            List<AccountingTransaction> transactions = ledger.getTransactions();
            if (transactions != null && !transactions.isEmpty()) {
                int limit = Math.min(transactions.size(), 10); // Show last 10 transactions
                 // Add transactions in reverse order to show newest first, then reverse list for table
                List<AccountingTransaction> recent = new ArrayList<>();
                for (int i = transactions.size() - 1; i >= transactions.size() - limit; i--) {
                    recent.add(transactions.get(i));
                }
                transactionDataList.addAll(recent);
            }
        }

        // Key figures are still static as per subtask instructions for now
        // These would be calculated from company data in a real implementation
        totalAssetsValueLabel.setText("$150,000"); // Placeholder
        totalLiabilitiesValueLabel.setText("$30,000"); // Placeholder
        equityValueLabel.setText("$120,000"); // Placeholder
        ytdIncomeValueLabel.setText("$25,000"); // Placeholder

        if (transactionDataList.isEmpty()) {
            recentTransactionsTable.setPlaceholder(new Label("No transactions found for the current company."));
        } else {
            recentTransactionsTable.setPlaceholder(new Label("No recent transactions to display.")); // Default
        }
    }

    // TransactionData inner class is removed
}
