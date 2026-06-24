package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;

class MainAppHeaderTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void alternateHeaderKeepsHeadingButRemovesRangeControls()
        throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                HBox header = assertInstanceOf(HBox.class, window.getTop());

                MainApp.removeAlternateHeaderRangeControls(window);

                assertEquals(2, header.getChildren().size());
                assertFalse(containsType(header, ChoiceBox.class));
                assertFalse(containsType(header, DateRangeSelector.class));
            }
            catch (Throwable throwable)
            {
                error[0] = throwable;
            }
            finally
            {
                latch.countDown();
            }
        });

        if (!latch.await(20, TimeUnit.SECONDS))
        {
            throw new IllegalStateException("Timed out waiting for FX task");
        }
        if (error[0] != null)
        {
            throw new AssertionError(error[0]);
        }
    }

    private static boolean containsType(Node node, Class<?> type)
    {
        if (type.isInstance(node))
        {
            return true;
        }
        if (node instanceof javafx.scene.Parent parent)
        {
            return parent.getChildrenUnmodifiable().stream()
                .anyMatch(child -> containsType(child, type));
        }
        return false;
    }
}
