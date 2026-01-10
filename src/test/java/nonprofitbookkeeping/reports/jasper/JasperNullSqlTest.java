package nonprofitbookkeeping.reports.jasper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aReportBean;
import nonprofitbookkeeping.reports.jasper.generator.ASSET_DTL_5aJasperGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapSqlBuilder;

class JasperNullSqlTest
{
    private static final class ExposedAssetDetailGenerator
        extends ASSET_DTL_5aJasperGenerator
    {
        private List<AssetDtl5aReportBean> load()
        {
            return super.getReportData();
        }
    }

    @Test
    void fieldMapSqlBuilderUsesNullForMissingExpressions() throws Exception
    {
        String selectList = FieldMapSqlBuilder.buildSelectList(
            "/nonprofitbookkeeping/reports/ASSET_DTL_5a_fieldmap.csv",
            null);

        assertTrue(selectList.contains("NULL as asset_dtl_5a_r2c3"));
        assertTrue(selectList.contains("NULL as contents_b59"));
    }

    @Test
    void fieldMapSqlBuilderUsesDbExprWhenPresent() throws Exception
    {
        String selectList = FieldMapSqlBuilder.buildSelectList(
            "/nonprofitbookkeeping/reports/test_fieldmap_with_dbexpr.csv",
            null);

        assertTrue(selectList.contains("accounts.name as account_name"));
        assertTrue(selectList.contains("NULL as fallback_field"));
    }

    @Test
    void assetDetailGeneratorReturnsNullsWhenSelectUsesNullFallback(@TempDir Path tempDir)
        throws SQLException
    {
        TestDatabase.reset(tempDir);

        try (Connection connection = Database.get().getConnection();
            Statement statement = connection.createStatement())
        {
            statement.execute(
                "insert into account (account_number, name) values ('1000', 'Cash')");
            statement.execute(
                "insert into journal_transaction (id, booking_ts, date_text, memo) " +
                    "values (1, 0, '2024-01-01', 'Sample memo')");
            statement.execute(
                "insert into journal_entry (txn_id, amount, account_number) " +
                    "values (1, 42.00, '1000')");
        }

        ExposedAssetDetailGenerator generator = new ExposedAssetDetailGenerator();
        List<AssetDtl5aReportBean> data = generator.load();

        assertFalse(data.isEmpty());

        AssetDtl5aReportBean bean = data.get(0);
        assertNull(bean.getContents_b59());
    }
}
