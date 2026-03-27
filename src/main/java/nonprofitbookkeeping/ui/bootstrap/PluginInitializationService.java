package nonprofitbookkeeping.ui.bootstrap;

import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.plugin.Plugin;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes and tracks legacy plugins.
 */
public class PluginInitializationService
{
    public List<Plugin> initialize(List<Plugin> pluginsToLoad,
        ApplicationContext context,
        Logger logger,
        PluginErrorHandler errorHandler)
    {
        List<Plugin> loaded = new ArrayList<>();
        for (Plugin plugin : pluginsToLoad)
        {
            try
            {
                logger.info("Initializing plugin: {} - {}", plugin.getName(), plugin.getDescription());
                plugin.initialize(context);
                loaded.add(plugin);
                logger.info("Plugin initialized successfully: {}", plugin.getName());
            }
            catch (Exception e)
            {
                logger.error("Failed to initialize plugin: {} - {}", plugin.getClass().getName(), e.getMessage(), e);
                errorHandler.onPluginInitError(plugin, e);
            }
        }
        logger.info("Plugin discovery complete. Loaded {} plugins.", loaded.size());
        return loaded;
    }

    @FunctionalInterface
    public interface PluginErrorHandler
    {
        void onPluginInitError(Plugin plugin, Exception exception);
    }
}
