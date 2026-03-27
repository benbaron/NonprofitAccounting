package nonprofitbookkeeping.ui.bootstrap;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.service.SettingsService;

import java.io.IOException;

/**
 * Isolates startup-time settings initialization concerns.
 */
public class SettingsInitializationService
{
    public boolean ensureLoaded(boolean alreadyLoaded,
        SettingsService settingsService,
        Runnable onApplied) throws IOException
    {
        if (alreadyLoaded || !Database.isInitialized())
        {
            return alreadyLoaded;
        }

        settingsService.loadSettings(null);
        onApplied.run();
        return true;
    }
}
