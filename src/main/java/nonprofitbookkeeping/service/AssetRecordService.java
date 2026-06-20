package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.persistence.records.AssetRecordRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service for fixed asset register operations.
 *
 * <p>The legacy {@link nonprofitbookkeeping.model.InventoryItem} document model
 * and the normalized asset tables are intentionally kept separate: inventory is
 * a backwards-compatible document workflow while fixed assets are persisted as
 * {@link AssetRecord} rows plus {@code asset_record_detail} depreciation data.</p>
 */
public class AssetRecordService
{
    public static final String STRAIGHT_LINE = "STRAIGHT_LINE";

    private final AssetRecordRepository repository;

    public AssetRecordService()
    {
        this(new AssetRecordRepository());
    }

    public AssetRecordService(AssetRecordRepository repository)
    {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public void save(AssetRecord record) throws SQLException
    {
        repository.upsert(record);
    }

    public List<AssetRecord> listAll() throws SQLException
    {
        return repository.listAll();
    }

    public int delete(String assetId) throws SQLException
    {
        return repository.deleteById(assetId);
    }

    public List<AssetRegisterRow> listRegisterRows() throws SQLException
    {
        ensureDatabaseOpen();
        List<AssetRegisterRow> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection())
        {
            ensureDetailTable(c);
            for (AssetRecord record : repository.listAll())
            {
                rows.add(loadRegisterRow(c, record));
            }
        }
        return rows;
    }

    public AssetRegisterRow saveRegisterRow(AssetRegisterSaveRequest request) throws SQLException
    {
        validate(request);
        ensureDatabaseOpen();
        AssetRecord record = new AssetRecord(
            request.assetId().trim(),
            request.dateAcquired(),
            blankToNull(request.description()),
            request.itemCount(),
            moneyOrNull(request.approxValueTotal()),
            moneyOrZero(request.accumulatedDepreciation()),
            null,
            request.itemType(),
            null,
            null,
            null,
            null,
            null,
            null,
            Map.of());
        repository.upsert(record);
        try (Connection c = Database.get().getConnection())
        {
            ensureDetailTable(c);
            upsertDetail(c, request, "ACTIVE", null);
            return loadRegisterRow(c, record);
        }
    }

    public AssetRegisterRow deactivate(String assetId) throws SQLException
    {
        return updateState(assetId, "RETIRED", null);
    }

    public AssetRegisterRow dispose(String assetId, java.time.LocalDate disposalDate) throws SQLException
    {
        if (disposalDate == null)
        {
            throw new IllegalArgumentException("Disposal date is required.");
        }
        return updateState(assetId, "DISPOSED", disposalDate);
    }

