package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.OtherAssetItemRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
    private static final String LIST_ALL_SQL = """
        SELECT
            other_asset_item_id, ledger_transaction_id, ledger_line_id, workbook_sheet_key,
            workbook_row_index, paid_to, year_value, reason, type_value, type_code,
            event_budget_label, amount_as_of_prior_year_end, paid_returned_on_ledger_row_index,
            settlement_transaction_id, settlement_line_id, status
        FROM imported_other_asset_item_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_other_asset_item_record WHERE other_asset_item_id = ?";

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

    public List<OtherAssetItemRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<OtherAssetItemRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    var ledgerRef = linkRef(rs.getString("ledger_transaction_id"), rs.getString("ledger_line_id"));
                    var workbookRef = workbookRef(rs.getString("workbook_sheet_key"), rs.getObject("workbook_row_index", Integer.class));
                    var settlementRef = linkRef(rs.getString("settlement_transaction_id"), rs.getString("settlement_line_id"));
                    rows.add(new OtherAssetItemRecord(
                        rs.getString("other_asset_item_id"),
                        ledgerRef,
                        workbookRef,
                        rs.getString("paid_to"),
                        rs.getObject("year_value", Integer.class),
                        rs.getString("reason"),
                        rs.getString("type_value"),
                        rs.getString("type_code"),
                        rs.getString("event_budget_label"),
                        rs.getBigDecimal("amount_as_of_prior_year_end"),
                        rs.getObject("paid_returned_on_ledger_row_index", Integer.class),
                        settlementRef,
                        rs.getString("status"),
                        java.util.Map.of()
                    ));
                }
            }
            return rows;
        }
    }

    public int deleteById(String otherAssetItemId) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(DELETE_SQL))
            {
                ps.setString(1, otherAssetItemId);
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

    private static OtherAssetItemRecord.LedgerLinkRef linkRef(String transactionId, String lineId)
    {
        if (transactionId == null && lineId == null)
        {
            return null;
        }
        return new OtherAssetItemRecord.LedgerLinkRef(transactionId, lineId);
    }

    private static OtherAssetItemRecord.WorkbookLinkRef workbookRef(String sheetKey, Integer ledgerRowIndex)
    {
        if (sheetKey == null && ledgerRowIndex == null)
        {
            return null;
        }
        return new OtherAssetItemRecord.WorkbookLinkRef(sheetKey, ledgerRowIndex);
    }
}
