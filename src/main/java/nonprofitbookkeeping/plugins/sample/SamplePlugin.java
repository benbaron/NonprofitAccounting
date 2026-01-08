
package nonprofitbookkeeping.plugins.sample;

import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.plugin.Plugin;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sample plugin implementation to demonstrate and test the plugin framework.
 * This plugin adds a "Show Sample Plugin Alert" item to the "Tools" menu
 * (or creates a "Tools" menu if one doesn't exist). Clicking this item
 * displays an informational alert.
 */
public class SamplePlugin implements Plugin
{
	
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SamplePlugin.class);
	private ApplicationContext applicationContext;
	
	/**
	 * {@inheritDoc}
	 * @return The name "Sample Test Plugin".
	 */
	@Override
	public String getName()
	{
		return "Sample Test Plugin";
		
	}
	
	/**
	 * {@inheritDoc}
	 * @return A description indicating this is a simple plugin for demonstration and testing.
	 */
	@Override
	public String getDescription()
	{
		return "A simple plugin to demonstrate and test the plugin framework functionality.";
		
	}
	
	/**
	 * {@inheritDoc}
	 * This implementation stores the provided {@link ApplicationContext} for later use
	 * and logs the initialization event.
	 * @param context The application context provided by the main application.
	 * @throws Exception If any error occurs during initialization (not thrown by this sample).
	 */
	@Override
	public void initialize(ApplicationContext context) throws Exception
	{
		this.applicationContext = context;
		LOGGER.info(
			getName() + " initialized successfully with context: " + context);
		
		// You can test accessing context here, e.g.:
		// LOGGER.info("Primary stage from context: " +
		// (context.getPrimaryStage() != null));
	}
	
	/**
	 * {@inheritDoc}
	 * This implementation adds a "Show Sample Plugin Alert" menu item to a "Tools" menu.
	 * If a "Tools" menu does not exist, it creates one. The new menu item, when clicked,
	 * shows an alert dialog using the primary stage from the {@link ApplicationContext}.
	 * @param mainMenuBar The main menu bar of the application.
	 */
	@Override
	public void addMenuItems(MenuBar mainMenuBar)
	{
		LOGGER.info("SamplePlugin: Adding menu items...");
		
		// Option 1: Find an existing menu (e.g., "Tools" or "Run") and add to
		// it.
		// Option 2: Create a new top-level menu (less preferred for simple
		// plugins).
		// Let's try to find or create a "Tools" menu.
		
		Menu toolsMenu = null;
		
		for (Menu menu : mainMenuBar.getMenus())
		{
			
			if ("Tools".equals(menu.getText()))
			{
				toolsMenu = menu;
				break;
			}
			
		}
		
		if (toolsMenu == null)
		{
			toolsMenu = new Menu("Tools");
			// Try to add before "Help" menu for conventional placement
			Menu helpMenu = null;
			int helpMenuIndex = -1;
			
			for (int i = 0; i < mainMenuBar.getMenus().size(); i++)
			{
				
				if ("Help".equals(mainMenuBar.getMenus().get(i).getText()))
				{
					helpMenu = mainMenuBar.getMenus().get(i);
					helpMenuIndex = i;
					break;
				}
				
			}
			
			if (helpMenu != null)
			{
				mainMenuBar.getMenus().add(helpMenuIndex, toolsMenu);
			}
			else
			{
				mainMenuBar.getMenus().add(toolsMenu); // Add to end if no Help
														// menu
			}
			
		}
		
		MenuItem sampleActionItem = new MenuItem("Show Sample Plugin Alert");
		sampleActionItem.setOnAction(e -> {
			
			if (this.applicationContext != null &&
				this.applicationContext.getPrimaryStage() != null)
			{
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.initOwner(this.applicationContext.getPrimaryStage());
				alert.setTitle(getName());
				alert.setHeaderText("Plugin Action Executed!");
				alert.setContentText(
					getDescription() + "\nApplicationContext is available.");
				alert.showAndWait();
			}
			else
			{
				LOGGER.warn(
					"ApplicationContext or PrimaryStage not available for SamplePlugin action.");
				// Fallback log when context/stage is null
				LOGGER.info(
					"{}: Action executed, but UI context was not available for Alert.",
					getName());
			}
			
		});
		toolsMenu.getItems().add(sampleActionItem);
		LOGGER.info(
			"SamplePlugin: 'Show Sample Plugin Alert' menu item added to Tools menu.");
		
	}
	
	/**
	 * {@inheritDoc}
	 * This implementation logs the shutdown event and releases its reference to the
	 * {@link ApplicationContext}.
	 */
	@Override
	public void shutdown()
	{
		LOGGER.info(getName() + " shutting down.");
		this.applicationContext = null; // Release context
		
	}
	
}
