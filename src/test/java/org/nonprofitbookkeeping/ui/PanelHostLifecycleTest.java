package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicInteger;

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


    @Test
    void showRendersPanelNodeAndCachesPanelInstance()
    {
        AtomicInteger createCalls = new AtomicInteger();
        javafx.scene.layout.StackPane dashboardNode = new javafx.scene.layout.StackPane();
        PanelHost host = new PanelHost(id -> {
            if (id == AppPanelId.DASHBOARD)
            {
                createCalls.incrementAndGet();
                return new FxAppPanelAdapter<>("Dashboard", () -> dashboardNode);
            }
            throw new IllegalArgumentException("Unexpected panel " + id);
        });

        host.show(AppPanelId.DASHBOARD);
        assertSame(dashboardNode, host.getCenter());

        host.show(AppPanelId.DASHBOARD);
        assertSame(dashboardNode, host.getCenter());
        assertEquals(1, createCalls.get());
    }

    @Test
    void adapterLifecycleHooksDelegateToFxPanelCallbacks()
    {
        AtomicInteger saveCalls = new AtomicInteger();
        AtomicInteger newCalls = new AtomicInteger();
        javafx.scene.layout.Pane node = new javafx.scene.layout.Pane();
        PanelHost host = new PanelHost(id -> new FxAppPanelAdapter<>("Dashboard",
            () -> node,
            n -> saveCalls.incrementAndGet(),
            n -> newCalls.incrementAndGet()));

        host.show(AppPanelId.DASHBOARD);
        host.saveActive();
        host.newItemActive();

        assertEquals(1, saveCalls.get());
        assertEquals(1, newCalls.get());
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
