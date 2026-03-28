package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

class MainWindowMenuBarTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void menuBarCopiesOldUiTopLevelOrderAndIncludesWiredItems() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindow window = new MainWindow();
                VBox top = (VBox) window.getTop();
                MenuBar menuBar = (MenuBar) top.getChildren().get(0);

                List<String> menuNames = menuBar.getMenus().stream()
                    .map(Menu::getText)
                    .collect(Collectors.toList());

                assertEquals(List.of("File", "Edit", "Search", "View", "Run", "Tools", "Account", "Help"),
                    menuNames);

                Menu file = menuBar.getMenus().get(0);
                assertTrue(hasItem(file, "New"));
                assertTrue(hasItem(file, "Save"));
                assertTrue(hasItem(file, "Exit"));

                Menu tools = menuBar.getMenus().get(5);
                assertTrue(hasItem(tools, "Import CoA CSV…"));
                assertTrue(hasItem(tools, "Import / Export Jobs…"));

                Menu account = menuBar.getMenus().get(6);
                assertTrue(hasItem(account, "Log In…"));
                assertTrue(hasItem(account, "Company Wizard…"));

                for (Menu menu : menuBar.getMenus())
                {
                    for (MenuItem item : menu.getItems())
                    {
                        if (!(item instanceof javafx.scene.control.SeparatorMenuItem))
                        {
                            assertNotNull(item.getOnAction(),
                                "Menu item should be wired: " + menu.getText() + " -> " + item.getText());
                        }
                    }
                }
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

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        if (error[0] != null)
        {
            throw new AssertionError("MainWindow menu test failed", error[0]);
        }
    }

    private static boolean hasItem(Menu menu, String text)
    {
        return menu.getItems().stream().anyMatch(item -> text.equals(item.getText()));
    }
}
