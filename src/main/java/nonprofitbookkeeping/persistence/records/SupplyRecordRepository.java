package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.SupplyRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported SCLX supply records into a concrete staging table.
 */
@ApplicationScoped
public class SupplyRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_supply_record (
            supply_id VARCHAR(255) PRIMARY KEY,
            item_number VARCHAR(255),
            date_acquired DATE,
            description CLOB,
            count_value INTEGER,
            approx_value_total DECIMAL(19,2),
            value_per_item DECIMAL(19,2),
            guardian_legal_name VARCHAR(255),
            guardian_email VARCHAR(255),
            guardian_phone VARCHAR(64),
            guardianship_date_as_of DATE,
            guardianship_last_confirmed DATE,
            guardianship_returned BOOLEAN,
            guardianship_notes CLOB,
            removal_approved_by VARCHAR(255),
            removal_reason CLOB,
            removal_number_removed INTEGER,
            removal_removed BOOLEAN,
            removal_type VARCHAR(128),
            additional_notes CLOB,
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_supply_record(
            supply_id, item_number, date_acquired, description, count_value,
            approx_value_total, value_per_item, guardian_legal_name, guardian_email,
            guardian_phone, guardianship_date_as_of, guardianship_last_confirmed,
            guardianship_returned, guardianship_notes, removal_approved_by,
            removal_reason, removal_number_removed, removal_removed, removal_type,
            additional_notes, extensions_json
        ) KEY(supply_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL_SQL = """
        SELECT
            supply_id, item_number, date_acquired, description, count_value,
            approx_value_total, value_per_item, guardian_legal_name, guardian_email,
            guardian_phone, guardianship_date_as_of, guardianship_last_confirmed,
            guardianship_returned, guardianship_notes, removal_approved_by,
            removal_reason, removal_number_removed, removal_removed, removal_type,
            additional_notes
        FROM imported_supply_record
        """;

    public void upsert(SupplyRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.supplyId());
                ps.setString(++i, row.itemNumber());
                ps.setDate(++i, row.dateAcquired() == null ? null : Date.valueOf(row.dateAcquired()));
                ps.setString(++i, row.description());
                ps.setObject(++i, row.count());
                ps.setBigDecimal(++i, row.approxValueTotal());
                ps.setBigDecimal(++i, row.valuePerItem());
                ps.setString(++i, row.guardian() == null ? null : row.guardian().legalName());
                ps.setString(++i, row.guardian() == null ? null : row.guardian().email());
                ps.setString(++i, row.guardian() == null ? null : row.guardian().phone());
                ps.setDate(++i, row.guardianshipDetails() == null || row.guardianshipDetails().dateAsOf() == null ? null : Date.valueOf(row.guardianshipDetails().dateAsOf()));
                ps.setDate(++i, row.guardianshipDetails() == null || row.guardianshipDetails().lastConfirmed() == null ? null : Date.valueOf(row.guardianshipDetails().lastConfirmed()));
                ps.setObject(++i, row.guardianshipDetails() == null ? null : row.guardianshipDetails().returned());
                ps.setString(++i, row.guardianshipDetails() == null ? null : row.guardianshipDetails().notes());
                ps.setString(++i, row.removalDetails() == null ? null : row.removalDetails().approvedBy());
                ps.setString(++i, row.removalDetails() == null ? null : row.removalDetails().reason());
                ps.setObject(++i, row.removalDetails() == null ? null : row.removalDetails().numberRemoved());
                ps.setObject(++i, row.removalDetails() == null ? null : row.removalDetails().removed());
                ps.setString(++i, row.removalDetails() == null ? null : row.removalDetails().removalType());
                ps.setString(++i, row.additionalNotes());
                ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
                ps.executeUpdate();
            }
        }
    }

    public List<SupplyRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<SupplyRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    rows.add(new SupplyRecord(
                        rs.getString("supply_id"),
                        rs.getString("item_number"),
                        rs.getDate("date_acquired") == null ? null : rs.getDate("date_acquired").toLocalDate(),
                        rs.getString("description"),
                        rs.getObject("count_value", Integer.class),
                        rs.getBigDecimal("approx_value_total"),
                        rs.getBigDecimal("value_per_item"),
                        guardian(rs),
                        guardianship(rs),
                        removalDetails(rs),
                        rs.getString("additional_notes"),
                        java.util.Map.of()
                    ));
                }
            }
            return rows;
        }
    }

    private void ensureTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(CREATE_SQL))
        {
            ps.execute();
        }
    }

    private static SupplyRecord.GuardianRecord guardian(ResultSet rs) throws SQLException
    {
        String legalName = rs.getString("guardian_legal_name");
        String email = rs.getString("guardian_email");
        String phone = rs.getString("guardian_phone");
        if (legalName == null && email == null && phone == null)
        {
            return null;
        }
        return new SupplyRecord.GuardianRecord(legalName, email, phone);
    }

    private static SupplyRecord.GuardianshipDetailsRecord guardianship(ResultSet rs) throws SQLException
    {
        Date dateAsOf = rs.getDate("guardianship_date_as_of");
        Date lastConfirmed = rs.getDate("guardianship_last_confirmed");
        Boolean returned = rs.getObject("guardianship_returned", Boolean.class);
        String notes = rs.getString("guardianship_notes");
        if (dateAsOf == null && lastConfirmed == null && returned == null && notes == null)
        {
            return null;
        }
        return new SupplyRecord.GuardianshipDetailsRecord(
            dateAsOf == null ? null : dateAsOf.toLocalDate(),
            lastConfirmed == null ? null : lastConfirmed.toLocalDate(),
            returned,
            notes
        );
    }

    private static SupplyRecord.RemovalDetailsRecord removalDetails(ResultSet rs) throws SQLException
    {
        String approvedBy = rs.getString("removal_approved_by");
        String reason = rs.getString("removal_reason");
        Integer numberRemoved = rs.getObject("removal_number_removed", Integer.class);
        Boolean removed = rs.getObject("removal_removed", Boolean.class);
        String removalType = rs.getString("removal_type");
        if (approvedBy == null && reason == null && numberRemoved == null && removed == null && removalType == null)
        {
            return null;
        }
        return new SupplyRecord.RemovalDetailsRecord(approvedBy, reason, numberRemoved, removed, removalType);
    }
}
