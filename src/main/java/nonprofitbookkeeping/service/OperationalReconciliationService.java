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
	private final PostingFacade postingFacade;

	public OperationalReconciliationService()
	{
		this(new DefaultPostingFacade());
	}

	OperationalReconciliationService(PostingFacade postingFacade)
	{
		this.postingFacade = postingFacade;
	}

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

	public PostingReference postAdjustment(String bankingRecordId,
		PostingCommand command) throws SQLException
	{
		if (bankingRecordId == null || bankingRecordId.isBlank())
		{
			throw new IllegalArgumentException("bankingRecordId is required");
		}
		enforceStatementUnlocked(bankingRecordId);
		PostingReference ref = this.postingFacade.post(command);
		try (var c = nonprofitbookkeeping.core.Database.get().getConnection();
			 var ps = c.prepareStatement("""
				UPDATE banking_transaction_record
				   SET journal_txn_id = ?,
				       canonical_txn_id = ?,
				       match_status = 'ADJUSTED',
				       matched_at = CURRENT_TIMESTAMP
				 WHERE banking_record_id = ?
				"""))
		{
			ps.setInt(1, ref.journalTxnId());
			Long canonicalTxnId = ref.canonicalTxnId();
			if (canonicalTxnId == null)
			{
				ps.setNull(2, java.sql.Types.BIGINT);
			}
			else
			{
				ps.setLong(2, canonicalTxnId);
			}
			ps.setString(3, bankingRecordId);
			ps.executeUpdate();
		}
		return ref;
	}

	private void enforceStatementUnlocked(String bankingRecordId)
		throws SQLException
	{
		try (var c = nonprofitbookkeeping.core.Database.get().getConnection();
			 var ps = c.prepareStatement("""
				SELECT bs.status
				FROM banking_transaction_record btr
				LEFT JOIN bank_statement bs ON bs.id = btr.statement_id
				WHERE btr.banking_record_id = ?
				"""))
		{
			ps.setString(1, bankingRecordId);
			try (var rs = ps.executeQuery())
			{
				if (rs.next() && "LOCKED".equalsIgnoreCase(rs.getString(1)))
				{
					throw new IllegalStateException(
						"Cannot modify transaction in locked statement period");
				}
			}
		}
	}
}
