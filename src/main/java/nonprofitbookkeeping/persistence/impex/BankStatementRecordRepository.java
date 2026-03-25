package nonprofitbookkeeping.persistence.impex;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BankStatementRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BankStatementRecordRepository
{
    public BankStatementRecordRepository()
    {
        try
        {
            ensureSchema();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to initialize bank_statement_import_record table", ex);
        }
    }

    public void upsert(BankStatementRecord row) throws SQLException
    {
        String sql =
            "MERGE INTO bank_statement_import_record(" +
            "import_id, source_format, source_version, statement_kind, " +
            "bank_id, account_id, account_type, currency, statement_start, statement_end, " +
            "ledger_balance_amount, ledger_balance_as_of, available_balance_amount, available_balance_as_of, " +
            "document_id, raw_json) KEY(import_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            int i = 0;
            ps.setString(++i, row.importId());
            ps.setString(++i, row.sourceFormat() == null ? null : row.sourceFormat().name());
            ps.setString(++i, row.sourceVersion());
            ps.setString(++i, row.statementKind() == null ? null : row.statementKind().name());
            ps.setString(++i, row.bankAccount() == null ? null : row.bankAccount().bankId());
            ps.setString(++i, row.bankAccount() == null ? null : row.bankAccount().accountId());
            ps.setString(++i, row.bankAccount() == null ? null : row.bankAccount().accountType());
            ps.setString(++i, row.currency());
            ps.setDate(++i, row.statementStart() == null ? null : Date.valueOf(row.statementStart()));
            ps.setDate(++i, row.statementEnd() == null ? null : Date.valueOf(row.statementEnd()));
            ps.setBigDecimal(++i, row.ledgerBalance() == null ? null : row.ledgerBalance().amount());
            ps.setTimestamp(++i, row.ledgerBalance() == null || row.ledgerBalance().asOf() == null ? null : Timestamp.from(row.ledgerBalance().asOf().toInstant()));
            ps.setBigDecimal(++i, row.availableBalance() == null ? null : row.availableBalance().amount());
            ps.setTimestamp(++i, row.availableBalance() == null || row.availableBalance().asOf() == null ? null : Timestamp.from(row.availableBalance().asOf().toInstant()));
            ps.setString(++i, row.documentId());
            ps.setString(++i, row.rawJson());
            ps.executeUpdate();
        }
    }

    public List<BankStatementRecord> listAll() throws SQLException
    {
        List<BankStatementRecord> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM bank_statement_import_record ORDER BY statement_start, import_id");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                BankStatementRecord.BankAccountRef bankAccount = new BankStatementRecord.BankAccountRef(
                    rs.getString("bank_id"),
                    rs.getString("account_id"),
                    rs.getString("account_type"));

                BankStatementRecord.BalanceSnapshot ledgerBalance = null;
                if (rs.getBigDecimal("ledger_balance_amount") != null || rs.getTimestamp("ledger_balance_as_of") != null)
                {
                    ledgerBalance = new BankStatementRecord.BalanceSnapshot(
                        rs.getBigDecimal("ledger_balance_amount"),
                        rs.getTimestamp("ledger_balance_as_of") == null ? null : rs.getTimestamp("ledger_balance_as_of").toInstant().atOffset(ZoneOffset.UTC));
                }

                BankStatementRecord.BalanceSnapshot availableBalance = null;
                if (rs.getBigDecimal("available_balance_amount") != null || rs.getTimestamp("available_balance_as_of") != null)
                {
                    availableBalance = new BankStatementRecord.BalanceSnapshot(
                        rs.getBigDecimal("available_balance_amount"),
                        rs.getTimestamp("available_balance_as_of") == null ? null : rs.getTimestamp("available_balance_as_of").toInstant().atOffset(ZoneOffset.UTC));
                }

                rows.add(new BankStatementRecord(
                    rs.getString("import_id"),
                    parseSourceFormat(rs.getString("source_format")),
                    rs.getString("source_version"),
                    parseStatementKind(rs.getString("statement_kind")),
                    bankAccount,
                    rs.getString("currency"),
                    rs.getDate("statement_start") == null ? null : rs.getDate("statement_start").toLocalDate(),
                    rs.getDate("statement_end") == null ? null : rs.getDate("statement_end").toLocalDate(),
                    ledgerBalance,
                    availableBalance,
                    rs.getString("document_id"),
                    java.util.Map.of(),
                    rs.getString("raw_json")
                ));
            }
        }
        return rows;
    }

    private void ensureSchema() throws SQLException
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS bank_statement_import_record(
                import_id VARCHAR(255) PRIMARY KEY,
                source_format VARCHAR(64),
                source_version VARCHAR(64),
                statement_kind VARCHAR(64),
                bank_id VARCHAR(255),
                account_id VARCHAR(255),
                account_type VARCHAR(255),
                currency VARCHAR(16),
                statement_start DATE,
                statement_end DATE,
                ledger_balance_amount DECIMAL(19,4),
                ledger_balance_as_of TIMESTAMP,
                available_balance_amount DECIMAL(19,4),
                available_balance_as_of TIMESTAMP,
                document_id VARCHAR(255),
                raw_json CLOB
            )
            """;
        try (Connection c = Database.get().getConnection();
             Statement st = c.createStatement())
        {
            st.execute(sql);
        }
    }

    private static BankStatementRecord.SourceFormat parseSourceFormat(String value)
    {
        if (value == null || value.isBlank()) return null;
        try { return BankStatementRecord.SourceFormat.valueOf(value); } catch (IllegalArgumentException ex) { return BankStatementRecord.SourceFormat.OTHER; }
    }

    private static BankStatementRecord.StatementKind parseStatementKind(String value)
    {
        if (value == null || value.isBlank()) return null;
        try { return BankStatementRecord.StatementKind.valueOf(value); } catch (IllegalArgumentException ex) { return BankStatementRecord.StatementKind.OTHER; }
    }
}
