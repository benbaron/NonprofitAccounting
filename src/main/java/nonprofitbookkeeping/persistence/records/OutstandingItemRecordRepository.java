package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.OutstandingItemRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported SCLX outstanding-item records into a concrete staging table.
 */
@ApplicationScoped
public class OutstandingItemRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_outstanding_item_record (
            outstanding_item_id VARCHAR(255) PRIMARY KEY,
            kind VARCHAR(64),
            ledger_transaction_id VARCHAR(255),
            ledger_line_id VARCHAR(255),
            workbook_sheet_key VARCHAR(255),
            workbook_row_index INTEGER,
            date_sent_or_received DATE,
            incoming_check_or_transfer_date DATE,
            transfer_id_or_check_number VARCHAR(255),
            date_shows_on_statement DATE,
            person_or_business_name VARCHAR(255),
            details_notes CLOB,
            from_to_card_merchant VARCHAR(255),
            account_for_payment_or_deposit VARCHAR(255),
            amount DECIMAL(19,2),
            date_reversed DATE,
            reversal_reason_and_approval CLOB,
            reversal_transaction_id VARCHAR(255),
            reversal_line_id VARCHAR(255),
            status VARCHAR(64),
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_outstanding_item_record(
            outstanding_item_id, kind, ledger_transaction_id, ledger_line_id,
            workbook_sheet_key, workbook_row_index, date_sent_or_received,
            incoming_check_or_transfer_date, transfer_id_or_check_number,
            date_shows_on_statement, person_or_business_name, details_notes,
            from_to_card_merchant, account_for_payment_or_deposit, amount,
            date_reversed, reversal_reason_and_approval, reversal_transaction_id,
            reversal_line_id, status, extensions_json
        ) KEY(outstanding_item_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL_SQL = """
        SELECT
            outstanding_item_id, kind, ledger_transaction_id, ledger_line_id,
            workbook_sheet_key, workbook_row_index, date_sent_or_received,
            incoming_check_or_transfer_date, transfer_id_or_check_number,
            date_shows_on_statement, person_or_business_name, details_notes,
            from_to_card_merchant, account_for_payment_or_deposit, amount,
            date_reversed, reversal_reason_and_approval, reversal_transaction_id,
            reversal_line_id, status
        FROM imported_outstanding_item_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_outstanding_item_record WHERE outstanding_item_id = ?";

    public void upsert(OutstandingItemRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.outstandingItemId());
                ps.setString(++i, row.kind());
                ps.setString(++i, row.ledgerLink() == null ? null : row.ledgerLink().transactionId());
                ps.setString(++i, row.ledgerLink() == null ? null : row.ledgerLink().lineId());
                ps.setString(++i, row.workbookLink() == null ? null : row.workbookLink().sheetKey());
                if (row.workbookLink() == null) {
                    ps.setObject(++i, null);
                } else {
                    ps.setObject(++i, row.workbookLink().ledgerRowIndex());
                }
                ps.setDate(++i, row.dateSentOrReceived() == null ? null : Date.valueOf(row.dateSentOrReceived()));
                ps.setDate(++i, row.incomingCheckOrTransferDate() == null ? null : Date.valueOf(row.incomingCheckOrTransferDate()));
                ps.setString(++i, row.transferIdOrCheckNumber());
                ps.setDate(++i, row.dateShowsOnStatement() == null ? null : Date.valueOf(row.dateShowsOnStatement()));
                ps.setString(++i, row.personOrBusinessName());
                ps.setString(++i, row.detailsNotes());
                ps.setString(++i, row.fromToCardMerchant());
                ps.setString(++i, row.accountForPaymentOrDeposit());
                ps.setBigDecimal(++i, row.amount());
                ps.setDate(++i, row.dateReversed() == null ? null : Date.valueOf(row.dateReversed()));
                ps.setString(++i, row.reversalReasonAndApproval());
                ps.setString(++i, row.reversalLedgerLink() == null ? null : row.reversalLedgerLink().transactionId());
                ps.setString(++i, row.reversalLedgerLink() == null ? null : row.reversalLedgerLink().lineId());
                ps.setString(++i, row.status());
                ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
                ps.executeUpdate();
            }
        }
    }

    public List<OutstandingItemRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<OutstandingItemRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    rows.add(new OutstandingItemRecord(
                        rs.getString("outstanding_item_id"),
                        rs.getString("kind"),
                        ledgerRef(rs.getString("ledger_transaction_id"), rs.getString("ledger_line_id")),
                        workbookRef(rs.getString("workbook_sheet_key"), rs.getObject("workbook_row_index", Integer.class)),
                        rs.getDate("date_sent_or_received") == null ? null : rs.getDate("date_sent_or_received").toLocalDate(),
                        rs.getDate("incoming_check_or_transfer_date") == null ? null : rs.getDate("incoming_check_or_transfer_date").toLocalDate(),
                        rs.getString("transfer_id_or_check_number"),
                        rs.getDate("date_shows_on_statement") == null ? null : rs.getDate("date_shows_on_statement").toLocalDate(),
                        rs.getString("person_or_business_name"),
                        rs.getString("details_notes"),
                        rs.getString("from_to_card_merchant"),
                        rs.getString("account_for_payment_or_deposit"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("date_reversed") == null ? null : rs.getDate("date_reversed").toLocalDate(),
                        rs.getString("reversal_reason_and_approval"),
                        ledgerRef(rs.getString("reversal_transaction_id"), rs.getString("reversal_line_id")),
                        rs.getString("status"),
                        java.util.Map.of()
                    ));
                }
            }
            return rows;
        }
    }

    public int deleteById(String outstandingItemId) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(DELETE_SQL))
            {
                ps.setString(1, outstandingItemId);
                return ps.executeUpdate();
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

    private static OutstandingItemRecord.LedgerLinkRef ledgerRef(String transactionId, String lineId)
    {
        if (transactionId == null && lineId == null)
        {
            return null;
        }
        return new OutstandingItemRecord.LedgerLinkRef(transactionId, lineId);
    }

    private static OutstandingItemRecord.WorkbookLinkRef workbookRef(String sheetKey, Integer ledgerRowIndex)
    {
        if (sheetKey == null && ledgerRowIndex == null)
        {
            return null;
        }
        return new OutstandingItemRecord.WorkbookLinkRef(sheetKey, ledgerRowIndex);
    }
}
