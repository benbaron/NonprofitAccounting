package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
import javafx.stage.Stage;

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
                MainWindow window = new MainWindow(Stage::new);
                VBox top = (VBox) window.getTop();
                MenuBar menuBar = (MenuBar) top.getChildren().get(0);

                List<String> menuNames = menuBar.getMenus().stream()
                    .map(Menu::getText)
                    .collect(Collectors.toList());

                assertEquals(List.of("File", "Edit", "Run", "Database", "Reports", "Fundraising", "Settings", "Plugins", "Help"),
                    menuNames);

                Menu file = menuBar.getMenus().get(0);
                assertTrue(hasItem(file, "Open Company"));
                assertTrue(hasItem(file, "Import Outlands Ledger..."));
                assertTrue(hasItem(file, "Import SCA Ledger..."));
                assertTrue(hasItem(file, "Save Modified SCA Workbook..."));

                Menu edit = menuBar.getMenus().get(1);
                assertTrue(hasItem(edit, "Create or Edit Company"));

                Menu database = menuBar.getMenus().get(3);
                assertTrue(hasItem(database, "Open/Create H2 DB..."));
                assertTrue(hasItem(database, "Export DB to H2 script..."));
                assertTrue(hasItem(database, "Migrate H2 DB to current level..."));
                assertTrue(hasItem(database, "Run SQL Query..."));

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

    @Test
    void hasLoadedScaWorkbookReflectsPluginFileState() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindow window = new MainWindow(Stage::new);
                assertTrue(!window.hasLoadedScaWorkbook());

                window.getScaLedgerPluginForTest()
                    .setCurrentScaFile(new File("demo.xlsm"));

                assertTrue(window.hasLoadedScaWorkbook());
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
            throw new AssertionError("MainWindow SCA workbook state test failed", error[0]);
        }
    }

    private static boolean hasItem(Menu menu, String text)
    {
        return menu.getItems().stream().anyMatch(item -> text.equals(item.getText()));
    }
}
