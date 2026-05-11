package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChartOfAccountsPanelMigrationEquivalenceTest
{
    @Test
    void chartOfAccountsUsesSharedPanelInstanceAndPersistsContextAcrossRouteSwitches()
    {
        TrackingPanel chartOfAccounts = new TrackingPanel("Chart of Accounts");
        TrackingPanel ledger = new TrackingPanel("Ledger Register");

        PanelHost host = new PanelHost(id -> switch (id)
        {
            case CHART_OF_ACCOUNTS -> chartOfAccounts;
            case LEDGER_REGISTER -> ledger;
            default -> throw new IllegalArgumentException("Unexpected panel " + id);
        });

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        chartOfAccounts.mutate("draft-account");

        host.show(AppPanelId.LEDGER_REGISTER);
        assertEquals(1, chartOfAccounts.saveCalls);
        assertEquals("draft-account", chartOfAccounts.contextToken);

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        assertSame(chartOfAccounts.rootNode, host.getCenter());
        assertEquals("draft-account", chartOfAccounts.contextToken);
        assertEquals("Chart of Accounts", host.getActiveTitle());
        assertTrue(!chartOfAccounts.isDirty());
    }

    private static final class TrackingPanel implements AppPanel, PanelHost.DirtyAwarePanel
    {
        private final String title;
        private final javafx.scene.layout.VBox rootNode = new javafx.scene.layout.VBox();
        private int saveCalls;
        private boolean dirty;
        private String contextToken;

        private TrackingPanel(String title)
        {
            this.title = title;
        }

        void mutate(String token)
        {
            contextToken = token;
            dirty = true;
        }

        @Override
        public String title()
        {
            return title;
        }

        @Override
        public javafx.scene.Node root()
        {
            return rootNode;
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
