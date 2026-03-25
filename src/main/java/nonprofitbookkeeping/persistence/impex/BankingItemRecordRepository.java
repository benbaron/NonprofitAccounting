package nonprofitbookkeeping.persistence.impex;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BankingItemRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persists imported banking-item records as staged impex rows.
 */
public class BankingItemRecordRepository
{
    private static final String DDL = """
        CREATE TABLE IF NOT EXISTS imported_banking_item (
            banking_item_id VARCHAR(255) PRIMARY KEY,
            kind VARCHAR(64),
            bank_account_id VARCHAR(255),
            transaction_id VARCHAR(255),
            line_ids CLOB,
            cleared_date DATE,
            amount DECIMAL(19,2),
            check_number VARCHAR(255),
            payee VARCHAR(255),
            deposit_date DATE,
            payer VARCHAR(255),
            deposit_id VARCHAR(255),
            memo CLOB,
            source VARCHAR(64),
            status VARCHAR(64),
            import_id VARCHAR(255),
            ofx_fit_id VARCHAR(255),
            ofx_reference_number VARCHAR(255),
            ofx_name VARCHAR(255)
        )
        """;

    private static final String UPSERT = """
        MERGE INTO imported_banking_item(
            banking_item_id, kind, bank_account_id, transaction_id, line_ids,
            cleared_date, amount, check_number, payee, deposit_date, payer,
            deposit_id, memo, source, status, import_id,
            ofx_fit_id, ofx_reference_number, ofx_name
        ) KEY(banking_item_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    private static final String LIST_ALL = """
        SELECT
            banking_item_id, kind, bank_account_id, transaction_id, line_ids,
            cleared_date, amount, check_number, payee, deposit_date, payer,
            deposit_id, memo, source, status, import_id,
            ofx_fit_id, ofx_reference_number, ofx_name
        FROM imported_banking_item
        """;

    public void upsert(BankingItemRecord record) throws SQLException
    {
        ensureSchema();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(UPSERT))
        {
            int i = 0;
            ps.setString(++i, record.bankingItemId());
            ps.setString(++i, record.kind());
            ps.setString(++i, record.bankAccountId());
            ps.setString(++i, record.transactionId());
            ps.setString(++i, record.lineIds() == null ? null : record.lineIds().stream().collect(Collectors.joining(",")));
            ps.setDate(++i, record.clearedDate() == null ? null : java.sql.Date.valueOf(record.clearedDate()));
            ps.setBigDecimal(++i, record.amount());
            ps.setString(++i, record.checkNumber());
            ps.setString(++i, record.payee());
            ps.setDate(++i, record.depositDate() == null ? null : java.sql.Date.valueOf(record.depositDate()));
            ps.setString(++i, record.payer());
            ps.setString(++i, record.depositId());
            ps.setString(++i, record.memo());
            ps.setString(++i, record.source());
            ps.setString(++i, record.status());
            ps.setString(++i, record.importId());
            ps.setString(++i, record.ofx() == null ? null : record.ofx().fitId());
            ps.setString(++i, record.ofx() == null ? null : record.ofx().referenceNumber());
            ps.setString(++i, record.ofx() == null ? null : record.ofx().name());
            ps.executeUpdate();
        }
    }

    public List<BankingItemRecord> listAll() throws SQLException
    {
        ensureSchema();
        List<BankingItemRecord> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(LIST_ALL);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                String lineIdsCsv = rs.getString("line_ids");
                List<String> lineIds = lineIdsCsv == null || lineIdsCsv.isBlank()
                    ? List.of()
                    : java.util.Arrays.stream(lineIdsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

                String fitId = rs.getString("ofx_fit_id");
                String referenceNumber = rs.getString("ofx_reference_number");
                String ofxName = rs.getString("ofx_name");
                BankingItemRecord.OfxTransactionRecord ofx =
                    (fitId == null && referenceNumber == null && ofxName == null)
                        ? null
                        : new BankingItemRecord.OfxTransactionRecord(
                        fitId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        referenceNumber,
                        ofxName,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        java.util.Map.of()
                    );

                rows.add(new BankingItemRecord(
                    rs.getString("banking_item_id"),
                    rs.getString("kind"),
                    rs.getString("bank_account_id"),
                    rs.getString("transaction_id"),
                    lineIds,
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
                    ofx,
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
