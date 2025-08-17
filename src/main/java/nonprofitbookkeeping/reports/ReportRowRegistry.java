package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import nonprofitbookkeeping.ui.panels.ReportRowPanel;

/**
 * Registry mapping report-row identifiers to their bean classes and
 * UI panel factories. The registry is populated from a configuration
 * file allowing new row types to be discovered without code changes.
 */
public final class ReportRowRegistry
{
    /** Location of the configuration resource on the classpath. */
    private static final String CONFIG_RESOURCE = "/report-rows.properties";

    /** Holder for registry entries. */
    private final Map<String, RowDefinition> rows = new HashMap<>();

    private ReportRowRegistry()
    {
        loadFromConfig();
    }

    /**
     * Load the registry entries from the configuration file.
     */
    private void loadFromConfig()
    {
        try (InputStream in = ReportRowRegistry.class.getResourceAsStream(CONFIG_RESOURCE))
        {
            if (in == null)
            {
                return; // No configuration found.
            }
            Properties props = new Properties();
            props.load(in);
            for (String id : props.stringPropertyNames())
            {
                String value = props.getProperty(id);
                if (value == null)
                    continue;
                String[] parts = value.split(",");
                if (parts.length != 2)
                    continue;
                Class<?> beanClass = Class.forName(parts[0].trim());
                Class<?> panelClass = Class.forName(parts[1].trim());
                rows.put(id.trim(), new RowDefinition(beanClass, () ->
                {
                    try
                    {
                        return (ReportRowPanel) panelClass.getDeclaredConstructor().newInstance();
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Cannot instantiate panel for " + id, e);
                    }
                }));
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            // Configuration errors should not crash the application; log and continue
            e.printStackTrace();
        }
    }

    /**
     * Look up the row definition for a given identifier.
     *
     * @param id report row identifier
     * @return the corresponding {@link RowDefinition} or {@code null} if none registered
     */
    public RowDefinition lookup(String id)
    {
        return this.rows.get(id);
    }

    /**
     * @return available report row identifiers registered
     */
    public Set<String> getIds()
    {
        return this.rows.keySet();
    }

    /** Singleton access */
    public static ReportRowRegistry getInstance()
    {
        return Holder.INSTANCE;
    }

    /** Holder class for lazy-loaded singleton instance. */
    private static class Holder
    {
        private static final ReportRowRegistry INSTANCE = new ReportRowRegistry();
    }

    /**
     * Value object describing a row type.
     */
    public record RowDefinition(Class<?> beanClass, Supplier<? extends ReportRowPanel> panelFactory)
    {
    }
}