    private AssetRegisterRow updateState(String assetId, String state, java.time.LocalDate disposalDate) throws SQLException
    {
        if (assetId == null || assetId.isBlank())
        {
            throw new IllegalArgumentException("Asset ID is required.");
        }
        ensureDatabaseOpen();
        try (Connection c = Database.get().getConnection())
        {
            ensureDetailTable(c);
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE asset_record_detail
                SET asset_state = ?, disposal_date = ?, updated_at = CURRENT_TIMESTAMP
                WHERE asset_record_id = ?
                """))
            {
                ps.setString(1, state);
                ps.setDate(2, disposalDate == null ? Date.valueOf(java.time.LocalDate.now()) : Date.valueOf(disposalDate));
                ps.setString(3, assetId.trim());
                if (ps.executeUpdate() == 0)
                {
                    throw new IllegalArgumentException("Asset detail not found: " + assetId);
                }
            }
            AssetRecord record = repository.listAll().stream()
                .filter(r -> assetId.trim().equals(r.assetId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetId));
            return loadRegisterRow(c, record);
        }
    }

    private AssetRegisterRow loadRegisterRow(Connection c, AssetRecord record) throws SQLException
    {
        AssetDetail detail = loadDetail(c, record.assetId());
        BigDecimal cost = moneyOrZero(record.approxValueTotal());
        BigDecimal accumulated = moneyOrZero(record.accumulatedDepreciation());
        BigDecimal net = cost.subtract(accumulated).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        List<String> runLinks = depreciationRunLinks(c, record.assetId());
        return new AssetRegisterRow(record, detail.depreciationMethod(), detail.usefulLifeMonths(),
            detail.assetState(), detail.disposalDate(), accumulated, net, runLinks);
    }

    private AssetDetail loadDetail(Connection c, String assetId) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT depreciation_method, useful_life_months, asset_state, disposal_date
            FROM asset_record_detail WHERE asset_record_id = ?
            """))
        {
            ps.setString(1, assetId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new AssetDetail(rs.getString(1), (Integer) rs.getObject(2), rs.getString(3),
                        rs.getDate(4) == null ? null : rs.getDate(4).toLocalDate());
                }
            }
        }
        return new AssetDetail(STRAIGHT_LINE, null, "DRAFT", null);
    }

    private List<String> depreciationRunLinks(Connection c, String assetId) throws SQLException
    {
        List<String> links = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT depreciation_run_id, net_depreciation
            FROM depreciation_record
            WHERE asset_record_id = ?
            ORDER BY depreciation_date DESC, sequence_in_run
            """))
        {
            ps.setString(1, assetId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    links.add(rs.getString(1) + " (" + moneyOrZero(rs.getBigDecimal(2)).toPlainString() + ")");
                }
            }
        }
        return links;
    }

    private void upsertDetail(Connection c, AssetRegisterSaveRequest request, String defaultState, java.time.LocalDate disposalDate) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO asset_record_detail(asset_record_id, asset_type, depreciation_method, date_acquired,
              asset_state, in_service_date, disposal_date, depreciable_basis, salvage_value, useful_life_months, updated_at)
            KEY(asset_record_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """))
        {
            ps.setString(1, request.assetId().trim());
            ps.setString(2, request.itemType() == null ? null : request.itemType().displayName());
            ps.setString(3, blankToNull(request.depreciationMethod()) == null ? STRAIGHT_LINE : request.depreciationMethod().trim());
            ps.setDate(4, request.dateAcquired() == null ? null : Date.valueOf(request.dateAcquired()));
            ps.setString(5, defaultState);
            ps.setDate(6, request.dateAcquired() == null ? null : Date.valueOf(request.dateAcquired()));
            ps.setDate(7, disposalDate == null ? null : Date.valueOf(disposalDate));
            ps.setBigDecimal(8, moneyOrNull(request.approxValueTotal()));
            ps.setBigDecimal(9, BigDecimal.ZERO.setScale(2));
            ps.setObject(10, request.usefulLifeMonths());
            ps.executeUpdate();
        }
    }

    public static void validate(AssetRegisterSaveRequest request)
    {
        if (request == null) throw new IllegalArgumentException("Asset is required.");
        if (request.assetId() == null || request.assetId().isBlank()) throw new IllegalArgumentException("Asset ID is required.");
        if (request.approxValueTotal() == null || request.approxValueTotal().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Asset cost must be zero or greater.");
        if (request.accumulatedDepreciation() != null && request.accumulatedDepreciation().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Accumulated depreciation must be zero or greater.");
        if (request.accumulatedDepreciation() != null && request.accumulatedDepreciation().compareTo(request.approxValueTotal()) > 0) throw new IllegalArgumentException("Accumulated depreciation cannot exceed asset cost.");
        if (request.usefulLifeMonths() != null && request.usefulLifeMonths() <= 0) throw new IllegalArgumentException("Useful life must be greater than zero months.");
    }

    public static BigDecimal parseMoneyInput(String raw, String fieldName)
    {
        if (raw == null || raw.isBlank()) return null;
        String text = raw.trim();
        if (text.contains("$") || text.contains(","))
        {
            throw new IllegalArgumentException(fieldName + " must be entered as a plain number, not formatted currency.");
        }
        try
        {
            return new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException(fieldName + " must be a valid decimal number.");
        }
    }

    private void ensureDatabaseOpen()
    {
        if (!Database.isInitialized()) throw new IllegalStateException("Open or create a company database first.");
    }

    private void ensureDetailTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            CREATE TABLE IF NOT EXISTS asset_record_detail(
              asset_record_id VARCHAR(255) PRIMARY KEY,
              asset_type VARCHAR(128), depreciation_method VARCHAR(64), details CLOB,
              date_acquired DATE, date_sold DATE, journal_txn_id INT,
              asset_state VARCHAR(30) DEFAULT 'DRAFT' NOT NULL,
              in_service_date DATE, disposal_date DATE, depreciable_basis DECIMAL(19,2),
              salvage_value DECIMAL(19,2) DEFAULT 0, useful_life_months INT,
              posted_acquisition_txn_id INT, posted_disposal_txn_id INT,
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)
            """)) { ps.execute(); }
    }

    private static BigDecimal moneyOrZero(BigDecimal value) { return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal moneyOrNull(BigDecimal value) { return value == null ? null : value.setScale(2, RoundingMode.HALF_UP); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private record AssetDetail(String depreciationMethod, Integer usefulLifeMonths, String assetState, java.time.LocalDate disposalDate) {}

    public record AssetRegisterSaveRequest(String assetId, java.time.LocalDate dateAcquired, String description,
        Integer itemCount, BigDecimal approxValueTotal, BigDecimal accumulatedDepreciation,
        AssetItemType itemType, String depreciationMethod, Integer usefulLifeMonths) {}

    public record AssetRegisterRow(AssetRecord record, String depreciationMethod, Integer usefulLifeMonths,
        String assetState, java.time.LocalDate disposalDate, BigDecimal accumulatedDepreciation,
        BigDecimal netBookValue, List<String> depreciationRunLinks) {}
}
