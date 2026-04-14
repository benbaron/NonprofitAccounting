package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.LedgerMatchRecord;
import nonprofitbookkeeping.persistence.BankingTransactionRepository;
import nonprofitbookkeeping.persistence.LedgerMatchRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OperationalReconciliationService
{
	public void confirmMatch(LedgerMatchRecord link) throws SQLException
	{
		if (link == null || link.getBankingRecordId() == null || link.getLedgerRecordId() == null)
		{
			throw new IllegalArgumentException("link, bankingRecordId, and ledgerRecordId are required");
		}
		link.setLinkStatus("ACTIVE");
		if (link.getReviewedAt() == null)
		{
			link.setReviewedAt(LocalDateTime.now());
		}
		LedgerMatchRepository.upsert(link);
		BankingTransactionRepository.transitionMatchStatus(
			link.getBankingRecordId(),
			"MATCH_CONFIRMED");
	}

	public void markReconciled(String bankingRecordId) throws SQLException
	{
		BankingTransactionRepository.markReconciled(bankingRecordId);
	}

	public void voidMatch(String bankingRecordId, String ledgerRecordId)
		throws SQLException
	{
		LedgerMatchRepository.setLinkStatus(ledgerRecordId, "VOIDED");
		List<LedgerMatchRecord> activeLinks =
			LedgerMatchRepository.findActiveByBankingRecordId(bankingRecordId);
		if (activeLinks.isEmpty())
		{
			BankingTransactionRepository.transitionMatchStatus(
				bankingRecordId,
				"UNMATCHED");
		}
	}
}
