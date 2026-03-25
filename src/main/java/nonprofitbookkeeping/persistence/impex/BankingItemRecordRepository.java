package nonprofitbookkeeping.persistence.impex;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BankingItemRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BankingItemRecordRepository
{
    public BankingItemRecordRepository()
    {
        try
        {
            ensureSchema();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to initialize banking_item_record table", ex);
        }
    }

    public void upsert(BankingItemRecord row) throws SQLException
    {
        String sql =
            "MERGE INTO banking_item_record(banking_item_id, kind, bank_account_id, transaction_id, cleared_date, amount, check_number, payee, deposit_date, payer, deposit_id, memo, source, status, import_id, raw_json) " +
            "KEY(banking_item_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            int i = 0;
            ps.setString(++i, row.bankingItemId());
            ps.setString(++i, row.kind());
            ps.setString(++i, row.bankAccountId());
            ps.setString(++i, row.transactionId());
            ps.setDate(++i, row.clearedDate() == null ? null : Date.valueOf(row.clearedDate()));
            ps.setBigDecimal(++i, row.amount());
            ps.setString(++i, row.checkNumber());
            ps.setString(++i, row.payee());
            ps.setDate(++i, row.depositDate() == null ? null : Date.valueOf(row.depositDate()));
            ps.setString(++i, row.payer());
            ps.setString(++i, row.depositId());
            ps.setString(++i, row.memo());
            ps.setString(++i, row.source());
            ps.setString(++i, row.status());
            ps.setString(++i, row.importId());
            ps.setString(++i, row.rawJson());
            ps.executeUpdate();
        }
    }

    public List<BankingItemRecord> listAll() throws SQLException
    {
        List<BankingItemRecord> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM banking_item_record ORDER BY cleared_date, banking_item_id");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                rows.add(new BankingItemRecord(
                    rs.getString("banking_item_id"),
                    rs.getString("kind"),
                    rs.getString("bank_account_id"),
                    rs.getString("transaction_id"),
                    java.util.List.of(),
                    rs.getDate("cleared_date") == null ? null : rs.getDate("cleared_date").toLocalDate(),
                    rs.getBigDecimal("amount"),
                    rs.getString("check_number"),
                    rs.getString("payee"),
                    rs.getDate("deposit_date") == null ? null : rs.getDate("deposit_date").toLocalDate(),
                    rs.getString("payer"),
                    rs.getString("deposit_id"),
                    rs.getString("memo"),
                    rs.getString("source"),
                    rs.getString("status"),
                    rs.getString("import_id"),
                    null,
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
            CREATE TABLE IF NOT EXISTS banking_item_record(
                banking_item_id VARCHAR(255) PRIMARY KEY,
                kind VARCHAR(64),
                bank_account_id VARCHAR(255),
                transaction_id VARCHAR(255),
                cleared_date DATE,
                amount DECIMAL(19,4),
                check_number VARCHAR(255),
                payee VARCHAR(255),
                deposit_date DATE,
                payer VARCHAR(255),
                deposit_id VARCHAR(255),
                memo CLOB,
                source VARCHAR(64),
                status VARCHAR(64),
                import_id VARCHAR(255),
                raw_json CLOB
            )
            """;
        try (Connection c = Database.get().getConnection();
             Statement st = c.createStatement())
        {
            st.execute(sql);
        }
    }
}
