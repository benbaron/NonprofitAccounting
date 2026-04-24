package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.model.records.AssetRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
            accumulated_depreciation DECIMAL(19,2),
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
            accumulated_depreciation, value_per_item, item_type, used_for, lot_paid_total, lot_item_count,
            guardian_legal_name, guardian_email, guardian_phone,
            guardianship_date_as_of, guardianship_confirmed, guardianship_confirmation_status,
            guardianship_notes, removal_approved_by, removal_approval_date,
            removal_reason, removal_number_removed, removal_removed, removal_type,
            extensions_json
        ) KEY(asset_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL_SQL = """
        SELECT
            asset_id, date_acquired, description, item_count, approx_value_total,
            accumulated_depreciation, value_per_item, item_type, used_for, lot_paid_total, lot_item_count,
            guardian_legal_name, guardian_email, guardian_phone,
            guardianship_date_as_of, guardianship_confirmed, guardianship_confirmation_status,
            guardianship_notes, removal_approved_by, removal_approval_date,
            removal_reason, removal_number_removed, removal_removed, removal_type
        FROM imported_asset_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_asset_record WHERE asset_id = ?";
    private static final String MIGRATE_ACCUMULATED_DEPRECIATION_SQL = """
        ALTER TABLE imported_asset_record
        ADD COLUMN IF NOT EXISTS accumulated_depreciation DECIMAL(19,2)
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
                ps.setBigDecimal(++i, row.accumulatedDepreciation());
                ps.setBigDecimal(++i, row.valuePerItem());
                ps.setString(++i, row.itemType() == null ? null : row.itemType().name());
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

    public List<AssetRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<AssetRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    rows.add(new AssetRecord(
                        rs.getString("asset_id"),
                        rs.getDate("date_acquired") == null ? null : rs.getDate("date_acquired").toLocalDate(),
                        rs.getString("description"),
                        rs.getObject("item_count", Integer.class),
                        rs.getBigDecimal("approx_value_total"),
                        rs.getBigDecimal("accumulated_depreciation"),
                        rs.getBigDecimal("value_per_item"),
                        AssetItemType.fromStorageValue(rs.getString("item_type")),
                        rs.getString("used_for"),
                        rs.getBigDecimal("lot_paid_total"),
                        rs.getObject("lot_item_count", Integer.class),
                        buildGuardian(rs),
                        buildGuardianshipDetails(rs),
                        buildRemovalDetails(rs),
                        java.util.Map.of()
                    ));
                }
            }
            return rows;
        }
    }

    public int deleteById(String assetId) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(DELETE_SQL))
            {
                ps.setString(1, assetId);
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
        try (PreparedStatement ps = c.prepareStatement(MIGRATE_ACCUMULATED_DEPRECIATION_SQL))
        {
            ps.execute();
        }
    }

    private static AssetRecord.GuardianRecord buildGuardian(ResultSet rs) throws SQLException
    {
        String legalName = rs.getString("guardian_legal_name");
        String email = rs.getString("guardian_email");
        String phone = rs.getString("guardian_phone");
        if (legalName == null && email == null && phone == null)
        {
            return null;
        }
        return new AssetRecord.GuardianRecord(legalName, email, phone);
    }

    private static AssetRecord.GuardianshipDetailsRecord buildGuardianshipDetails(ResultSet rs) throws SQLException
    {
        Date dateAsOf = rs.getDate("guardianship_date_as_of");
        Boolean confirmed = rs.getObject("guardianship_confirmed", Boolean.class);
        String confirmationStatus = rs.getString("guardianship_confirmation_status");
        String notes = rs.getString("guardianship_notes");
        if (dateAsOf == null && confirmed == null && confirmationStatus == null && notes == null)
        {
            return null;
        }
        return new AssetRecord.GuardianshipDetailsRecord(
            dateAsOf == null ? null : dateAsOf.toLocalDate(),
            confirmed,
            confirmationStatus,
            notes
        );
    }

    private static AssetRecord.RemovalDetailsRecord buildRemovalDetails(ResultSet rs) throws SQLException
    {
        String approvedBy = rs.getString("removal_approved_by");
        Date approvalDate = rs.getDate("removal_approval_date");
        String reason = rs.getString("removal_reason");
        Integer numberRemoved = rs.getObject("removal_number_removed", Integer.class);
        Boolean removed = rs.getObject("removal_removed", Boolean.class);
        String removalType = rs.getString("removal_type");
        if (approvedBy == null && approvalDate == null && reason == null
            && numberRemoved == null && removed == null && removalType == null)
        {
            return null;
        }
        return new AssetRecord.RemovalDetailsRecord(
            approvedBy,
            approvalDate == null ? null : approvalDate.toLocalDate(),
            reason,
            numberRemoved,
            removed,
            removalType
        );
    }
}
