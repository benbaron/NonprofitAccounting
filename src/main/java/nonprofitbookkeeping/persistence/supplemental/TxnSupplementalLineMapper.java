package nonprofitbookkeeping.persistence.supplemental;

import nonprofitbookkeeping.model.supplemental.DeferredRevenueLine;
import nonprofitbookkeeping.model.supplemental.OtherAssetsLine;
import nonprofitbookkeeping.model.supplemental.OtherLiabilitiesLine;
import nonprofitbookkeeping.model.supplemental.PayablesLine;
import nonprofitbookkeeping.model.supplemental.PrepaidExpenseLine;
import nonprofitbookkeeping.model.supplemental.ReceivablesLine;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;

// TODO: Auto-generated Javadoc
/**
 * The Class TxnSupplementalLineMapper.
 */
public final class TxnSupplementalLineMapper
{
	
	/**
	 * Instantiates a new txn supplemental line mapper.
	 */
	private TxnSupplementalLineMapper()
	{
	}

	/**
	 * To record.
	 *
	 * @param bean the bean
	 * @return the txn supplemental line record
	 */
	public static TxnSupplementalLineRecord toRecord(TxnSupplementalLineBase bean)
	{
		TxnSupplementalLineRecord record = new TxnSupplementalLineRecord();
		record.id = bean.getId();
		record.txnId = bean.getTxnId();
		record.entryId = bean.getEntryId();
		record.kind = bean.getKind();
		record.counterpartyPersonId = bean.getCounterpartyPersonId();
		record.description = bean.getDescription();
		record.reference = bean.getReference();
		record.amount = bean.getAmount();
		record.dueDate = bean.getDueDate();
		record.startDate = bean.getStartDate();
		record.endDate = bean.getEndDate();
		record.notes = bean.getNotes();
		return record;
	}

	/**
	 * To bean.
	 *
	 * @param record the record
	 * @return the txn supplemental line base
	 */
	public static TxnSupplementalLineBase toBean(TxnSupplementalLineRecord record)
	{
		TxnSupplementalLineBase bean = createBean(record.kind);
		bean.setId(record.id);
		bean.setTxnId(record.txnId);
		bean.setEntryId(record.entryId);
		bean.setCounterpartyPersonId(record.counterpartyPersonId);
		bean.setDescription(record.description);
		bean.setReference(record.reference);
		bean.setAmount(record.amount);
		bean.setDueDate(record.dueDate);
		bean.setStartDate(record.startDate);
		bean.setEndDate(record.endDate);
		bean.setNotes(record.notes);
		return bean;
	}

	/**
	 * Creates the bean.
	 *
	 * @param kind the kind
	 * @return the txn supplemental line base
	 */
	private static TxnSupplementalLineBase createBean(SupplementalLineKind kind)
	{
		return switch (kind)
		{
			case RECEIVABLE -> new ReceivablesLine();
			case PREPAID_EXPENSE -> new PrepaidExpenseLine();
			case OTHER_ASSET -> new OtherAssetsLine();
			case DEFERRED_REVENUE -> new DeferredRevenueLine();
			case PAYABLE -> new PayablesLine();
			case OTHER_LIABILITY -> new OtherLiabilitiesLine();
		};
	}
}
