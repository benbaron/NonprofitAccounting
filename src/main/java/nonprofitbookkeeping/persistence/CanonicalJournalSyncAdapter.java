package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Synchronizes legacy journal rows with canonical JPA txn / txn_split tables.
 */
final class CanonicalJournalSyncAdapter
{
    private static final String DOMAIN_ACCOUNT = "ACCOUNT";
    private static final String DOMAIN_FUND = "FUND";

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.BASIC_ISO_DATE,
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M-d-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy")
    );

    private CanonicalJournalSyncAdapter() {}

    static void syncTransaction(Connection c, AccountingTransaction txn) throws SQLException
    {
        if (txn == null)
        {
            return;
        }

        upsertTxn(c, txn);
        replaceSplits(c, txn);
    }

    static void deleteLegacyTxnIds(Connection c, Collection<Integer> txnIds) throws SQLException
    {
        if (txnIds == null || txnIds.isEmpty())
        {
            return;
        }

        try (PreparedStatement delSplits = c.prepareStatement("DELETE FROM txn_split WHERE txn_id = ?");
             PreparedStatement delTxn = c.prepareStatement("DELETE FROM txn WHERE id = ?"))
        {
            for (Integer id : txnIds)
            {
                if (id == null)
                {
                    continue;
                }
                delSplits.setLong(1, id);
                delSplits.addBatch();
                delTxn.setLong(1, id);
                delTxn.addBatch();
            }
            delSplits.executeBatch();
            delTxn.executeBatch();
        }
    }

    static List<Integer> listLegacyTxnIds(Connection c) throws SQLException
    {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM journal_transaction");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private static void upsertTxn(Connection c, AccountingTransaction txn) throws SQLException
    {
        LocalDate date = parseDate(txn.getDate());
        if (date == null)
        {
            date = LocalDate.now();
        }

        Long payeeId = resolveCounterpartyId(c, txn.getToFrom());
        Long bankAccountId = resolveAccountId(c, txn.getClearBank(), null);

        try (PreparedStatement ps = c.prepareStatement(
            "MERGE INTO txn(id, txn_date, payee_id, memo, bank_account_id, created_at, updated_at) KEY(id) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"))
        {
            ps.setInt(1, txn.getId());
            ps.setDate(2, Date.valueOf(date));
            if (payeeId == null)
            {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            else
            {
                ps.setLong(3, payeeId);
            }
            ps.setString(4, txn.getMemo());
            if (bankAccountId == null)
            {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            else
            {
                ps.setLong(5, bankAccountId);
            }
            ps.executeUpdate();
        }
    }

    private static void replaceSplits(Connection c, AccountingTransaction txn) throws SQLException
    {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM txn_split WHERE txn_id = ?"))
        {
            del.setInt(1, txn.getId());
            del.executeUpdate();
        }

        if (txn.getEntries() == null || txn.getEntries().isEmpty())
        {
            return;
        }

        Map<String, Long> fundCache = new HashMap<>();
        Map<String, Long> accountCache = new HashMap<>();
        Long fallbackFundId = resolveFundId(c, txn.getAssociatedFundName(), fundCache);

        try (PreparedStatement ins = c.prepareStatement(
            "INSERT INTO txn_split(txn_id, account_id, fund_id, amount_signed, notes, nmr_flag) VALUES (?, ?, ?, ?, ?, FALSE)"))
        {
            for (AccountingEntry entry : txn.getEntries())
            {
                if (entry == null || entry.getAccountNumber() == null || entry.getAmount() == null)
                {
                    continue;
                }
                Long accountId = resolveAccountId(c, entry.getAccountNumber(), accountCache);
                if (accountId == null)
                {
                    continue;
                }
                Long fundId = resolveFundId(c, entry.getFundNumber(), fundCache);
                if (fundId == null)
                {
                    fundId = fallbackFundId;
                }
                if (fundId == null)
                {
                    fundId = 1L;
                }
                ins.setInt(1, txn.getId());
                ins.setLong(2, accountId);
                ins.setLong(3, fundId);
                ins.setBigDecimal(4, signedAmount(entry));
                ins.setString(5, entry.getAccountName());
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private static Long resolveFundId(Connection c, String token, Map<String, Long> cache)
        throws SQLException
    {
        String key = token == null ? "" : token.trim();
        if (key.isEmpty())
        {
            return null;
        }
        if (cache.containsKey(key))
        {
            return cache.get(key);
        }

        String normalized = normalizeToken(key);
        Set<Long> candidates = new LinkedHashSet<>();
        candidates.addAll(queryIds(c, "SELECT id FROM fund WHERE is_active = TRUE AND UPPER(code) = UPPER(?)", key));
        if (candidates.size() == 1)
        {
            Long resolved = candidates.iterator().next();
            cache.put(key, resolved);
            return resolved;
        }
        candidates.addAll(queryIds(c, "SELECT id FROM fund WHERE is_active = TRUE AND UPPER(name) = UPPER(?)", key));
        if (candidates.size() == 1)
        {
            Long resolved = candidates.iterator().next();
            cache.put(key, resolved);
            return resolved;
        }
        candidates.addAll(queryIds(c,
            "SELECT f.id FROM fund_alias fa JOIN fund f ON f.id = fa.fund_id WHERE fa.is_active = TRUE AND f.is_active = TRUE AND UPPER(fa.alias_text) = UPPER(?)",
            key));
        if (candidates.size() == 1)
        {
            Long resolved = candidates.iterator().next();
            cache.put(key, resolved);
            return resolved;
        }
        candidates.addAll(queryIds(c,
            "SELECT id FROM fund WHERE is_active = TRUE AND REPLACE(REPLACE(UPPER(code), ' ', ''), '-', '') = ?",
            normalized));
        if (candidates.size() == 1)
        {
            Long resolved = candidates.iterator().next();
            cache.put(key, resolved);
            return resolved;
        }
        candidates.addAll(queryIds(c,
            "SELECT id FROM fund WHERE is_active = TRUE AND REPLACE(REPLACE(UPPER(name), ' ', ''), '-', '') = ?",
            normalized));
        if (candidates.size() > 1)
        {
            enqueueAliasReview(c, DOMAIN_FUND, token, normalized, candidates, "AMBIGUOUS_IMPORT_MATCH");
        }

        Long resolved = candidates.size() == 1 ? candidates.iterator().next() : null;
        cache.put(key, resolved);
        return resolved;
    }

    private static Long resolveCounterpartyId(Connection c, String displayName)
        throws SQLException
    {
        if (displayName == null || displayName.isBlank())
        {
            return null;
        }
        String key = displayName.trim();
        String normalized = normalizeToken(key);
        try (PreparedStatement ps = c.prepareStatement(
            "SELECT id FROM counterparty WHERE UPPER(display_name) = UPPER(?) ORDER BY CASE kind WHEN 'DONOR' THEN 0 WHEN 'PERSON' THEN 1 ELSE 2 END, id FETCH FIRST 1 ROWS ONLY");
             PreparedStatement psNormalized = c.prepareStatement(
                 "SELECT id FROM counterparty WHERE REPLACE(REPLACE(UPPER(display_name), ' ', ''), '-', '') = ? ORDER BY CASE kind WHEN 'DONOR' THEN 0 WHEN 'PERSON' THEN 1 ELSE 2 END, id FETCH FIRST 1 ROWS ONLY"))
        {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getLong(1);
                }
            }
            psNormalized.setString(1, normalized);
            try (ResultSet rs = psNormalized.executeQuery())
            {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static Long resolveAccountId(Connection c, String token, Map<String, Long> cache)
        throws SQLException
    {
        if (token == null || token.isBlank())
        {
            return null;
        }

        String value = token.trim();
        if (cache != null && cache.containsKey(value))
        {
            return cache.get(value);
        }
        String normalized = normalizeToken(value);
        Set<Long> candidates = new LinkedHashSet<>();
        candidates.addAll(queryIds(c, "SELECT id FROM account WHERE is_active = TRUE AND UPPER(account_number) = UPPER(?)", value));
        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }
        candidates.addAll(queryIds(c, "SELECT id FROM account WHERE is_active = TRUE AND UPPER(code) = UPPER(?)", value));
        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }
        candidates.addAll(queryIds(c, "SELECT id FROM account WHERE is_active = TRUE AND UPPER(name) = UPPER(?)", value));
        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }
        candidates.addAll(queryIds(c,
            "SELECT a.id FROM account_alias aa JOIN account a ON a.id = aa.account_id WHERE aa.is_active = TRUE AND a.is_active = TRUE AND UPPER(aa.alias_text) = UPPER(?)",
            value));
        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }
        candidates.addAll(queryIds(c,
            "SELECT id FROM account WHERE is_active = TRUE AND REPLACE(REPLACE(UPPER(account_number), ' ', ''), '-', '') = ?",
            normalized));
        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }
        candidates.addAll(queryIds(c,
            "SELECT id FROM account WHERE is_active = TRUE AND REPLACE(REPLACE(UPPER(code), ' ', ''), '-', '') = ?",
            normalized));
        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }
        candidates.addAll(queryIds(c,
            "SELECT id FROM account WHERE is_active = TRUE AND REPLACE(REPLACE(UPPER(name), ' ', ''), '-', '') = ?",
            normalized));
        if (candidates.size() > 1)
        {
            enqueueAliasReview(c, DOMAIN_ACCOUNT, token, normalized, candidates, "AMBIGUOUS_IMPORT_MATCH");
        }
        Long resolved = candidates.size() == 1 ? candidates.iterator().next() : null;
        if (cache != null)
        {
            cache.put(value, resolved);
        }
        return resolved;
    }

    private static List<Long> queryIds(Connection c, String sql, String value) throws SQLException
    {
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    ids.add(rs.getLong(1));
                }
            }
        }
        return ids;
    }

    private static void enqueueAliasReview(Connection c,
                                           String domain,
                                           String aliasText,
                                           String normalizationKey,
                                           Set<Long> candidateIds,
                                           String reason)
    {
        String ids = candidateIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        try (PreparedStatement ps = c.prepareStatement(
            "INSERT INTO alias_review_queue(alias_domain, alias_id, alias_text, normalization_key, candidate_count, candidate_ids, reason, status) " +
                "SELECT ?, NULL, ?, ?, ?, ?, ?, 'OPEN' WHERE NOT EXISTS (" +
                "SELECT 1 FROM alias_review_queue q WHERE q.alias_domain = ? AND q.normalization_key = ? AND q.reason = ? AND q.status IN ('OPEN','IN_PROGRESS'))"))
        {
            ps.setString(1, domain);
            ps.setString(2, aliasText);
            ps.setString(3, normalizationKey);
            ps.setInt(4, candidateIds.size());
            ps.setString(5, ids);
            ps.setString(6, reason);
            ps.setString(7, domain);
            ps.setString(8, normalizationKey);
            ps.setString(9, reason);
            ps.executeUpdate();
        }
        catch (SQLException ignored)
        {
            // Queue table may not be present before harmonization migration is applied.
        }
    }

    private static String normalizeToken(String value)
    {
        return value == null ? "" : value.toUpperCase().replace(" ", "").replace("-", "");
    }

    private static BigDecimal signedAmount(AccountingEntry entry)
    {
        BigDecimal amount = entry.getAmount();
        if (entry.getAccountSide() == AccountSide.CREDIT)
        {
            return amount.negate();
        }
        return amount;
    }

    private static LocalDate parseDate(String raw)
    {
        if (raw == null || raw.isBlank())
        {
            return null;
        }

        String value = raw.trim();
        for (DateTimeFormatter formatter : DATE_FORMATS)
        {
            try
            {
                return LocalDate.parse(value, formatter);
            }
            catch (DateTimeParseException ignored)
            {
                // try next format
            }
        }
        return null;
    }
}
