package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BankStatementRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Persists imported SCLX bank statement records into a concrete staging table.
 */
@ApplicationScoped
public class BankStatementRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_bank_statement_record (
            import_id VARCHAR(255) PRIMARY KEY,
            source_format VARCHAR(64) NOT NULL,
            source_version VARCHAR(64),
            statement_kind VARCHAR(64) NOT NULL,
            bank_id VARCHAR(255),
            account_id VARCHAR(255) NOT NULL,
            account_type VARCHAR(64),
            currency VARCHAR(16),
            statement_start DATE NOT NULL,
            statement_end DATE NOT NULL,
            ledger_balance_amount DECIMAL(19,2),
            ledger_balance_as_of TIMESTAMP WITH TIME ZONE,
            available_balance_amount DECIMAL(19,2),
            available_balance_as_of TIMESTAMP WITH TIME ZONE,
            document_id VARCHAR(255),
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_bank_statement_record(
            import_id, source_format, source_version, statement_kind, bank_id, account_id, account_type,
            currency, statement_start, statement_end, ledger_balance_amount, ledger_balance_as_of,
            available_balance_amount, available_balance_as_of, document_id, extensions_json
        ) KEY(import_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    public void upsert(BankStatementRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.importId());
                ps.setString(++i, row.sourceFormat().name());
                ps.setString(++i, row.sourceVersion());
                ps.setString(++i, row.statementKind().name());
                ps.setString(++i, row.bankAccount().bankId());
                ps.setString(++i, row.bankAccount().accountId());
                ps.setString(++i, row.bankAccount().accountType());
                ps.setString(++i, row.currency());
                ps.setDate(++i, Date.valueOf(row.statementStart()));
                ps.setDate(++i, Date.valueOf(row.statementEnd()));
                ps.setBigDecimal(++i, row.ledgerBalance() == null ? null : row.ledgerBalance().amount());
                ps.setObject(++i, row.ledgerBalance() == null ? null : row.ledgerBalance().asOf());
                ps.setBigDecimal(++i, row.availableBalance() == null ? null : row.availableBalance().amount());
                ps.setObject(++i, row.availableBalance() == null ? null : row.availableBalance().asOf());
                ps.setString(++i, row.documentId());
                ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
                ps.executeUpdate();
            }
        }
    }

    private void ensureTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(CREATE_SQL))
        {
            ps.execute();
        }
    }
}
