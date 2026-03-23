package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BankingItemRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists imported SCLX banking items into a concrete staging table.
 */
@ApplicationScoped
public class BankingItemRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_banking_item_record (
            banking_item_id VARCHAR(255) PRIMARY KEY,
            kind VARCHAR(64) NOT NULL,
            bank_account_id VARCHAR(255),
            transaction_id VARCHAR(255),
            line_ids_json CLOB,
            cleared_date DATE NOT NULL,
            amount DECIMAL(19,2) NOT NULL,
            check_number VARCHAR(255),
            payee VARCHAR(500),
            deposit_date DATE,
            payer VARCHAR(500),
            deposit_id VARCHAR(255),
            memo CLOB,
            source VARCHAR(64),
            status VARCHAR(64),
            import_id VARCHAR(255),
            ofx_json CLOB,
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_banking_item_record(
            banking_item_id, kind, bank_account_id, transaction_id, line_ids_json, cleared_date, amount,
            check_number, payee, deposit_date, payer, deposit_id, memo, source, status, import_id, ofx_json, extensions_json
        ) KEY(banking_item_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    public void upsert(BankingItemRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.bankingItemId());
                ps.setString(++i, row.kind() == null ? null : row.kind().name());
                ps.setString(++i, row.bankAccountId());
                ps.setString(++i, row.transactionId());
                ps.setString(++i, JsonColumnCodec.toJson(row.lineIds()));
                ps.setDate(++i, Date.valueOf(row.clearedDate()));
                ps.setBigDecimal(++i, row.amount());
                ps.setString(++i, row.checkNumber());
                ps.setString(++i, row.payee());
                ps.setDate(++i, row.depositDate() == null ? null : Date.valueOf(row.depositDate()));
                ps.setString(++i, row.payer());
                ps.setString(++i, row.depositId());
                ps.setString(++i, row.memo());
                ps.setString(++i, row.source() == null ? null : row.source().name());
                ps.setString(++i, row.status() == null ? null : row.status().name());
                ps.setString(++i, row.importId());
                ps.setString(++i, JsonColumnCodec.toJson(row.ofx()));
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
