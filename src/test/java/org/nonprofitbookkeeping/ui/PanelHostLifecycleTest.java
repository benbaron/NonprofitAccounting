package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PanelHostLifecycleTest
{
    @Test
    void switchingBetweenPhaseOnePanelsSavesActivePanelBeforeShow()
    {
        TrackingPanel coa = new TrackingPanel("COA");
        TrackingPanel ledger = new TrackingPanel("Ledger");
        PanelHost host = new PanelHost(id -> switch (id)
        {
            case CHART_OF_ACCOUNTS -> coa;
            case LEDGER_REGISTER -> ledger;
            default -> throw new IllegalArgumentException("Unexpected panel " + id);
        });

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        coa.markDirty();
        host.show(AppPanelId.LEDGER_REGISTER);

        assertEquals(1, coa.saveCalls);
        assertEquals(0, ledger.saveCalls);
    }

    private static final class TrackingPanel implements AppPanel, PanelHost.DirtyAwarePanel
    {
        private final String title;
        private boolean dirty;
        private int saveCalls;

        private TrackingPanel(String title)
        {
            this.title = title;
        }

        void markDirty()
        {
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
