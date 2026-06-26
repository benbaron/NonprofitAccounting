package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;

class NavigationCorrectionsTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void alternateNavigationIncludesCompaniesAndScrollingCommandSections()
        throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] failure = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                AlternateNavigationCustomizer.apply(window);

                Button companies = findButton(window, "Companies");
                assertNotNull(companies);
                assertNotNull(findButton(window, "Open Company"));

                Button commandCenter = findButton(window, "Command Center");
                assertNotNull(commandCenter);
                commandCenter.fire();

                Platform.runLater(() -> {
                    try
                    {
                        assertNotNull(find(window, ScrollPane.class));
                        TitledPane database = find(window, TitledPane.class,
                            "Database");
                        TitledPane company = find(window, TitledPane.class,
                            "Company");
                        assertNotNull(database);
                        assertNotNull(company);
                        assertFalse(database.isExpanded());
                        assertFalse(company.isExpanded());
                        assertTrue(hasExpandedNonAdministrativeSection(window));
                    }
                    catch (Throwable throwable)
                    {
                        failure[0] = throwable;
                    }
                    finally
                    {
                        latch.countDown();
                    }
                });
            }
            catch (Throwable throwable)
            {
                failure[0] = throwable;
                latch.countDown();
            }
        });

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        if (failure[0] != null)
        {
            throw new AssertionError(failure[0]);
        }
    }

    private static boolean hasExpandedNonAdministrativeSection(Node root)
    {
        if (root instanceof TitledPane pane && pane.isExpanded() &&
            !"Database".equals(pane.getText()) &&
            !"Company".equals(pane.getText()))
        {
            return true;
        }
        if (root instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                if (hasExpandedNonAdministrativeSection(child))
                {
                    return true;
                }
            }
        }
        if (root instanceof ScrollPane pane && pane.getContent() != null)
        {
            return hasExpandedNonAdministrativeSection(pane.getContent());
        }
        if (root instanceof TitledPane pane && pane.getContent() != null)
        {
            return hasExpandedNonAdministrativeSection(pane.getContent());
        }
        return false;
    }

    private static Button findButton(Node root, String text)
    {
        if (root instanceof Button button && button.getText() != null &&
            button.getText().contains(text))
        {
            return button;
        }
        if (root instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                Button found = findButton(child, text);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }

    private static <T extends Node> T find(Node root, Class<T> type)
    {
        return find(root, type, null);
    }

    private static <T extends Node> T find(Node root, Class<T> type,
        String titledPaneText)
    {
        if (type.isInstance(root) &&
            (titledPaneText == null || root instanceof TitledPane pane &&
                titledPaneText.equals(pane.getText())))
        {
            return type.cast(root);
        }
        if (root instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                T found = find(child, type, titledPaneText);
                if (found != null)
                {
                    return found;
                }
            }
        }
        if (root instanceof ScrollPane pane && pane.getContent() != null)
        {
            return find(pane.getContent(), type, titledPaneText);
        }
        if (root instanceof TitledPane pane && pane.getContent() != null)
        {
            return find(pane.getContent(), type, titledPaneText);
        }
        return null;
    }
}
