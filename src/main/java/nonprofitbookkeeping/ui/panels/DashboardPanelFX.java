
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import nonprofitbookkeeping.model.*;

public class DashboardPanelFX extends BorderPane
{
	
	/* ── “company loaded” banner ─────────────────────────────── */
	private final Label companyLbl = new Label("No company loaded");
	private final Button reloadBtn = new Button("Reload");
	
	/* account / filter controls */
	private final ComboBox<String> accountSelector = new ComboBox<>();
	private final TextField dateFilter = new TextField();
	private final TextField memoFilter = new TextField();
	private final TextField amountFilter = new TextField();
	
	/* data table */
	private final TableView<Row> table = new TableView<>();
	private final ObservableList<Row> rows = FXCollections.observableArrayList();
	private List<AccountingTransaction> allTxns = List.of(); // empty until a file is open
	private BigDecimal amtF = null;
	private ReadOnlyObjectProperty<Company> prop;

	
	/**
	 * Constructor DashboardPanelFX
	 */
	public DashboardPanelFX()
	{
		setPadding(new Insets(10));
		
		buildTopBanner();  // always visible
		buildTopFilters(); // filters + selector
		buildTable();
		setCenter(new TitledPane("Journal Transactions", this.table)
		{
			{
				setCollapsible(false);
			}
			
		});
		
		// If a company is loaded later, refresh() will populate everything:
		this.prop = Company.getCompanyProperty();
		this.prop.addListener((obs, o, n) -> loadCompany(n));
		
		loadCompany(Company.getCompany()); // initial
	}
	
	/**
	 * Top Banner widget
	 */
	private void buildTopBanner()
	{
		this.companyLbl.getStyleClass().add("company-indicator");
		this.reloadBtn.setOnAction(e -> loadCompany(Company.getCompany()));
		HBox banner = new HBox(10, new Label("Current Company:"), this.companyLbl, this.reloadBtn);
		banner.setPadding(new Insets(4));
		banner.setStyle("-fx-background-color:#f0f0f0; -fx-border-color:lightgray;");
		setTop(banner);
	}
	
	/**
	 * Top Filters widget
	 */
	private void buildTopFilters()
	{
		/* selector */
		HBox selector = new HBox(10, new Label("Account:"), this.accountSelector);
		selector.setPadding(new Insets(5));
		selector.setStyle("-fx-border-color: lightgray;");
		
		/* filters */
		Button apply = new Button("Apply");
		apply.setOnAction(e -> refresh());
		HBox filter = new HBox(10,
			new Label("Date (yyyy-mm-dd):"), this.dateFilter,
			new Label("Memo:"), this.memoFilter,
			new Label("Amount:"), this.amountFilter,
			apply);
		filter.setPadding(new Insets(5));
		filter.setStyle("-fx-border-color: lightgray;");
		
		VBox top = new VBox(selector, filter);
		setMargin(top, new Insets(0, 0, 5, 0));
		setTop(new VBox(getTop(), top)); // banner + filters
	}
	
	/**
	 * Build Table widget
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<Row, Object> dateCol = mkCol("Date", r -> r.date);
		TableColumn<Row, Object> descCol = mkCol("Description", r -> r.desc);
		TableColumn<Row, Object> amtCol = mkCol("Amount", r -> r.amount);
		TableColumn<Row, Object> balCol = mkCol("Balance", r -> r.balance);
		TableColumn<Row, Object> memoCol = mkCol("Memo", r -> r.memo);
		
		this.table.getColumns().addAll(dateCol, descCol, amtCol, balCol, memoCol);
		this.table.setItems(this.rows);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	/**
	 * TableColumn widget builder
	 * 
	 * @param <T>
	 * @param n
	 * @param f
	 * @return the table column
	 */
	private static <T> TableColumn<Row, T> mkCol(String n, Function<Row, T> f)
	{
		TableColumn<Row, T> c = new TableColumn<>(n);
		c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(f.apply(cd.getValue())));
		return c;
	}
	
	/** 
	 * Called whenever the user opens/closes a company file.
	 * @param cdf Company
	 */
	private void loadCompany(Company cdf)
	{
		
		if (cdf == null)
		{
			this.companyLbl.setText("None");
			this.accountSelector.getItems().clear();
			this.rows.clear();
			this.allTxns = List.of();
			return;
		}
		
		this.companyLbl.setText(cdf.getCompanyProfile().getCompanyName());
		this.allTxns = cdf.getLedger().getTransactions();
		refresh();
	}
	
	/**
	 * refresh
	 */
	private void refresh()
	{
		String acct = this.accountSelector.getValue();
		
		if (acct == null || this.allTxns.isEmpty())
		{
			this.rows.clear();
			return;
		}
		
		String dateF = this.dateFilter.getText().trim();
		String memoF = this.memoFilter.getText().trim().toLowerCase();
		
		
		if (!this.amountFilter.getText().isBlank())
		{
			
			try
			{
				this.amtF = new BigDecimal(this.amountFilter.getText().trim());
			}
			catch (NumberFormatException ignore)
			{
			}
			
		}
		
		Predicate<AccountingTransaction> p = t -> t.getAccountName().equals(acct) &&
			(dateF.isEmpty() ||
				t.getDate().contains(dateF)) &&
			(memoF.isEmpty() ||
				t.getMemo().toLowerCase().contains(memoF)) &&
			(this.amtF == null);
		
		List<AccountingTransaction> list =
			this.allTxns.stream().filter(p).collect(Collectors.toList());
		
		this.rows.clear();
		BigDecimal running = BigDecimal.ZERO;
		
		for (var t : list)
		{
			BigDecimal amt = t.getTotalAmount();
			running = running.add(amt);
			this.rows.add(new Row(t, amt, running));
		}
		
	}
	
	/* ---------------------- row helper ------------------------- */
	private static class Row
	{
		final StringProperty date = new SimpleStringProperty();
		final StringProperty desc = new SimpleStringProperty();
		final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
		final ObjectProperty<BigDecimal> balance = new SimpleObjectProperty<>();
		final StringProperty memo = new SimpleStringProperty();
		
		/**
		 * 
		 * Constructor Row
		 * @param t
		 * @param amt
		 * @param bal
		 */
		Row(AccountingTransaction t, BigDecimal amt, BigDecimal bal)
		{
			this.date.set(t.getDate());
			this.desc.set(t.getDescription());
			this.amount.set(amt);
			this.balance.set(bal);
			this.memo.set(t.getMemo());
		}
		
	}
	
}
