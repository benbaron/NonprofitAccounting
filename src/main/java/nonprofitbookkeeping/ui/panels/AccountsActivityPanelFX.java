
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Ledger;

/**
 * JavaFX rewrite of {@code AccountsActivityPanel}.
 */
public class AccountsActivityPanelFX extends BorderPane
{
	
	/* ───────────────────────── UI fields ───────────────────────── */
	private final ComboBox<String> accountSelector = new ComboBox<>();
	private final TextField filterDateField = new TextField();
	private final TextField filterMemoField = new TextField();
	private final TextField filterAmountField = new TextField();
	
	private final TableView<TransactionRow> table = new TableView<>();
	private final ObservableList<TransactionRow> backingList = FXCollections.observableArrayList();
	
	/* Back‑reference */
	private final List<AccountingTransaction> transactions;
	
	public AccountsActivityPanelFX(Ledger ledger)
	{
		this.transactions = ledger.getTransactions();
		setPadding(new Insets(10));
		
		/* NORTH: selectors + filters */
		VBox top = new VBox(10);
		top.getChildren().addAll(selectorPane(), filterPane());
		setTop(top);
		
		/* CENTER: table */
		configureTable();
		setCenter(new TitledPane("Ledger", this.table)
		{
			{
				setCollapsible(false);
			}
			
		});
		
		/* SOUTH: buttons */
		setBottom(buttonBar());
		
		/* initial fill */
		applyFilters();
	}
	
	/* ───────────────────────── Builders ───────────────────────── */
	
	private HBox selectorPane()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(5));
		box.setStyle(
			"-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");
		this.accountSelector.getItems().addAll(Ledger.getAccountNames());
		this.accountSelector.getSelectionModel().selectFirst();
		this.accountSelector.setOnAction(e -> applyFilters());
		box.getChildren().addAll(new Label("Account:"), this.accountSelector);
		return box;
	}
	
	private HBox filterPane()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(5));
		box.setStyle(
			"-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");
		Button apply = new Button("Apply");
		apply.setOnAction(e -> applyFilters());
		box.getChildren().addAll(
			new Label("Date:"), this.filterDateField,
			new Label("Memo:"), this.filterMemoField,
			new Label("Amount:"), this.filterAmountField,
			apply);
		return box;
	}
	
	private static HBox buttonBar()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(10));
		Button reconcile = new Button("Reconcile");
		reconcile.setOnAction(
			e -> new Alert(Alert.AlertType.INFORMATION, "Reconciliation process would start here.")
				.showAndWait());
		Button importBtn = new Button("Import Statement (CSV/QIF/OFX)");
		importBtn.setOnAction(
			e -> new Alert(Alert.AlertType.INFORMATION, "Import dialog not implemented.")
				.showAndWait());
		box.getChildren().addAll(reconcile, importBtn);
		return box;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" }) private void configureTable()
	{
		TableColumn<TransactionRow, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(d -> d.getValue().date);
		
		TableColumn<TransactionRow, String> descCol = new TableColumn<>("Description");
		descCol.setCellValueFactory(d -> d.getValue().description);
		
		TableColumn<TransactionRow, BigDecimal> amtCol = new TableColumn<>("Amount");
		amtCol.setCellValueFactory(d -> d.getValue().amount);
		
		TableColumn<TransactionRow, BigDecimal> balCol = new TableColumn<>("Balance");
		balCol.setCellValueFactory(d -> d.getValue().balance);
		
		TableColumn<TransactionRow, String> memoCol = new TableColumn<>("Memo");
		memoCol.setCellValueFactory(d -> d.getValue().memo);
		
		this.table.getColumns().addAll(dateCol, descCol, amtCol, balCol, memoCol);
		this.table.setItems(this.backingList);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	/* ───────────────────────── Logic ───────────────────────── */
	BigDecimal amountFilter = null;
	
	private void applyFilters()
	{
		String acct = this.accountSelector.getValue();
		String dateFilter = this.filterDateField.getText().trim();
		String memoFilter = this.filterMemoField.getText().trim().toLowerCase();
		String amountText = this.filterAmountField.getText().trim();
		
		try
		{
			if (!amountText.isEmpty())
				this.amountFilter = new BigDecimal(amountText);
		}
		catch (@SuppressWarnings("unused") NumberFormatException ignore)
		{
		}
		
		Predicate<AccountingTransaction> predicate = t -> t.getAccountName().equals(acct) &&
			(dateFilter.isEmpty() || t.getDate().contains(dateFilter)) &&
			(memoFilter.isEmpty() || t.getMemo().toLowerCase().contains(memoFilter)) &&
			(this.amountFilter == null || t.getTotalAmount().compareTo(this.amountFilter) == 0);
		
		List<AccountingTransaction> filtered =
			this.transactions.stream().filter(predicate).collect(Collectors.toList());
		this.backingList.setAll(filtered.stream().map(TransactionRow::new).collect(Collectors.toList()));
	}
	
	/* ───────────────────────── Table row ───────────────────────── */
	public static class TransactionRow
	{
		final SimpleStringProperty date = new SimpleStringProperty();
		final SimpleStringProperty description = new SimpleStringProperty();
		final SimpleObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
		final SimpleObjectProperty<BigDecimal> balance = new SimpleObjectProperty<>();
		final SimpleStringProperty memo = new SimpleStringProperty();
		
		TransactionRow(AccountingTransaction t)
		{
			this.date.set(t.getDate());
			this.description.set(t.getDescription());
			this.amount.set(t.getTotalAmount());
			this.balance.set(t.getAccountBalance());
			this.memo.set(t.getMemo());
		}
		
	}
	
}
