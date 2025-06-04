package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SkeletonCoaPanel extends BorderPane {

    public SkeletonCoaPanel() {
        setPadding(new Insets(15)); // Overall padding

        // Chart of Accounts Tree Table (Center)
        TreeTableView<AccountData> treeTableView = new TreeTableView<>();
        treeTableView.setPlaceholder(new Label("No chart of accounts data to display."));

        TreeTableColumn<AccountData, String> numberCol = new TreeTableColumn<>("Account Number");
        numberCol.setPrefWidth(150);
        numberCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("number"));

        TreeTableColumn<AccountData, String> nameCol = new TreeTableColumn<>("Account Name");
        nameCol.setPrefWidth(300);
        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));

        TreeTableColumn<AccountData, String> typeCol = new TreeTableColumn<>("Type");
        typeCol.setPrefWidth(150);
        typeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));

        treeTableView.getColumns().addAll(numberCol, nameCol, typeCol);

        // Create data
        TreeItem<AccountData> rootNode = new TreeItem<>(new AccountData("0", "COA Root", ""));
        rootNode.setExpanded(true);

        TreeItem<AccountData> assets = new TreeItem<>(new AccountData("1000", "Assets", "Category"));
        assets.setExpanded(true);
        TreeItem<AccountData> currentAssets = new TreeItem<>(new AccountData("1100", "Current Assets", "Asset"));
        currentAssets.setExpanded(true);
        TreeItem<AccountData> bankAccounts = new TreeItem<>(new AccountData("1110", "Bank Accounts", "Asset"));
        bankAccounts.setExpanded(true);
        bankAccounts.getChildren().add(new TreeItem<>(new AccountData("1111", "Checking Account", "Asset")));
        bankAccounts.getChildren().add(new TreeItem<>(new AccountData("1112", "Savings Account", "Asset")));
        currentAssets.getChildren().add(bankAccounts);
        TreeItem<AccountData> receivables = new TreeItem<>(new AccountData("1200", "Accounts Receivable", "Asset"));
        currentAssets.getChildren().add(receivables);
        assets.getChildren().add(currentAssets);

        TreeItem<AccountData> fixedAssets = new TreeItem<>(new AccountData("1500", "Fixed Assets", "Asset"));
        fixedAssets.getChildren().add(new TreeItem<>(new AccountData("1510", "Equipment", "Asset")));
        assets.getChildren().add(fixedAssets);
        rootNode.getChildren().add(assets);

        TreeItem<AccountData> liabilities = new TreeItem<>(new AccountData("2000", "Liabilities", "Category"));
        liabilities.setExpanded(true);
        TreeItem<AccountData> currentLiabilities = new TreeItem<>(new AccountData("2100", "Current Liabilities", "Liability"));
        currentLiabilities.getChildren().add(new TreeItem<>(new AccountData("2110", "Accounts Payable", "Liability")));
        liabilities.getChildren().add(currentLiabilities);
        rootNode.getChildren().add(liabilities);

        TreeItem<AccountData> equity = new TreeItem<>(new AccountData("3000", "Equity", "Category"));
        equity.setExpanded(true);
        equity.getChildren().add(new TreeItem<>(new AccountData("3100", "Net Assets", "Equity")));
        rootNode.getChildren().add(equity);

        TreeItem<AccountData> income = new TreeItem<>(new AccountData("4000", "Income", "Category"));
        income.setExpanded(true);
        income.getChildren().add(new TreeItem<>(new AccountData("4100", "Donations", "Income")));
        income.getChildren().add(new TreeItem<>(new AccountData("4200", "Grants", "Income")));
        rootNode.getChildren().add(income);

        TreeItem<AccountData> expenses = new TreeItem<>(new AccountData("5000", "Expenses", "Category"));
        expenses.setExpanded(true);
        expenses.getChildren().add(new TreeItem<>(new AccountData("5100", "Office Supplies", "Expense")));
        expenses.getChildren().add(new TreeItem<>(new AccountData("5200", "Rent", "Expense")));
        rootNode.getChildren().add(expenses);

        treeTableView.setRoot(rootNode);
        treeTableView.setShowRoot(false); // Hide the dummy root

        this.setCenter(treeTableView);

        // Action Buttons (Bottom)
        HBox actionButtonsBox = new HBox();
        actionButtonsBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding
        actionButtonsBox.setSpacing(10);
        actionButtonsBox.setAlignment(Pos.CENTER_LEFT);

        Button addAccountButton = new Button("Add Account");
        Button editAccountButton = new Button("Edit Account");
        Button deleteAccountButton = new Button("Delete Account");

        actionButtonsBox.getChildren().addAll(addAccountButton, editAccountButton, deleteAccountButton);
        this.setBottom(actionButtonsBox);
    }

    public static class AccountData {
        private final SimpleStringProperty number;
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;

        public AccountData(String number, String name, String type) {
            this.number = new SimpleStringProperty(number);
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
        }

        public String getNumber() { return number.get(); }
        public SimpleStringProperty numberProperty() { return number; }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getType() { return type.get(); }
        public SimpleStringProperty typeProperty() { return type; }
    }
}
