package org.nonprofitbookkeeping.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AlternateDatabaseAdminServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void invalidPathFailsBeforeRecentDatabaseIsChanged() throws Exception
    {
        UiSessionContext context = new UiSessionContext();
        AlternateDatabaseAdminService service = service(context);

        assertThrows(IOException.class, () -> service.openDatabase(tempDir.resolve("missing.mv.db")));

        assertFalse(context.isDatabaseOpen());
    }

    @Test
    void unsupportedExtensionFails() throws Exception
    {
        Path txt = tempDir.resolve("company.txt");
        Files.writeString(txt, "not a database");

        assertThrows(IllegalArgumentException.class, () -> service(new UiSessionContext()).validateDatabase(txt));
    }

    @Test
    void exportTargetExistsFailsWithoutOverwrite() throws Exception
    {
        Path source = tempDir.resolve("source.mv.db");
        Path target = tempDir.resolve("target.mv.db");
        Files.writeString(source, "source");
        Files.writeString(target, "target");

        IOException ex = assertThrows(IOException.class,
            () -> service(new UiSessionContext()).exportDatabase(source, target, false));

        assertTrue(ex.getMessage().contains("Target already exists"));
        assertEquals("target", Files.readString(target));
    }

    @Test
    void repairActiveDatabaseRequiresBackupConfirmation() throws Exception
    {
        Path source = tempDir.resolve("active.mv.db");
        Files.writeString(source, "not a valid h2 database");
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(tempDir.resolve("active"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> service(context).repairDatabase(source, false, false));

        assertTrue(ex.getMessage().contains("backup confirmation"));
        assertTrue(context.isDatabaseOpen());
    }

    @Test
    void repairFailureClosesActiveContextOnlyAfterConfirmation() throws Exception
    {
        Path source = tempDir.resolve("active.mv.db");
        Files.createDirectory(source);
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(tempDir.resolve("active"));

        assertThrows(Exception.class, () -> failingRepairService(context).repairDatabase(source, true, false));

        assertFalse(context.isDatabaseOpen());
    }

    @Test
    void importCanOpenAfterSuccessfulCopy() throws Exception
    {
        Path source = tempDir.resolve("source.mv.db");
        Path target = tempDir.resolve("target.mv.db");
        Files.writeString(source, "database payload");
        UiSessionContext context = new UiSessionContext();

        AlternateDatabaseAdminService.AdminResult result = service(context).importDatabase(source, target, true);

        assertEquals("database payload", Files.readString(target));
        assertTrue(context.isDatabaseOpen());
        assertEquals(tempDir.resolve("target").toAbsolutePath().normalize(), context.activeDatabaseBasePath());
        assertNotNull(result.openResult());
    }

    @Test
    void repairCanOpenAfterSuccessfulRecovery() throws Exception
    {
        Path source = tempDir.resolve("recover.mv.db");
        Files.writeString(source, "database payload");
        UiSessionContext context = new UiSessionContext();

        AlternateDatabaseAdminService.AdminResult result = successfulRepairService(context)
            .repairDatabase(source, false, true);

        assertTrue(context.isDatabaseOpen());
        assertEquals(tempDir.resolve("recover").toAbsolutePath().normalize(), context.activeDatabaseBasePath());
        assertEquals(tempDir.resolve("recover-recovered.sql"), result.resultPath());
        assertNotNull(result.openResult());
    }

    private AlternateDatabaseAdminService service(UiSessionContext context)
    {
        return new AlternateDatabaseAdminService(new FakeDatabaseAdministrationService(context), context);
    }

    private AlternateDatabaseAdminService failingRepairService(UiSessionContext context)
    {
        return new AlternateDatabaseAdminService(new FakeDatabaseAdministrationService(context), context)
        {
            @Override
            nonprofitbookkeeping.tools.H2SchemaMigrator.RepairResult repairCorruptedDatabase(Path basePath) throws Exception
            {
                throw new IOException("simulated repair failure");
            }
        };
    }

    private AlternateDatabaseAdminService successfulRepairService(UiSessionContext context)
    {
        return new AlternateDatabaseAdminService(new FakeDatabaseAdministrationService(context), context)
        {
            @Override
            nonprofitbookkeeping.tools.H2SchemaMigrator.RepairResult repairCorruptedDatabase(Path basePath)
            {
                return new nonprofitbookkeeping.tools.H2SchemaMigrator.RepairResult(
                    tempDir.resolve("recover-recovered.sql"), java.util.List.of(tempDir.resolve("recover.mv.db.bak")));
            }
        };
    }

    private static class FakeDatabaseAdministrationService extends UiServiceProvider.DatabaseAdministrationService
    {
        private final UiSessionContext context;

        FakeDatabaseAdministrationService(UiSessionContext context)
        {
            super(context, null);
            this.context = context;
        }

        @Override
        public DatabaseOpenService.OpenResult openDatabase(Path basePath)
        {
            Path normalized = basePath.toAbsolutePath().normalize();
            context.openDatabase(normalized);
            return new DatabaseOpenService.OpenResult(normalized, null);
        }

        @Override
        public void closeDatabase()
        {
            context.clearDatabase();
        }
    }
}
