package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

class AlternateNavigationCustomizerTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void buildsOrderedThreeSectionNavigationAndCollapsesToIcons()
        throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                MainApp.removeAlternateHeaderRangeControls(window);
                AlternateNavigationCustomizer.apply(window);

                SplitPane split = assertInstanceOf(SplitPane.class,
                    window.getCenter());
                VBox navigation = assertInstanceOf(VBox.class,
                    split.getItems().get(0));
                assertTrue(navigation.getMinWidth() <= 58.0);

                VBox primary = assertInstanceOf(VBox.class,
                    navigation.getChildren().get(1));
                assertEquals(List.of(
                    "Profile",
                    "Dashboard",
                    "Search",
                    "Settings",
                    "Command Center"), accessibleTitles(primary));

                TitledPane accounting = assertInstanceOf(TitledPane.class,
                    navigation.getChildren().get(2));
                assertEquals("Accounting", accounting.getText());
                assertEquals(List.of(
                    "Chart of Accounts",
                    "Journal",
                    "Ledger"),
                    accessibleTitles(accounting.getContent()));

                TitledPane utilities = assertInstanceOf(TitledPane.class,
                    navigation.getChildren().get(3));
                assertEquals("Utilities", utilities.getText());
                assertEquals(List.of(
                    "Open Database",
                    "Open Company",
                    "Import & Tools"),
                    accessibleTitles(utilities.getContent()));

                assertEquals(1, countTitle(navigation, "Dashboard"));

                AlternateNavigationCustomizer.applyCollapsedState(
                    navigation, true);
                for (Button button : buttons(navigation))
                {
                    assertNotNull(button.getTooltip());
                    assertEquals(button.getAccessibleText(),
                        button.getTooltip().getText());
                    assertTrue(!button.getText().contains(
                        button.getAccessibleText()));
                }
                assertEquals("▤", accounting.getText());
                assertEquals("⋮", utilities.getText());
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

    private static List<String> accessibleTitles(Node root)
    {
        return buttons(root).stream()
            .map(Button::getAccessibleText)
            .toList();
    }

    private static long countTitle(Node root, String title)
    {
        return buttons(root).stream()
            .filter(button -> title.equals(button.getAccessibleText()))
            .count();
    }

    private static List<Button> buttons(Node root)
    {
        java.util.ArrayList<Button> result = new java.util.ArrayList<>();
        collectButtons(root, result);
        return result;
    }

    private static void collectButtons(Node node, List<Button> result)
    {
        if (node instanceof Button button)
        {
            result.add(button);
        }
        if (node instanceof javafx.scene.Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                collectButtons(child, result);
            }
        }
        if (node instanceof TitledPane pane && pane.getContent() != null &&
            !pane.getChildrenUnmodifiable().contains(pane.getContent()))
        {
            collectButtons(pane.getContent(), result);
        }
    }
}
