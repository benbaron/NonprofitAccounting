package nonprofitbookkeeping.persistence.impex;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BankStatementRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

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

    private void ensureSchema() throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             Statement st = c.createStatement())
        {
            st.execute(DDL);
        }
    }
}
