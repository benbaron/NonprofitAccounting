package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.BankStatementRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported bank statement records as staged impex rows.
 */
public class BankStatementRecordRepository
{
    private static final String DDL = """
        CREATE TABLE IF NOT EXISTS imported_bank_statement (
            import_id VARCHAR(255) PRIMARY KEY,
            source_format VARCHAR(64),
            source_version VARCHAR(128),
            statement_kind VARCHAR(64),
            bank_id VARCHAR(255),
            account_id VARCHAR(255),
            account_type VARCHAR(128),
            currency VARCHAR(32),
            statement_start DATE,
            statement_end DATE,
            ledger_balance DECIMAL(19,2),
            ledger_balance_asof TIMESTAMP,
            available_balance DECIMAL(19,2),
            available_balance_asof TIMESTAMP,
            document_id VARCHAR(255)
        )
        """;

    private static final String UPSERT = """
        MERGE INTO imported_bank_statement(
            import_id, source_format, source_version, statement_kind,
            bank_id, account_id, account_type, currency,
            statement_start, statement_end,
            ledger_balance, ledger_balance_asof,
            available_balance, available_balance_asof,
            document_id
        ) KEY(import_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL = """
        SELECT
            import_id, source_format, source_version, statement_kind,
            bank_id, account_id, account_type, currency,
            statement_start, statement_end,
            ledger_balance, ledger_balance_asof,
            available_balance, available_balance_asof,
            document_id
        FROM imported_bank_statement
        """;

    public void upsert(BankStatementRecord record) throws SQLException
    {
        ensureSchema();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(UPSERT))
        {
            int i = 0;
            ps.setString(++i, record.importId());
            ps.setString(++i, record.sourceFormat() == null ? null : record.sourceFormat().name());
            ps.setString(++i, record.sourceVersion());
            ps.setString(++i, record.statementKind() == null ? null : record.statementKind().name());
            ps.setString(++i, record.bankAccount() == null ? null : record.bankAccount().bankId());
            ps.setString(++i, record.bankAccount() == null ? null : record.bankAccount().accountId());
            ps.setString(++i, record.bankAccount() == null ? null : record.bankAccount().accountType());
            ps.setString(++i, record.currency());
            ps.setDate(++i, record.statementStart() == null ? null : java.sql.Date.valueOf(record.statementStart()));
            ps.setDate(++i, record.statementEnd() == null ? null : java.sql.Date.valueOf(record.statementEnd()));
            ps.setBigDecimal(++i, record.ledgerBalance() == null ? null : record.ledgerBalance().amount());
            ps.setTimestamp(++i, record.ledgerBalance() == null || record.ledgerBalance().asOf() == null ? null : Timestamp.from(record.ledgerBalance().asOf().toInstant()));
            ps.setBigDecimal(++i, record.availableBalance() == null ? null : record.availableBalance().amount());
            ps.setTimestamp(++i, record.availableBalance() == null || record.availableBalance().asOf() == null ? null : Timestamp.from(record.availableBalance().asOf().toInstant()));
            ps.setString(++i, record.documentId());
            ps.executeUpdate();
        }
    }

    public List<BankStatementRecord> listAll() throws SQLException
    {
        ensureSchema();
        List<BankStatementRecord> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(LIST_ALL);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                String sourceFormat = rs.getString("source_format");
                String statementKind = rs.getString("statement_kind");
                Timestamp ledgerAsOf = rs.getTimestamp("ledger_balance_asof");
                Timestamp availableAsOf = rs.getTimestamp("available_balance_asof");
                var bankAccount = new BankStatementRecord.BankAccountRef(
                    rs.getString("bank_id"),
                    rs.getString("account_id"),
                    rs.getString("account_type")
                );
                var ledger = rs.getBigDecimal("ledger_balance") == null && ledgerAsOf == null
                    ? null
                    : new BankStatementRecord.BalanceSnapshot(
                    rs.getBigDecimal("ledger_balance"),
                    ledgerAsOf == null ? null : ledgerAsOf.toInstant().atOffset(ZoneOffset.UTC)
                );
                var available = rs.getBigDecimal("available_balance") == null && availableAsOf == null
                    ? null
                    : new BankStatementRecord.BalanceSnapshot(
                    rs.getBigDecimal("available_balance"),
                    availableAsOf == null ? null : availableAsOf.toInstant().atOffset(ZoneOffset.UTC)
                );

                rows.add(new BankStatementRecord(
                    rs.getString("import_id"),
                    sourceFormat == null ? null : BankStatementRecord.SourceFormat.valueOf(sourceFormat),
                    rs.getString("source_version"),
                    statementKind == null ? null : BankStatementRecord.StatementKind.valueOf(statementKind),
                    bankAccount.bankId() == null && bankAccount.accountId() == null && bankAccount.accountType() == null ? null : bankAccount,
                    rs.getString("currency"),
                    rs.getDate("statement_start") == null ? null : rs.getDate("statement_start").toLocalDate(),
                    rs.getDate("statement_end") == null ? null : rs.getDate("statement_end").toLocalDate(),
                    ledger,
                    available,
                    rs.getString("document_id"),
                    java.util.Map.of(),
                    null
                ));
            }
        }
        return rows;
    }

    private void ensureSchema() throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             Statement st = c.createStatement())
        {
            st.execute(DDL);
        }
    }
}
