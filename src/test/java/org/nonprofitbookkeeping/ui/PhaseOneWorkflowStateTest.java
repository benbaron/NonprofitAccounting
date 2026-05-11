package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.persistence.CompanyRepository;

class PhaseOneWorkflowStateTest
{
    @Test
    void databaseOpenThenPanelSwitchPersistsActivePanelBeforeNextPanelShows() throws Exception
    {
        RecordingSwitcher switcher = new RecordingSwitcher();
        AlternateDataContextService context = new AlternateDataContextService(
            new CompanyRepository(),
            new AlternateRecentsStore(new InMemoryStore()),
            switcher);

        context.openDatabase(Path.of("/tmp/phase1-workflow.mv.db"));
        assertTrue(context.isDatabaseOpen());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeCompanyId());

        TrackingPanel coa = new TrackingPanel();
        TrackingPanel ledger = new TrackingPanel();
        PanelHost host = new PanelHost(id -> switch (id)
        {
            case CHART_OF_ACCOUNTS -> coa;
            case LEDGER_REGISTER -> ledger;
            default -> throw new IllegalArgumentException("Unexpected id " + id);
        });

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        coa.markDirty();
        host.show(AppPanelId.LEDGER_REGISTER);

        assertEquals(1, coa.saveCalls);
        assertEquals(0, ledger.saveCalls);
        assertEquals(Path.of("/tmp/phase1-workflow").toAbsolutePath().normalize(), switcher.lastOpenedBasePath);
    }

    private static final class RecordingSwitcher extends AlternateDatabaseContextSwitcher
    {
        private Path lastOpenedBasePath;

        @Override
        void openDatabase(Path basePath)
        {
            this.lastOpenedBasePath = basePath;
        }
    }

    private static final class InMemoryStore implements AlternateDataContextService.PreferencesStore
    {
        private final java.util.Map<String, String> values = new java.util.HashMap<>();

        @Override
        public String get(String key, String defaultValue)
        {
            return values.getOrDefault(key, defaultValue);
        }

        @Override
        public void put(String key, String value)
        {
            values.put(key, value);
        }
    }

    private static final class TrackingPanel implements AppPanel, PanelHost.DirtyAwarePanel
    {
        private int saveCalls;
        private boolean dirty;

        void markDirty()
        {
            dirty = true;
        }

        @Override
        public String title()
        {
            return "Tracking";
        }

        @Override
        public javafx.scene.Node root()
        {
            return new javafx.scene.layout.VBox();
        }

        @Override
        public void onSave()
        {
            saveCalls++;
            dirty = false;
        }

        @Override
        public boolean isDirty()
        {
            return dirty;
        }
    }
}
