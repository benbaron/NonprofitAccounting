
package nonprofitbookkeeping.ui.javafx.supplemental;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * The Class SupplementalLineRow.
 */
public class SupplementalLineRow
{
	
	/** The id. */
	private final LongProperty id = new SimpleLongProperty(0);
	
	/** The txn id. */
	private final LongProperty txnId = new SimpleLongProperty(0);
	
	/** The entry id. */
	private final ObjectProperty<Long> entryId =
		new SimpleObjectProperty<>(null);
	
	/** The counterparty person id. */
	private final ObjectProperty<Long> counterpartyPersonId =
		new SimpleObjectProperty<>(null);
	
	/** The description. */
	private final StringProperty description = new SimpleStringProperty("");
	
	/** The reference. */
	private final StringProperty reference = new SimpleStringProperty("");
	
	/** The amount. */
	private final ObjectProperty<BigDecimal> amount =
		new SimpleObjectProperty<>(BigDecimal.ZERO);
	
	/** The due date. */
	private final ObjectProperty<LocalDate> dueDate =
		new SimpleObjectProperty<>(null);
	
	/** The start date. */
	private final ObjectProperty<LocalDate> startDate =
		new SimpleObjectProperty<>(null);
	
	/** The end date. */
	private final ObjectProperty<LocalDate> endDate =
		new SimpleObjectProperty<>(null);
	
	/** The notes. */
	private final StringProperty notes = new SimpleStringProperty("");
	
	/**
	 * Id property.
	 *
	 * @return the long property
	 */
	public LongProperty idProperty()
	{
		return this.id;
		
	}
	
	/**
	 * Txn id property.
	 *
	 * @return the long property
	 */
	public LongProperty txnIdProperty()
	{
		return this.txnId;
		
	}
	
	/**
	 * Entry id property.
	 *
	 * @return the object property
	 */
	public ObjectProperty<Long> entryIdProperty()
	{
		return this.entryId;
		
	}
	
	/**
	 * Counterparty person id property.
	 *
	 * @return the object property
	 */
	public ObjectProperty<Long> counterpartyPersonIdProperty()
	{
		return this.counterpartyPersonId;
		
	}
	
	/**
	 * Description property.
	 *
	 * @return the string property
	 */
	public StringProperty descriptionProperty()
	{
		return this.description;
		
	}
	
	/**
	 * Reference property.
	 *
	 * @return the string property
	 */
	public StringProperty referenceProperty()
	{
		return this.reference;
		
	}
	
	/**
	 * Amount property.
	 *
	 * @return the object property
	 */
	public ObjectProperty<BigDecimal> amountProperty()
	{
		return this.amount;
		
	}
	
	/**
	 * Due date property.
	 *
	 * @return the object property
	 */
	public ObjectProperty<LocalDate> dueDateProperty()
	{
		return this.dueDate;
		
	}
	
	/**
	 * Start date property.
	 *
	 * @return the object property
	 */
	public ObjectProperty<LocalDate> startDateProperty()
	{
		return this.startDate;
		
	}
	
	/**
	 * End date property.
	 *
	 * @return the object property
	 */
	public ObjectProperty<LocalDate> endDateProperty()
	{
		return this.endDate;
		
	}
	
	/**
	 * Notes property.
	 *
	 * @return the string property
	 */
	public StringProperty notesProperty()
	{
		return this.notes;
		
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId()
	{
		return this.id.get();
		
	}
	
	/**
	 * Sets the id.
	 *
	 * @param value the new id
	 */
	public void setId(long value)
	{
		this.id.set(value);
		
	}
	
	/**
	 * Gets the txn id.
	 *
	 * @return the txn id
	 */
	public long getTxnId()
	{
		return this.txnId.get();
		
	}
	
	/**
	 * Sets the txn id.
	 *
	 * @param value the new txn id
	 */
	public void setTxnId(long value)
	{
		this.txnId.set(value);
		
	}
	
	/**
	 * Gets the entry id.
	 *
	 * @return the entry id
	 */
	public Long getEntryId()
	{
		return this.entryId.get();
		
	}
	
	/**
	 * Sets the entry id.
	 *
	 * @param value the new entry id
	 */
	public void setEntryId(Long value)
	{
		this.entryId.set(value);
		
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription()
	{
		return this.description.get();
		
	}
	
	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	public void setDescription(String value)
	{
		this.description.set(value);
		
	}
	
	/**
	 * Gets the reference.
	 *
	 * @return the reference
	 */
	public String getReference()
	{
		return this.reference.get();
		
	}
	
	/**
	 * Sets the reference.
	 *
	 * @param value the new reference
	 */
	public void setReference(String value)
	{
		this.reference.set(value);
		
	}
	
	/**
	 * Gets the amount.
	 *
	 * @return the amount
	 */
	public BigDecimal getAmount()
	{
		return this.amount.get();
		
	}
	
	/**
	 * Sets the amount.
	 *
	 * @param value the new amount
	 */
	public void setAmount(BigDecimal value)
	{
		this.amount.set(value);
		
	}
	
	/**
	 * Gets the due date.
	 *
	 * @return the due date
	 */
	public LocalDate getDueDate()
	{
		return this.dueDate.get();
		
	}
	
	/**
	 * Sets the due date.
	 *
	 * @param value the new due date
	 */
	public void setDueDate(LocalDate value)
	{
		this.dueDate.set(value);
		
	}
	
	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public LocalDate getStartDate()
	{
		return this.startDate.get();
		
	}
	
	/**
	 * Sets the start date.
	 *
	 * @param value the new start date
	 */
	public void setStartDate(LocalDate value)
	{
		this.startDate.set(value);
		
	}
	
	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public LocalDate getEndDate()
	{
		return this.endDate.get();
		
	}
	
	/**
	 * Sets the end date.
	 *
	 * @param value the new end date
	 */
	public void setEndDate(LocalDate value)
	{
		this.endDate.set(value);
		
	}
	
	/**
	 * Gets the notes.
	 *
	 * @return the notes
	 */
	public String getNotes()
	{
		return this.notes.get();
		
	}
	
	/**
	 * Sets the notes.
	 *
	 * @param value the new notes
	 */
	public void setNotes(String value)
	{
		this.notes.set(value);
		
	}
	
}
