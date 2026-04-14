package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.LedgerMatchRecord;
import nonprofitbookkeeping.model.BankStatementRecord;
import nonprofitbookkeeping.model.BankIdentityRecord;
import nonprofitbookkeeping.persistence.BankingTransactionRepository;
import nonprofitbookkeeping.persistence.BankIdentityRepository;
import nonprofitbookkeeping.persistence.BankStatementRepository;
import nonprofitbookkeeping.persistence.LedgerMatchRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

	public int reconcileFromBookingTimestamps(String accountIdentifier,
		String statementDate, BigDecimal endingBalance,
		List<Long> bookingTimestamps) throws SQLException
	{
		Optional<BankIdentityRecord> bankIdentity =
			BankIdentityRepository.findByAccountOrRecordId(accountIdentifier);

		BankStatementRecord statement = new BankStatementRecord();
		statement.setBankName(bankIdentity.map(BankIdentityRecord::getBankName)
			.orElse(accountIdentifier));
		statement.setAccountLabel(bankIdentity.map(BankIdentityRecord::getAccountId)
			.orElse(accountIdentifier));
		statement.setBankIdRecordId(bankIdentity.map(BankIdentityRecord::getBankIdRecordId)
			.orElse(null));
		statement.setStatementDate(LocalDate.parse(statementDate));
		statement.setStatementBalance(endingBalance);
		statement.setStatus("CLOSED");
		statement.setClosedAt(LocalDateTime.now());
		BankStatementRepository.upsert(statement);
		return BankingTransactionRepository
			.markReconciledByBookingTimestamps(bookingTimestamps);
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
