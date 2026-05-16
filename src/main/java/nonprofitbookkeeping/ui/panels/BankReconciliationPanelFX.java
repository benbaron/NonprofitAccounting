package nonprofitbookkeeping.ui.panels;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.BankStatementRecord;
import nonprofitbookkeeping.persistence.BankStatementRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankReconciliationPanelFX extends BorderPane
{
	private final TextField bankField = new TextField("Checking");
	private final TextField accountField = new TextField("Account #1");
	private final TextField institutionField = new TextField();
	private final TextField contactField = new TextField();
	private final TextField accountNumberField = new TextField();
	private final TextField accountTypeField = new TextField("Checking");
	private final TextField signatureField = new TextField("Dual");
	private final TextField interestField = new TextField("No");
	private final TextField currencyField = new TextField("USD $");
	private final TextField yearField = new TextField(String.valueOf(Year.now().getValue()));

	private final ObservableList<ReconRow> rows = FXCollections.observableArrayList();
	private final TableView<ReconRow> table = new TableView<>(this.rows);

	public BankReconciliationPanelFX()
	{
		setPadding(PanelChrome.PANEL_PADDING);
		setTop(PanelChrome.topSection("Bank Reconciliation", buildHeader()));
		buildTable();
		setCenter(this.table);
		loadYear();
	}

	private HBox buildHeader()
	{
		GridPane meta = new GridPane();
		meta.setHgap(8);
		meta.setVgap(6);
		int row = 0;
		addMeta(meta, row++, "Bank", this.bankField);
		addMeta(meta, row++, "Account", this.accountField);
		addMeta(meta, row++, "Institution", this.institutionField);
		addMeta(meta, row++, "Contact", this.contactField);
		addMeta(meta, row++, "Account Number", this.accountNumberField);
		addMeta(meta, row++, "Account Type", this.accountTypeField);
		addMeta(meta, row++, "Signature Req", this.signatureField);
		addMeta(meta, row++, "Interest Bearing", this.interestField);
		addMeta(meta, row++, "Currency", this.currencyField);
		addMeta(meta, row, "Year", this.yearField);

		Button load = new Button("Load");
		load.setOnAction(e -> loadYear());
		Button save = new Button("Save Year");
		save.setOnAction(e -> saveYear());
		HBox bar = new HBox(10, meta, load, save);
		bar.setPadding(new Insets(0, 0, 10, 0));
		return bar;
	}

	private void addMeta(GridPane meta, int row, String label, TextField field)
	{
		meta.add(new Label(label), 0, row);
		field.setPrefWidth(180);
		meta.add(field, 1, row);
	}

	private void buildTable()
	{
		TableColumn<ReconRow, String> month = new TableColumn<>("Month");
		month.setCellValueFactory(d -> d.getValue().month);
		TableColumn<ReconRow, String> ledger = editableCol("Ledger Balance", r -> r.ledgerBalance, (r, v) -> r.ledgerBalance.set(v));
		TableColumn<ReconRow, String> stmt = editableCol("Statement Balance", r -> r.statementBalance, (r, v) -> r.statementBalance.set(v));
		TableColumn<ReconRow, String> out = editableCol("Outstanding", r -> r.outstanding, (r, v) -> r.outstanding.set(v));
		TableColumn<ReconRow, String> bankAfter = editableCol("Bank, After Outstanding", r -> r.bankAfterOutstanding, (r, v) -> r.bankAfterOutstanding.set(v));
		TableColumn<ReconRow, String> diff = editableCol("Difference", r -> r.difference, (r, v) -> r.difference.set(v));
		TableColumn<ReconRow, String> status = editableCol("Ledger is", r -> r.status, (r, v) -> r.status.set(v));
		this.table.getColumns().addAll(month, ledger, stmt, out, bankAfter, diff, status);
		this.table.setEditable(true);
	}

	private interface Getter { SimpleStringProperty get(ReconRow row); }
	private interface Setter { void set(ReconRow row, String val); }
	private TableColumn<ReconRow, String> editableCol(String name, Getter getter, Setter setter)
	{
		TableColumn<ReconRow, String> c = new TableColumn<>(name);
		c.setCellValueFactory(d -> getter.get(d.getValue()));
		c.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
		c.setOnEditCommit(e -> setter.set(e.getRowValue(), e.getNewValue()));
		return c;
	}

	private void loadYear()
	{
		this.rows.clear();
		Map<Month, BankStatementRecord> byMonth = new HashMap<>();
		try
		{
			List<BankStatementRecord> records = BankStatementRepository.findByBankAndYear(this.bankField.getText(), Integer.parseInt(this.yearField.getText()));
			for (BankStatementRecord r : records)
			{
				byMonth.put(r.getStatementDate().getMonth(), r);
			}
		}
		catch (Exception ignored)
		{
		}
		for (Month m : Month.values())
		{
			BankStatementRecord existing = byMonth.get(m);
			this.rows.add(new ReconRow(m, existing));
		}
	}

	private void saveYear()
	{
		int year = Integer.parseInt(this.yearField.getText());
		for (ReconRow r : this.rows)
		{
			BankStatementRecord rec = new BankStatementRecord();
			rec.setBankName(this.bankField.getText());
			rec.setAccountLabel(this.accountField.getText());
			rec.setStatementDate(LocalDate.of(year, r.monthValue.getValue(), r.monthValue.length(false)));
			rec.setLedgerBalance(num(r.ledgerBalance.get()));
			rec.setStatementBalance(num(r.statementBalance.get()));
			rec.setOutstanding(num(r.outstanding.get()));
			rec.setBankAfterOutstanding(num(r.bankAfterOutstanding.get()));
			rec.setDifference(num(r.difference.get()));
			rec.setLedgerStatus(r.status.get());
			rec.setInstitutionName(this.institutionField.getText());
			rec.setInstitutionContact(this.contactField.getText());
			rec.setAccountNumber(this.accountNumberField.getText());
			rec.setAccountType(this.accountTypeField.getText());
			rec.setSignatureRequirement(this.signatureField.getText());
			rec.setInterestBearing(this.interestField.getText());
			rec.setCurrency(this.currencyField.getText());
			try { BankStatementRepository.upsert(rec); } catch (SQLException ignored) { }
		}
	}

	private BigDecimal num(String s)
	{
		try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
	}

	private static class ReconRow
	{
		private final Month monthValue;
		private final SimpleStringProperty month;
		private final SimpleStringProperty ledgerBalance = new SimpleStringProperty("-");
		private final SimpleStringProperty statementBalance = new SimpleStringProperty("-");
		private final SimpleStringProperty outstanding = new SimpleStringProperty("-");
		private final SimpleStringProperty bankAfterOutstanding = new SimpleStringProperty("-");
		private final SimpleStringProperty difference = new SimpleStringProperty("-");
		private final SimpleStringProperty status = new SimpleStringProperty("Reconciled");

		private ReconRow(Month m, BankStatementRecord existing)
		{
			this.monthValue = m;
			this.month = new SimpleStringProperty(m.name());
			if (existing != null)
			{
				this.ledgerBalance.set(String.valueOf(existing.getLedgerBalance()));
				this.statementBalance.set(String.valueOf(existing.getStatementBalance()));
				this.outstanding.set(String.valueOf(existing.getOutstanding()));
				this.bankAfterOutstanding.set(String.valueOf(existing.getBankAfterOutstanding()));
				this.difference.set(String.valueOf(existing.getDifference()));
				this.status.set(existing.getLedgerStatus());
			}
		}
	}
}
