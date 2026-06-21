package nonprofitbookkeeping.ui.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import org.nonprofitbookkeeping.ui.AppPanel;

class OrgAppPanelAdapterTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void wrapsAppPanelAsNodeAndPreservesLifecycleHooks() throws Exception
    {
        runOnFxThread(() -> {
            TrackingPanel panel = new TrackingPanel();
            OrgAppPanelNodeAdapter adapter = new OrgAppPanelNodeAdapter(panel);

            assertSame(panel, adapter.getAppPanel());
            assertSame(panel.root(), adapter.getCenter());

            adapter.onNew();
            adapter.onSave();
            adapter.onCopy();
            adapter.onPaste();

            assertEquals(1, panel.newCalls);
            assertEquals(1, panel.saveCalls);
            assertEquals(1, panel.copyCalls);
            assertEquals(1, panel.pasteCalls);
        });
    }

    @Test
    void createsNonClosableTabWithPanelTitle() throws Exception
    {
        runOnFxThread(() -> {
            TrackingPanel panel = new TrackingPanel();
            Tab tab = OrgAppPanelTabAdapter.toTab(panel);

            assertEquals("Tracking Panel", tab.getText());
            assertFalse(tab.isClosable());
            assertNotNull(tab.getContent());
            assertEquals(OrgAppPanelNodeAdapter.class, tab.getContent().getClass());
        });
    }

    private static void runOnFxThread(FxRunnable runnable) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                runnable.run();
            }
            catch (Throwable t)
            {
                error[0] = t;
            }
            finally
            {
                latch.countDown();
            }
        });

        if (!latch.await(20, TimeUnit.SECONDS))
        {
            throw new IllegalStateException("Timed out waiting for JavaFX work");
        }
        if (error[0] != null)
        {
            throw new AssertionError(error[0]);
        }
    }

    private static final class TrackingPanel implements AppPanel
    {
        private final Label root = new Label("root");
        private int saveCalls;
        private int newCalls;
        private int copyCalls;
        private int pasteCalls;

        @Override
        public String title()
        {
            return "Tracking Panel";
        }

        @Override
        public Node root()
        {
            return this.root;
        }

        @Override
        public void onSave()
        {
            this.saveCalls++;
        }

        @Override
        public void onNew()
        {
            this.newCalls++;
        }

        @Override
        public void onCopy()
        {
            this.copyCalls++;
        }

        @Override
        public void onPaste()
        {
            this.pasteCalls++;
        }
    }

    @FunctionalInterface
    private interface FxRunnable
    {
        void run() throws Exception;
    }
}
