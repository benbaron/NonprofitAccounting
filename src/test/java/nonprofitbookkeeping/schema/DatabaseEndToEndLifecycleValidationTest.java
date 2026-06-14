package nonprofitbookkeeping.schema;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.BankingTransactionRecord;
import nonprofitbookkeeping.model.DonationRecord;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.model.LedgerMatchRecord;
import nonprofitbookkeeping.service.DefaultPostingFacade;
import nonprofitbookkeeping.service.DonationPostingService;
import nonprofitbookkeeping.service.FundTransferPostingService;
import nonprofitbookkeeping.service.GrantsFinancePostingService;
import nonprofitbookkeeping.service.OperationalReconciliationService;
import nonprofitbookkeeping.service.PostingCommand;
import nonprofitbookkeeping.service.PostingReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseEndToEndLifecycleValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void flywayEnsureSchemaPostingBackfillViewsAndReadModelsWorkEndToEnd() throws Exception
    {
        Database.init(tempDir.resolve("database-end-to-end-lifecycle"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();

        seedChartAccountsFundsDonorAndBankIdentity();
        seedAssetAndDepreciationRun();

        PostingReference manualPosting = postManualJournalTransaction();
        PostingReference donationPosting = postDonation();
        PostingReference fundTransferPosting = postFundTransfer();
        PostingReference grantPosting = postGrantAward();
        PostingReference bankAdjustmentPosting = postBankAdjustment();
        reconcileOperationalBankingFlow(manualPosting);

        Database.get().ensureSchema();
        new org.nonprofitbookkeeping.service.ReadModelMaintenanceService().rebuildAll();

        assertPostedTransactionIsVisibleEverywhere(manualPosting.journalTxnId());
        assertPostedTransactionIsVisibleEverywhere(donationPosting.journalTxnId());
        assertPostedTransactionIsVisibleEverywhere(fundTransferPosting.journalTxnId());
        assertPostedTransactionIsVisibleEverywhere(grantPosting.journalTxnId());
        assertNoUnbalancedCanonicalTransactions();
        assertLegacyMapCoversEveryPostedJournalTransaction();
        assertReadModelsArePopulatedAndBalanced();
        assertDomainLinksSurvivedTheFullLifecycle(fundTransferPosting, grantPosting);
        assertOperationalBankingFlowSurvivedTheFullLifecycle(bankAdjustmentPosting);
    }

    private PostingReference postManualJournalTransaction() throws Exception
    {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(1000);
        txn.setDate(LocalDate.of(2026, 6, 1).toString());
        txn.setMemo("manual lifecycle posting");
        txn.setInfo(Map.of("module", "E2E", "domain_record_id", "manual-1"));
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        AccountingEntry debit = new AccountingEntry(new BigDecimal("25.00"), "1000", AccountSide.DEBIT, "Cash");
        debit.setFundNumber("F1");
        entries.add(debit);
        AccountingEntry credit = new AccountingEntry(new BigDecimal("25.00"), "4000", AccountSide.CREDIT, "Revenue");
        credit.setFundNumber("F1");
        entries.add(credit);
        txn.setEntries(entries);
        return new DefaultPostingFacade().post(new PostingCommand(txn, "E2E", "manual-1", "ORIGINAL", "E2E:manual-1"));
    }

    private PostingReference postDonation() throws Exception
    {
        DonationPostingService service = new DonationPostingService();
        DonationRecord donation = new DonationRecord();
        donation.setDonationId("don-e2e-1");
        donation.setDonorExternalId("donor-e2e");
        donation.setDonationDate(LocalDate.of(2026, 6, 2));
        donation.setAmount(new BigDecimal("125.00"));
        donation.setMemo("database lifecycle donation");
        donation.setCashAccountNumber("1000");
        donation.setRevenueAccountNumber("4000");
        donation.setFundNumber("F1");
        DonationRecord posted = service.postDonation(donation);
        return new PostingReference(posted.getJournalTxnId(), "txn:" + posted.getJournalTxnId());
    }

    private PostingReference postFundTransfer() throws Exception
    {
        long transferId = insertFundTransfer("APPROVED");
        return new FundTransferPostingService().postTransfer(
            transferId,
            LocalDate.of(2026, 6, 3),
            "F1",
            "F2",
            new BigDecimal("50.00"),
            "database lifecycle fund transfer",
            "1000",
            "2100");
    }

    private PostingReference postGrantAward() throws Exception
    {
        seedGrant("GR-E2E-1");
        Grant grant = new Grant("G-E2E", "Grantor", new BigDecimal("200.00"),
            "2026-06-04", "database lifecycle grant", "ACTIVE");
        return new GrantsFinancePostingService().postFinancialEvent(
            "GR-E2E-1",
            grant,
            GrantsFinancePostingService.GrantEventType.AWARD,
            LocalDate.of(2026, 6, 4),
            new BigDecimal("200.00"),
            "1000",
            "4000",
            "F1");
    }

    private PostingReference postBankAdjustment() throws Exception
    {
        seedBankingTransaction("BANK-E2E-ADJ", null, new BigDecimal("15.75"), "NEW");
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(1004);
        txn.setDate(LocalDate.of(2026, 6, 5).toString());
        txn.setBookingDateTimestamp(1781450004000L);
        txn.setMemo("database lifecycle bank adjustment");
        txn.setInfo(Map.of("module", "BANKING", "domain_record_id", "BANK-E2E-ADJ"));
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        AccountingEntry debit = new AccountingEntry(new BigDecimal("15.75"), "1000", AccountSide.DEBIT, "Cash");
        debit.setFundNumber("F1");
        entries.add(debit);
        AccountingEntry credit = new AccountingEntry(new BigDecimal("15.75"), "4000", AccountSide.CREDIT, "Revenue");
        credit.setFundNumber("F1");
        entries.add(credit);
        txn.setEntries(entries);
        return new OperationalReconciliationService().postAdjustment("BANK-E2E-ADJ",
            new PostingCommand(txn, "BANKING", "BANK-E2E-ADJ", "ORIGINAL", "BANKING:BANK-E2E-ADJ"));
    }

    private void reconcileOperationalBankingFlow(PostingReference manualPosting) throws Exception
    {
        seedBankingTransaction("BANK-E2E-MATCH", manualPosting.journalTxnId(), new BigDecimal("25.00"), "AUTO_MATCHED");
        LedgerMatchRecord match = new LedgerMatchRecord();
        match.setLedgerRecordId("LEDGER-E2E-MATCH");
        match.setLedgerId("PRIMARY_LEDGER");
        match.setBankIdRecordId("BANK-ID-E2E");
        match.setBankingRecordId("BANK-E2E-MATCH");
        match.setMatchGroupId("MATCH-GROUP-E2E");
        match.setMatchMethod("AUTO");
        match.setReviewerUser("e2e-test");
        new OperationalReconciliationService().confirmMatch(match);
        new OperationalReconciliationService().reconcileFromBookingTimestamps("OPERATING-1000",
            "2026-06-30", new BigDecimal("25.00"), java.util.List.of(0L));
    }

    private void assertPostedTransactionIsVisibleEverywhere(int txnId) throws Exception
    {
        assertEquals(1, count("SELECT COUNT(*) FROM journal_transaction WHERE id = ?", txnId));
        assertEquals(1, count("SELECT COUNT(*) FROM txn WHERE id = ?", txnId));
        assertTrue(count("SELECT COUNT(*) FROM journal_entry WHERE txn_id = ?", txnId) >= 2);
        assertTrue(count("SELECT COUNT(*) FROM txn_split WHERE txn_id = ?", txnId) >= 2);
        assertEquals(1, count("SELECT COUNT(*) FROM v_journal_transaction WHERE id = ?", txnId));
        assertTrue(count("SELECT COUNT(*) FROM v_journal_entry WHERE txn_id = ?", txnId) >= 2);
        assertEquals(1, count("SELECT COUNT(*) FROM legacy_txn_map WHERE legacy_txn_id = ?", txnId));
    }

    private void assertNoUnbalancedCanonicalTransactions() throws Exception
    {
        assertEquals(0, count("""
            SELECT COUNT(*)
            FROM (
              SELECT txn_id
              FROM txn_split
              GROUP BY txn_id
              HAVING ABS(COALESCE(SUM(amount_signed), 0)) > 0.001
            ) unbalanced
            """));
    }

    private void assertLegacyMapCoversEveryPostedJournalTransaction() throws Exception
    {
        assertEquals(0, count("""
            SELECT COUNT(*)
            FROM journal_transaction jt
            JOIN txn t ON t.id = jt.id
            LEFT JOIN legacy_txn_map m ON m.legacy_txn_id = jt.id
            WHERE m.legacy_txn_id IS NULL
            """));
    }

    private void assertReadModelsArePopulatedAndBalanced() throws Exception
    {
        assertTrue(count("SELECT COUNT(*) FROM rm_donation_summary") >= 1);
        assertTrue(count("SELECT COUNT(*) FROM rm_fund_summary") >= 1);
        assertTrue(count("SELECT COUNT(*) FROM rm_reconciliation_summary") >= 1);
        assertEquals(1, count("SELECT COUNT(*) FROM rm_depreciation_summary WHERE depreciation_run_id = 'DEP-E2E-1'"));
        Map<String, BigDecimal> drift = new org.nonprofitbookkeeping.service.ReadModelMaintenanceService().detectDrift();
        for (Map.Entry<String, BigDecimal> entry : drift.entrySet())
        {
            assertEquals(0, entry.getValue().compareTo(BigDecimal.ZERO), "read-model drift for " + entry.getKey());
        }
    }

    private void assertOperationalBankingFlowSurvivedTheFullLifecycle(PostingReference bankAdjustmentPosting) throws Exception
    {
        assertEquals(1, count("SELECT COUNT(*) FROM banking_transaction_record WHERE banking_record_id = 'BANK-E2E-ADJ' AND journal_txn_id = ? AND match_status = 'ADJUSTED'",
            bankAdjustmentPosting.journalTxnId()));
        assertEquals(1, count("SELECT COUNT(*) FROM banking_transaction_record WHERE banking_record_id = 'BANK-E2E-MATCH' AND match_status = 'RECONCILED'"));
        assertEquals(1, count("SELECT COUNT(*) FROM ledger_record WHERE ledger_record_id = 'LEDGER-E2E-MATCH' AND link_status = 'ACTIVE'"));
        assertEquals(1, count("SELECT COUNT(*) FROM bank_statement WHERE bank_id_record_id = 'BANK-ID-E2E' AND statement_date = DATE '2026-06-30' AND status = 'CLOSED'"));
    }

    private void assertDomainLinksSurvivedTheFullLifecycle(PostingReference fundTransferPosting,
        PostingReference grantPosting) throws Exception
    {
        assertEquals(1, count("SELECT COUNT(*) FROM fund_transfer WHERE posted_txn_id = ? AND status = 'POSTED'",
            fundTransferPosting.canonicalTxnId().intValue()));
        assertEquals(1, count("SELECT COUNT(*) FROM donation_journal_link WHERE donation_id = 'don-e2e-1'"));
        assertEquals(1, count("SELECT COUNT(*) FROM grant_record WHERE grant_record_id = 'GR-E2E-1' AND canonical_txn_id = ?",
            grantPosting.canonicalTxnId().intValue()));
        assertTrue(count("SELECT COUNT(*) FROM grant_posting_link WHERE grant_record_id = 'GR-E2E-1'") >= 1);
    }

    private void seedChartAccountsFundsDonorAndBankIdentity() throws Exception
    {
        try (Connection c = Database.get().getConnection())
        {
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO chart_of_accounts(id, name, version, status) KEY(id)
                VALUES (1, 'Default', 'E2E', 'ACTIVE')
                """))
            {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO account(account_number, name, account_type, increase_side, code, normal_balance, chart_id, is_posting, is_active)
                KEY(account_number) VALUES (?,?,?,?,?,?,?,?,?)
                """))
            {
                addAccount(ps, "1000", "Cash", "ASSET", "DEBIT");
                addAccount(ps, "2100", "Due To From Funds", "LIABILITY", "CREDIT");
                addAccount(ps, "4000", "Revenue", "INCOME", "CREDIT");
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO fund(code, name, fund_type, is_active) KEY(code) VALUES (?, ?, ?, TRUE)
                """))
            {
                ps.setString(1, "F1");
                ps.setString(2, "Fund 1");
                ps.setString(3, "RESTRICTED");
                ps.addBatch();
                ps.setString(1, "F2");
                ps.setString(2, "Fund 2");
                ps.setString(3, "RESTRICTED");
                ps.addBatch();
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO donor(external_id, name, email, phone) KEY(external_id) VALUES (?, ?, ?, ?)
                """))
            {
                ps.setString(1, "donor-e2e");
                ps.setString(2, "Donor E2E");
                ps.setString(3, "donor-e2e@example.org");
                ps.setString(4, "555-0100");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO bank_id_record(bank_id_record_id, bank_id, bank_name, account_id, account_type)
                KEY(bank_id_record_id) VALUES ('BANK-ID-E2E', 'ROUTING-E2E', 'Operating Bank', 'OPERATING-1000', 'CHECKING')
                """))
            {
                ps.executeUpdate();
            }
        }
    }

    private void addAccount(PreparedStatement ps, String number, String name, String type, String normalBalance)
        throws Exception
    {
        ps.setString(1, number);
        ps.setString(2, name);
        ps.setString(3, type);
        ps.setString(4, normalBalance);
        ps.setString(5, number);
        ps.setString(6, normalBalance);
        ps.setLong(7, 1L);
        ps.setBoolean(8, true);
        ps.setBoolean(9, true);
        ps.addBatch();
    }

    private void seedAssetAndDepreciationRun() throws Exception
    {
        try (Connection c = Database.get().getConnection())
        {
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO asset_record_detail(asset_record_id, asset_type, depreciation_method, date_acquired, asset_state,
                  in_service_date, depreciable_basis, salvage_value, useful_life_months)
                KEY(asset_record_id) VALUES ('ASSET-E2E-1', 'Equipment', 'STRAIGHT_LINE', DATE '2026-01-01', 'ACTIVE',
                  DATE '2026-01-01', 1200.00, 0.00, 60)
                """))
            {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO depreciation_run(depreciation_run_id, run_date, notes, period_start, period_end, run_status)
                KEY(depreciation_run_id) VALUES ('DEP-E2E-1', DATE '2026-06-30', 'database lifecycle depreciation',
                  DATE '2026-06-01', DATE '2026-06-30', 'CALCULATED')
                """))
            {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO depreciation_record(depreciation_record_id, depreciation_run_id, asset_record_id, net_depreciation,
                  depreciation_date, period_start, period_end, sequence_in_run)
                KEY(depreciation_record_id) VALUES ('DREC-E2E-1', 'DEP-E2E-1', 'ASSET-E2E-1', 12.34,
                  DATE '2026-06-30', DATE '2026-06-01', DATE '2026-06-30', 1)
                """))
            {
                ps.executeUpdate();
            }
        }
    }

    private void seedGrant(String grantRecordId) throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 MERGE INTO grant_record(grant_record_id, grant_id, grantor, amount, date_awarded_text, purpose, status, details)
                 KEY(grant_record_id) VALUES (?, 'G-E2E', 'Grantor', 200.00, '2026-06-04', 'database lifecycle grant', 'ACTIVE', '{}')
                 """))
        {
            ps.setString(1, grantRecordId);
            ps.executeUpdate();
        }
    }

    private void seedBankingTransaction(String bankingRecordId, Integer journalTxnId, BigDecimal amount, String matchStatus)
        throws Exception
    {
        BankingTransactionRecord record = new BankingTransactionRecord();
        record.setBankingRecordId(bankingRecordId);
        record.setBankIdRecordId("BANK-ID-E2E");
        record.setJournalTxnId(journalTxnId);
        record.setFundId(fundId("F1"));
        record.setTransactionDate(LocalDate.of(2026, 6, 5));
        record.setExternalTransactionId(bankingRecordId + "-EXT");
        record.setSourceFingerprint(bankingRecordId + "-FP");
        record.setNormalizedDescription("database lifecycle banking " + bankingRecordId);
        record.setAmount(amount);
        record.setMatchStatus(matchStatus);
        nonprofitbookkeeping.persistence.BankingTransactionRepository.upsert(record);
    }

    private long insertFundTransfer(String status) throws Exception
    {
        long fromFundId = fundId("F1");
        long toFundId = fundId("F2");
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO fund_transfer(transfer_date, from_fund_id, to_fund_id, amount, memo, status)
                 VALUES (DATE '2026-06-03', ?, ?, 50.00, 'database lifecycle transfer', ?)
                 """, new String[] { "id" }))
        {
            ps.setLong(1, fromFundId);
            ps.setLong(2, toFundId);
            ps.setString(3, status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys())
            {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long fundId(String code) throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM fund WHERE code = ?"))
        {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private int count(String sql, int value) throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int count(String sql) throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            rs.next();
            return rs.getInt(1);
        }
    }
}
