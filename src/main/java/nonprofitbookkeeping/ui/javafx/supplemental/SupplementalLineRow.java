package nonprofitbookkeeping.ui.javafx.supplemental;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SupplementalLineRow
{
	private final LongProperty id = new SimpleLongProperty(0);
	private final LongProperty txnId = new SimpleLongProperty(0);
	private final ObjectProperty<Long> entryId = new SimpleObjectProperty<>(null);
	private final ObjectProperty<Long> counterpartyPersonId = new SimpleObjectProperty<>(null);
	private final StringProperty description = new SimpleStringProperty("");
	private final StringProperty reference = new SimpleStringProperty("");
	private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
	private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>(null);
	private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(null);
	private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(null);
	private final StringProperty notes = new SimpleStringProperty("");

	public LongProperty idProperty()
	{
		return this.id;
	}

	public LongProperty txnIdProperty()
	{
		return this.txnId;
	}

	public ObjectProperty<Long> entryIdProperty()
	{
		return this.entryId;
	}

	public ObjectProperty<Long> counterpartyPersonIdProperty()
	{
		return this.counterpartyPersonId;
	}

	public StringProperty descriptionProperty()
	{
		return this.description;
	}

	public StringProperty referenceProperty()
	{
		return this.reference;
	}

	public ObjectProperty<BigDecimal> amountProperty()
	{
		return this.amount;
	}

	public ObjectProperty<LocalDate> dueDateProperty()
	{
		return this.dueDate;
	}

	public ObjectProperty<LocalDate> startDateProperty()
	{
		return this.startDate;
	}

	public ObjectProperty<LocalDate> endDateProperty()
	{
		return this.endDate;
	}

	public StringProperty notesProperty()
	{
		return this.notes;
	}

	public long getId()
	{
		return this.id.get();
	}

	public void setId(long value)
	{
		this.id.set(value);
	}

	public long getTxnId()
	{
		return this.txnId.get();
	}

	public void setTxnId(long value)
	{
		this.txnId.set(value);
	}

	public Long getEntryId()
	{
		return this.entryId.get();
	}

	public void setEntryId(Long value)
	{
		this.entryId.set(value);
	}

	public String getDescription()
	{
		return this.description.get();
	}

	public void setDescription(String value)
	{
		this.description.set(value);
	}

	public String getReference()
	{
		return this.reference.get();
	}

	public void setReference(String value)
	{
		this.reference.set(value);
	}

	public BigDecimal getAmount()
	{
		return this.amount.get();
	}

	public void setAmount(BigDecimal value)
	{
		this.amount.set(value);
	}

	public LocalDate getDueDate()
	{
		return this.dueDate.get();
	}

	public void setDueDate(LocalDate value)
	{
		this.dueDate.set(value);
	}

	public LocalDate getStartDate()
	{
		return this.startDate.get();
	}

	public void setStartDate(LocalDate value)
	{
		this.startDate.set(value);
	}

	public LocalDate getEndDate()
	{
		return this.endDate.get();
	}

	public void setEndDate(LocalDate value)
	{
		this.endDate.set(value);
	}

	public String getNotes()
	{
		return this.notes.get();
	}

	public void setNotes(String value)
	{
		this.notes.set(value);
	}
}
