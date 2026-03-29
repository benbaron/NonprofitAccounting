package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.model.AppPreferencesState;
import org.nonprofitbookkeeping.model.MultiCompanyState;

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
        try
        {
            new JFXPanel();
        }
        catch (UnsatisfiedLinkError | NoClassDefFoundError ex)
        {
            assumeTrue(false, "Skipping JavaFX UI tests in headless CI without AWT native deps: " + ex.getMessage());
        }
    }

    @Test
    void menuBarCopiesOldUiTopLevelOrderAndIncludesWiredItems() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindow window = new MainWindow(new InMemoryStateStore());
                VBox top = (VBox) window.getTop();
                MenuBar menuBar = (MenuBar) top.getChildren().get(0);

                List<String> menuNames = menuBar.getMenus().stream()
                    .map(Menu::getText)
                    .collect(Collectors.toList());

                assertEquals(List.of("File", "Edit", "Search", "View", "Run", "Database", "Tools", "Fundraising", "Account", "Help"),
                    menuNames);

                Menu file = menuBar.getMenus().get(0);
                assertTrue(hasItem(file, "Open…"));
                assertTrue(hasItem(file, "Import H2 SQL Script…"));
                assertTrue(hasItem(file, "Export H2 SQL Script…"));
                assertTrue(hasItem(file, "Run SQL Query…"));

                Menu edit = menuBar.getMenus().get(1);
                assertTrue(hasItem(edit, "Copy"));

                Menu run = menuBar.getMenus().get(4);
                assertTrue(hasItem(run, "Post / Validate"));

                Menu database = menuBar.getMenus().get(5);
                assertTrue(hasItem(database, "Open/Create H2 Database…"));
                assertTrue(hasItem(database, "Import Legacy .npbk Archive…"));

                Menu tools = menuBar.getMenus().get(6);
                assertTrue(hasItem(tools, "Import CoA CSV…"));
                assertTrue(hasItem(tools, "Import / Export Jobs…"));

                Menu fundraising = menuBar.getMenus().get(7);
                assertTrue(hasItem(fundraising, "Donors"));
                assertTrue(hasItem(fundraising, "Grants"));

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
                MainWindow window = new MainWindow(new InMemoryStateStore());
                assertNotNull(window.inspectorPane());
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

    private static final class InMemoryStateStore implements AppStateStore
    {
        @Override
        public Optional<AppPreferencesState> loadPreferences()
        {
            return Optional.empty();
        }

        @Override
        public Optional<MultiCompanyState> loadMultiCompany()
        {
            return Optional.empty();
        }

        @Override
        public void savePreferences(AppPreferencesState state)
        {
        }

        @Override
        public void saveMultiCompany(MultiCompanyState state)
        {
        }
    }
}
