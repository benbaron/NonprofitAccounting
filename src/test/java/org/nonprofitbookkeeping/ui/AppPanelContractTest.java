package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

class AppPanelContractTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel(); // Initializes JavaFX toolkit in test JVM
    }

    static Stream<AppPanelId> panelIds()
    {
        return Arrays.stream(AppPanelId.values());
    }

    @ParameterizedTest
    @MethodSource("panelIds")
    void eachPanelIdResolvesToRenderablePanel(AppPanelId id) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                PanelHost host = new PanelHost();
                host.show(id);

                assertNotNull(host.getCenter(), "Center node should be set for panel " + id);
                assertNotEquals("(none)", host.getActiveTitle(),
                    "Panel title should be populated for panel " + id);
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

        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertNotEquals(false, completed,
            "Timed out waiting for JavaFX panel init for " + id);

        if (error[0] != null)
        {
            throw new AssertionError("Panel contract failed for " + id, error[0]);
        }
    }
}
