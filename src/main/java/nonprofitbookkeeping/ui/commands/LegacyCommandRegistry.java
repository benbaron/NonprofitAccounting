package nonprofitbookkeeping.ui.commands;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for legacy command handlers so menu construction can remain declarative.
 */
public class LegacyCommandRegistry
{
    private final Map<String, Runnable> handlers = new LinkedHashMap<>();

    public LegacyCommandRegistry register(String commandId, Runnable handler)
    {
        if (commandId == null || commandId.isBlank())
        {
            throw new IllegalArgumentException("commandId must not be blank");
        }
        if (handler == null)
        {
            throw new IllegalArgumentException("handler must not be null");
        }
        handlers.put(commandId, handler);
        return this;
    }

    public Runnable resolve(String commandId)
    {
        Runnable handler = handlers.get(commandId);
        if (handler == null)
        {
            throw new IllegalArgumentException("No command registered for id: " + commandId);
        }
        return handler;
    }
}
