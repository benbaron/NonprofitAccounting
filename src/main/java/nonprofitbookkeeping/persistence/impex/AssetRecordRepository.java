package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.AssetRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists imported SCLX asset records into a concrete staging table.
 */
@ApplicationScoped
public class AssetRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_asset_record (
            asset_id VARCHAR(255) PRIMARY KEY,
            date_acquired DATE,
            description CLOB,
            item_count INTEGER,
            approx_value_total DECIMAL(19,2),
            value_per_item DECIMAL(19,2),
            item_type VARCHAR(128),
            used_for VARCHAR(255),
            lot_paid_total DECIMAL(19,2),
            lot_item_count INTEGER,
            guardian_legal_name VARCHAR(255),
            guardian_email VARCHAR(255),
            guardian_phone VARCHAR(64),
            guardianship_date_as_of DATE,
            guardianship_confirmed BOOLEAN,
            guardianship_confirmation_status VARCHAR(128),
            guardianship_notes CLOB,
            removal_approved_by VARCHAR(255),
            removal_approval_date DATE,
            removal_reason CLOB,
            removal_number_removed INTEGER,
            removal_removed BOOLEAN,
            removal_type VARCHAR(128),
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_asset_record(
            asset_id, date_acquired, description, item_count, approx_value_total,
            value_per_item, item_type, used_for, lot_paid_total, lot_item_count,
            guardian_legal_name, guardian_email, guardian_phone,
            guardianship_date_as_of, guardianship_confirmed, guardianship_confirmation_status,
            guardianship_notes, removal_approved_by, removal_approval_date,
            removal_reason, removal_number_removed, removal_removed, removal_type,
            extensions_json
        ) KEY(asset_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    public void upsert(AssetRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.assetId());
                ps.setDate(++i, row.dateAcquired() == null ? null : Date.valueOf(row.dateAcquired()));
                ps.setString(++i, row.description());
                ps.setObject(++i, row.itemCount());
                ps.setBigDecimal(++i, row.approxValueTotal());
                ps.setBigDecimal(++i, row.valuePerItem());
                ps.setString(++i, row.itemType());
                ps.setString(++i, row.usedFor());
                ps.setBigDecimal(++i, row.lotPaidTotal());
                ps.setObject(++i, row.lotItemCount());
                ps.setString(++i, row.currentGuardian() == null ? null : row.currentGuardian().legalName());
                ps.setString(++i, row.currentGuardian() == null ? null : row.currentGuardian().email());
                ps.setString(++i, row.currentGuardian() == null ? null : row.currentGuardian().phone());
                ps.setDate(++i, row.guardianshipDetails() == null || row.guardianshipDetails().dateAsOf() == null ? null : Date.valueOf(row.guardianshipDetails().dateAsOf()));
                ps.setObject(++i, row.guardianshipDetails() == null ? null : row.guardianshipDetails().confirmed());
                ps.setString(++i, row.guardianshipDetails() == null ? null : row.guardianshipDetails().confirmationStatus());
                ps.setString(++i, row.guardianshipDetails() == null ? null : row.guardianshipDetails().notes());
                ps.setString(++i, row.removalDetails() == null ? null : row.removalDetails().approvedBy());
                ps.setDate(++i, row.removalDetails() == null || row.removalDetails().approvalDate() == null ? null : Date.valueOf(row.removalDetails().approvalDate()));
                ps.setString(++i, row.removalDetails() == null ? null : row.removalDetails().reason());
                ps.setObject(++i, row.removalDetails() == null ? null : row.removalDetails().numberRemoved());
                ps.setObject(++i, row.removalDetails() == null ? null : row.removalDetails().removed());
                ps.setString(++i, row.removalDetails() == null ? null : row.removalDetails().removalType());
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
