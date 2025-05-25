
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import nonprofitbookkeeping.model.JournalEntry;

/**
 * JavaFX rewrite of {@code NewTransactionPanel}. Presents a simple form for
 * entering or editing a single debit/credit journal entry (one account).
 * On Save it passes a populated {@link JournalEntry} to the provided callback.
 */
public class NewTransactionPanelFX extends BorderPane
{
	private final DatePicker datePicker = new DatePicker(LocalDate.now());
	private final ComboBox<String> accountBox = new ComboBox<>();
	private final TextField debitField = new TextField("0.00");
	private final TextField creditField = new TextField("0.00");
	private final TextArea memoArea = new TextArea();
	
	private final Consumer<JournalEntry> onSave;
	private final String existingId;
	
	/** Create a blank transaction form. */
	public NewTransactionPanelFX(Consumer<JournalEntry> onSave)
	{
		this(null, onSave);
	}
	
	/** Edit constructor — fills fields with existing entry values. */
	public NewTransactionPanelFX(JournalEntry existing, Consumer<JournalEntry> onSave)
	{
		this.onSave = onSave;
		this.existingId = existing != null ? existing.getId() : null;
		setPadding(new Insets(10));
		buildUI();
		if (existing != null)
			fill(existing);
	}
	
	private void buildUI()
	{
		this.accountBox.getItems().addAll("Cash", "Bank Checking", "Donations", "Supplies Expense",
			"Accounts Receivable");
		this.accountBox.getSelectionModel().selectFirst();
		this.memoArea.setPrefRowCount(3);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.setPadding(new Insets(10));
		grid.addRow(0, new Label("Date:"), this.datePicker);
		grid.addRow(1, new Label("Account:"), this.accountBox);
		grid.addRow(2, new Label("Debit:"), this.debitField);
		grid.addRow(3, new Label("Credit:"), this.creditField);
		grid.addRow(4, new Label("Memo:"), this.memoArea);
		setCenter(grid);
		
		Button save = new Button("Save");
		save.setDefaultButton(true);
		save.setOnAction(e -> save());
		setBottom(save);
		BorderPane.setMargin(save, new Insets(8));
	}
	
	private void fill(JournalEntry e)
	{
		this.datePicker.setValue(LocalDate.parse(e.getDate()));
		this.accountBox.setValue(e.getAccount());
		this.debitField.setText(e.getDebit().toPlainString());
		this.creditField.setText(e.getCredit().toPlainString());
		this.memoArea.setText(e.getMemo());
	}
	
	private void save()
	{
		
		try
		{
			BigDecimal debit = new BigDecimal(this.debitField.getText().trim());
			BigDecimal credit = new BigDecimal(this.creditField.getText().trim());
			String id = this.existingId != null ? this.existingId : UUID.randomUUID().toString();
			JournalEntry entry = new JournalEntry(id,
				this.datePicker.getValue().toString(),
				this.accountBox.getValue(),
				debit, credit,
				this.memoArea.getText());
			if (this.onSave != null)
				this.onSave.accept(entry);
		}
		catch (@SuppressWarnings("unused") NumberFormatException ex)
		{
			new Alert(Alert.AlertType.ERROR, "Debit and Credit must be numeric").showAndWait();
		}
		
	}
	
}
