package nonprofitbookkeeping.ui.javafx.supplemental;

import java.util.ArrayList;
import java.util.List;

import nonprofitbookkeeping.model.supplemental.DeferredRevenueLine;
import nonprofitbookkeeping.model.supplemental.OtherAssetsLine;
import nonprofitbookkeeping.model.supplemental.OtherLiabilitiesLine;
import nonprofitbookkeeping.model.supplemental.PayablesLine;
import nonprofitbookkeeping.model.supplemental.PrepaidExpenseLine;
import nonprofitbookkeeping.model.supplemental.ReceivablesLine;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;

public final class SupplementalLinesFxAdapter
{
	private SupplementalLinesFxAdapter()
	{
	}

	public static SupplementalLineRow toRow(TxnSupplementalLineBase bean)
	{
		SupplementalLineRow row = new SupplementalLineRow();
		row.setId(bean.getId());
		row.setTxnId(bean.getTxnId());
		row.setEntryId(bean.getEntryId());
		row.counterpartyPersonIdProperty().set(bean.getCounterpartyPersonId());
		row.setDescription(bean.getDescription());
		row.setReference(bean.getReference());
		row.setAmount(bean.getAmount());
		row.setDueDate(bean.getDueDate());
		row.setStartDate(bean.getStartDate());
		row.setEndDate(bean.getEndDate());
		row.setNotes(bean.getNotes());
		return row;
	}

	public static <T extends TxnSupplementalLineBase> T toBean(
		SupplementalLineKind kind, SupplementalLineRow row)
	{
		TxnSupplementalLineBase bean = createBean(kind);
		bean.setId(row.getId());
		bean.setTxnId(row.getTxnId());
		bean.setEntryId(row.getEntryId());
		bean.setCounterpartyPersonId(row.counterpartyPersonIdProperty().get());
		bean.setDescription(row.getDescription());
		bean.setReference(row.getReference());
		bean.setAmount(row.getAmount());
		bean.setDueDate(row.getDueDate());
		bean.setStartDate(row.getStartDate());
		bean.setEndDate(row.getEndDate());
		bean.setNotes(row.getNotes());

		@SuppressWarnings("unchecked")
		T result = (T) bean;
		return result;
	}

	public static List<SupplementalLineRow> toRows(
		List<? extends TxnSupplementalLineBase> beans)
	{
		List<SupplementalLineRow> rows = new ArrayList<>();
		if (beans == null)
		{
			return rows;
		}
		for (TxnSupplementalLineBase bean : beans)
		{
			rows.add(toRow(bean));
		}
		return rows;
	}

	public static <T extends TxnSupplementalLineBase> List<T> toBeans(
		SupplementalLineKind kind, List<SupplementalLineRow> rows)
	{
		List<T> beans = new ArrayList<>();
		if (rows == null)
		{
			return beans;
		}
		for (SupplementalLineRow row : rows)
		{
			beans.add(toBean(kind, row));
		}
		return beans;
	}

	private static TxnSupplementalLineBase createBean(SupplementalLineKind kind)
	{
		return switch (kind)
		{
			case RECEIVABLE -> new ReceivablesLine();
			case PAYABLE -> new PayablesLine();
			case PREPAID_EXPENSE -> new PrepaidExpenseLine();
			case DEFERRED_REVENUE -> new DeferredRevenueLine();
			case OTHER_ASSET -> new OtherAssetsLine();
			case OTHER_LIABILITY -> new OtherLiabilitiesLine();
		};
	}
}
