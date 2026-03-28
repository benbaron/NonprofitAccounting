package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.OtherAssetItemRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists imported SCLX other-asset item records into a concrete staging table.
 */
@ApplicationScoped
public class OtherAssetItemRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_other_asset_item_record (
            other_asset_item_id VARCHAR(255) PRIMARY KEY,
            ledger_transaction_id VARCHAR(255),
            ledger_line_id VARCHAR(255),
            workbook_sheet_key VARCHAR(255),
            workbook_row_index INTEGER,
            paid_to VARCHAR(255),
            year_value INTEGER,
            reason CLOB,
            type_value VARCHAR(128),
            type_code VARCHAR(128),
            event_budget_label VARCHAR(255),
            amount_as_of_prior_year_end DECIMAL(19,2),
            paid_returned_on_ledger_row_index INTEGER,
            settlement_transaction_id VARCHAR(255),
            settlement_line_id VARCHAR(255),
            status VARCHAR(64),
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_other_asset_item_record(
            other_asset_item_id, ledger_transaction_id, ledger_line_id,
            workbook_sheet_key, workbook_row_index, paid_to, year_value,
            reason, type_value, type_code, event_budget_label,
            amount_as_of_prior_year_end, paid_returned_on_ledger_row_index,
            settlement_transaction_id, settlement_line_id, status, extensions_json
        ) KEY(other_asset_item_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    public void upsert(OtherAssetItemRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.otherAssetItemId());
                ps.setString(++i, row.ledgerLink() == null ? null : row.ledgerLink().transactionId());
                ps.setString(++i, row.ledgerLink() == null ? null : row.ledgerLink().lineId());
                ps.setString(++i, row.workbookLink() == null ? null : row.workbookLink().sheetKey());
                ps.setObject(++i, row.workbookLink() == null ? null : row.workbookLink().ledgerRowIndex());
                ps.setString(++i, row.paidTo());
                ps.setObject(++i, row.year());
                ps.setString(++i, row.reason());
                ps.setString(++i, row.type());
                ps.setString(++i, row.typeCode());
                ps.setString(++i, row.eventBudgetLabel());
                ps.setBigDecimal(++i, row.amountAsOfPriorYearEnd());
                ps.setObject(++i, row.paidReturnedOnLedgerRowIndex());
                ps.setString(++i, row.settlementLedgerLink() == null ? null : row.settlementLedgerLink().transactionId());
                ps.setString(++i, row.settlementLedgerLink() == null ? null : row.settlementLedgerLink().lineId());
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
