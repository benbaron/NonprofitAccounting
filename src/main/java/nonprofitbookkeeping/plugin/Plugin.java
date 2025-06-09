package nonprofitbookkeeping.plugin;

import nonprofitbookkeeping.core.ApplicationContext;
import javafx.scene.control.MenuBar;

/**
 * Defines the contract for plugins in the nonprofit bookkeeping application.
 * Plugins can extend the application's functionality, for example, by adding
 * new menu items, providing new features, or integrating with external services.
 */
public interface Plugin {
    /**
     * Gets the name of the plugin.
     * This name may be displayed in the UI or used for identification.
     * @return The name of the plugin.
     */
    String getName();

    /**
     * Gets a brief description of what the plugin does.
     * This description can be used in a plugin manager UI or for help purposes.
     * @return A string describing the plugin's functionality.
     */
    String getDescription();

    /**
     * Initializes the plugin with the application context.
     * This method is called once when the plugin is loaded by the application.
     * Plugins can use the application context to access core application services
     * and UI components like the primary stage.
     *
     * @param context The {@link ApplicationContext} providing access to core application functionalities.
     * @throws Exception if an error occurs during plugin initialization.
     */
    void initialize(ApplicationContext context) throws Exception;

    /**
     * Allows the plugin to add custom menu items to the application's main menu bar.
     * This method is called after {@link #initialize(ApplicationContext)} and provides
     * the plugin with a reference to the main {@link MenuBar}.
     *
     * @param mainMenuBar The main {@link MenuBar} of the application to which custom menus or items can be added.
     */
    void addMenuItems(MenuBar mainMenuBar);

    /**
     * Called when the application is shutting down or when the plugin is being unloaded.
     * Plugins should use this method to perform any necessary cleanup, such as releasing resources,
     * saving state, or closing connections.
     */
    void shutdown();
}
