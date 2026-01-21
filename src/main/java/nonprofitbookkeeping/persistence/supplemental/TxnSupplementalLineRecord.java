package nonprofitbookkeeping.persistence.supplemental;

import java.math.BigDecimal;
import java.time.LocalDate;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

public class TxnSupplementalLineRecord
{
	public long id;
	public long txnId;
	public Long entryId;
	public SupplementalLineKind kind;
	public Long counterpartyPersonId;
	public String description;
	public String reference;
	public BigDecimal amount;
	public LocalDate dueDate;
	public LocalDate startDate;
	public LocalDate endDate;
	public String notes;
}
