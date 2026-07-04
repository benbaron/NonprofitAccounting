package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;

class PanelHostLifecycleTest
{
    @BeforeAll
    static void initToolkit() throws Exception
    {
        System.setProperty("testfx.toolkit", "glass");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.es2", "false");
        CountDownLatch latch = new CountDownLatch(1);
        try
        {
            Platform.startup(latch::countDown);
        }
        catch (IllegalStateException alreadyStarted)
        {
            latch.countDown();
        }
        if (!latch.await(30, TimeUnit.SECONDS))
        {
            throw new AssertionError("Timed out starting JavaFX toolkit");
        }
    }

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
        SaveResult result = host.show(AppPanelId.LEDGER_REGISTER);

        assertEquals(SaveResult.Status.SAVED, result.status());
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

        Map<?, ?> cachedPanels = panels(host);
        assertEquals(1, cachedPanels.size());
        assertTrue(cachedPanels.containsKey(AppPanelId.DASHBOARD));
    }


    @Test
    void panelHostDelegatesDeleteAndCancelToActivePanel()
    {
        TrackingPanel panel = new TrackingPanel("COA");
        PanelHost host = new PanelHost(id -> panel);

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        host.deleteActive();
        host.cancelActive();

        assertEquals(1, panel.deleteCalls);
        assertEquals(1, panel.cancelCalls);
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
        SaveResult result = host.saveActive();
        host.newItemActive();

        assertEquals(SaveResult.Status.SAVED, result.status());
        assertEquals(1, saveCalls.get());
        assertEquals(1, newCalls.get());
    }

    @Test
    void saveActiveReportsNoChangesForCleanBackwardCompatiblePanel()
    {
        TrackingPanel panel = new TrackingPanel("COA");
        PanelHost host = new PanelHost(id -> panel);

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        SaveResult result = host.saveActive();

        assertEquals(SaveResult.Status.NO_CHANGES, result.status());
        assertEquals(0, panel.saveCalls);
    }

    @Test
    void saveActiveReportsUnsupportedFromSaveAwarePanel()
    {
        AppPanel panel = new UnsupportedSavePanel();
        PanelHost host = new PanelHost(id -> panel);

        host.show(AppPanelId.SETTINGS);
        SaveResult result = host.saveActive();

        assertEquals(SaveResult.Status.UNSUPPORTED, result.status());
    }

    @Test
    void saveActiveReportsFailedAndKeepsPanelWhenSaveThrows()
    {
        TrackingPanel failing = new TrackingPanel("Failing");
        failing.failSave = true;
        TrackingPanel next = new TrackingPanel("Next");
        PanelHost host = new PanelHost(id -> id == AppPanelId.CHART_OF_ACCOUNTS ? failing : next);

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        failing.markDirty();
        SaveResult result = host.show(AppPanelId.LEDGER_REGISTER);

        assertEquals(SaveResult.Status.FAILED, result.status());
        assertSame(failing.root(), host.getCenter());
        assertEquals("Failing", host.getActiveTitle());
    }

    @Test
    void switchingIsBlockedWhenActivePanelCannotNavigateAway()
    {
        GuardedPanel guarded = new GuardedPanel("Admin Operation");
        TrackingPanel next = new TrackingPanel("Next");
        PanelHost host = new PanelHost(id -> id == AppPanelId.CHART_OF_ACCOUNTS ? guarded : next);

        host.show(AppPanelId.CHART_OF_ACCOUNTS);
        SaveResult result = host.show(AppPanelId.LEDGER_REGISTER);

        assertEquals(SaveResult.Status.FAILED, result.status());
        assertFalse(guarded.saveAttempted);
        assertSame(guarded.root(), host.getCenter());
    }

    @Test
    void defaultFactoryHostsClassicSettingsPanelInAlternateChrome()
    {
        PanelHost host = new PanelHost(new UiServiceProvider(new AlternateDataContextService()));

        host.show(AppPanelId.SETTINGS);

        assertTrue(host.getCenter() instanceof javafx.scene.layout.BorderPane);
        assertEquals(SaveResult.Status.UNSUPPORTED, host.saveActive().status());
    }


    @SuppressWarnings("unchecked")
    private static Map<AppPanelId, AppPanel> panels(PanelHost host)
    {
        try
        {
            Field field = PanelHost.class.getDeclaredField("panels");
            field.setAccessible(true);
            return (Map<AppPanelId, AppPanel>) field.get(host);
        }
        catch (ReflectiveOperationException ex)
        {
            throw new AssertionError("Unable to read PanelHost panels cache", ex);
        }
    }

    private static class TrackingPanel implements AppPanel, PanelHost.DirtyAwarePanel
    {
        private final String title;
        private boolean dirty;
        private int saveCalls;
        private int deleteCalls;
        private int cancelCalls;
        private boolean failSave;
        private final javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();

        private TrackingPanel(String title)
        {
            this.title = title;
        }

        void markDirty()
        {
            this.dirty = true;
        }

        @Override
        public String title()
        {
            return this.title;
        }

        @Override
        public javafx.scene.Node root()
        {
            return this.root;
        }

        @Override
        public void onSave()
        {
            if (this.failSave)
            {
                throw new IllegalStateException("boom");
            }
            this.saveCalls++;
            this.dirty = false;
        }

        @Override
        public void onDelete()
        {
            this.deleteCalls++;
        }

        @Override
        public void onCancel()
        {
            this.cancelCalls++;
        }

        @Override
        public boolean isDirty()
        {
            return this.dirty;
        }
    }

    private static final class UnsupportedSavePanel implements AppPanel, AppPanel.SaveAware
    {
        @Override public String title() { return "Unsupported"; }
        @Override public javafx.scene.Node root() { return new javafx.scene.layout.VBox(); }
        @Override public SaveResult save() { return SaveResult.unsupported("No persistence."); }
    }

    private static final class GuardedPanel extends TrackingPanel implements PanelHost.NavigationGuardPanel
    {
        private boolean saveAttempted;

        private GuardedPanel(String title)
        {
            super(title);
        }

        @Override
        public void onSave()
        {
            this.saveAttempted = true;
            super.onSave();
        }

        @Override
        public boolean canNavigateAway()
        {
            return false;
        }
    }
}
