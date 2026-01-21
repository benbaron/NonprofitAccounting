package nonprofitbookkeeping.model.supplemental;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class TxnSupplementalLineBase
{
	private long id;
	private long txnId;
	private Long entryId;
	private Long counterpartyPersonId;
	private String description;
	private String reference;
	private BigDecimal amount;
	private LocalDate dueDate;
	private LocalDate startDate;
	private LocalDate endDate;
	private String notes;

	public abstract SupplementalLineKind getKind();

	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public long getTxnId()
	{
		return this.txnId;
	}

	public void setTxnId(long txnId)
	{
		this.txnId = txnId;
	}

	public Long getEntryId()
	{
		return this.entryId;
	}

	public void setEntryId(Long entryId)
	{
		this.entryId = entryId;
	}

	public Long getCounterpartyPersonId()
	{
		return this.counterpartyPersonId;
	}

	public void setCounterpartyPersonId(Long counterpartyPersonId)
	{
		this.counterpartyPersonId = counterpartyPersonId;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getReference()
	{
		return this.reference;
	}

	public void setReference(String reference)
	{
		this.reference = reference;
	}

	public BigDecimal getAmount()
	{
		return this.amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public LocalDate getDueDate()
	{
		return this.dueDate;
	}

	public void setDueDate(LocalDate dueDate)
	{
		this.dueDate = dueDate;
	}

	public LocalDate getStartDate()
	{
		return this.startDate;
	}

	public void setStartDate(LocalDate startDate)
	{
		this.startDate = startDate;
	}

	public LocalDate getEndDate()
	{
		return this.endDate;
	}

	public void setEndDate(LocalDate endDate)
	{
		this.endDate = endDate;
	}

	public String getNotes()
	{
		return this.notes;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
	}
}
