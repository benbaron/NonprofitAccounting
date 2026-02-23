package nonprofitbookkeeping.persistence.supplemental;

import java.math.BigDecimal;
import java.time.LocalDate;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

// TODO: Auto-generated Javadoc
/**
 * The Class TxnSupplementalLineRecord.
 */
public class TxnSupplementalLineRecord
{
	
	/** The id. */
	public long id;
	
	/** The txn id. */
	public long txnId;
	
	/** The entry id. */
	public Long entryId;
	
	/** The kind. */
	public SupplementalLineKind kind;
	
	/** The counterparty person id. */
	public Long counterpartyPersonId;
	
	/** The description. */
	public String description;
	
	/** The reference. */
	public String reference;
	
	/** The amount. */
	public BigDecimal amount;
	
	/** The due date. */
	public LocalDate dueDate;
	
	/** The start date. */
	public LocalDate startDate;
	
	/** The end date. */
	public LocalDate endDate;
	
	/** The notes. */
	public String notes;
}
