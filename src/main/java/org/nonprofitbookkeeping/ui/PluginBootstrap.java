package org.nonprofitbookkeeping.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.core.ApplicationContextImpl;
import nonprofitbookkeeping.plugin.Plugin;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.service.ReportConfigurationService;
import nonprofitbookkeeping.service.ReportService;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootstraps legacy plugin discovery for the B-shell startup path.
 */
final class PluginBootstrap
{
    private final Stage primaryStage;
    private final MenuBar menuBar;
    private final Menu pluginsMenu = new Menu("Plugins");
    private final List<Plugin> loadedPlugins = new ArrayList<>();

    private PluginBootstrap(Stage primaryStage, MenuBar menuBar)
    {
        this.primaryStage = primaryStage;
        this.menuBar = menuBar;
    }

    static PluginBootstrap initialize(Stage primaryStage, MenuBar menuBar)
    {
        PluginBootstrap bootstrap = new PluginBootstrap(primaryStage, menuBar);
        bootstrap.loadPlugins();
        return bootstrap;
    }

    private void loadPlugins()
    {
        if (!menuBar.getMenus().contains(pluginsMenu))
        {
            menuBar.getMenus().add(pluginsMenu);
        }

        ApplicationContext context = buildContext();
        List<Plugin> candidates = List.of(
                new SCALedgerPlugin(),
                new nonprofitbookkeeping.plugins.sample.SamplePlugin());

        for (Plugin plugin : candidates)
        {
            try
            {
                plugin.initialize(context);
                loadedPlugins.add(plugin);
                addPluginInfoMenuItem(plugin);
                plugin.addMenuItems(menuBar);
            }
            catch (Exception ex)
            {
                MenuItem failed = new MenuItem(plugin.getName() + " (failed)");
                failed.setDisable(true);
                pluginsMenu.getItems().add(failed);
            }
        }

        if (loadedPlugins.isEmpty())
        {
            MenuItem none = new MenuItem("No plugins available");
            none.setDisable(true);
            pluginsMenu.getItems().add(none);
        }
    }

    private ApplicationContext buildContext()
    {
        return new ApplicationContextImpl(
                primaryStage,
                menuBar,
                new ReportService(),
                new ReportConfigurationService(),
                new InventoryService(),
                new DocumentStorageService(),
                new FundAccountingService());
    }

    private void addPluginInfoMenuItem(Plugin plugin)
    {
        MenuItem item = new MenuItem(plugin.getName());
        item.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(primaryStage);
            alert.setTitle("Plugin Information");
            alert.setHeaderText(plugin.getName());
            String description = plugin.getDescription() == null || plugin.getDescription().isBlank()
                    ? "No description available."
                    : plugin.getDescription();
            alert.setContentText(description);
            alert.showAndWait();
        });
        pluginsMenu.getItems().add(item);
    }
}
