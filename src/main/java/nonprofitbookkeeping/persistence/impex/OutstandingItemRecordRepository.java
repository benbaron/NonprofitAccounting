package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.OutstandingItemRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private void ensureTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(CREATE_SQL))
        {
            ps.execute();
        }
    }
}
