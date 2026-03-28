package nonprofitbookkeeping.ui.bootstrap;

import javafx.stage.Stage;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.ui.MainApplicationView;
import nonprofitbookkeeping.ui.ThemeManager;
import nonprofitbookkeeping.util.FormatUtils;
import org.nonprofitbookkeeping.model.UiThemePreference;

import java.io.IOException;
import java.util.Locale;

/**
 * Coordinates startup-time settings loading and application.
 */
public class SettingsStartupCoordinator
{
    private final SettingsService settingsService;
    private final SettingsInitializationService initializationService;
    private boolean settingsLoaded;

    public SettingsStartupCoordinator(SettingsService settingsService,
        SettingsInitializationService initializationService)
    {
        this.settingsService = settingsService;
        this.initializationService = initializationService;
    }

    public void ensureSettingsLoaded(Stage stage, MainApplicationView mainView)
        throws IOException
    {
        settingsLoaded = initializationService.ensureLoaded(settingsLoaded,
            settingsService, () -> applyGlobalSettings(stage, mainView));
    }

    public void applyGlobalSettings(Stage stage, MainApplicationView mainView)
    {
        SettingsModel settings = settingsService.getSettings();
        applySettings(stage, settings);
        applyTitle(stage, settings);
        applyAccountDetailsDefaults(mainView, settings);
    }

    public void applySettings(Stage stage, SettingsModel settings)
    {
        if (settings == null)
        {
            return;
        }

        Locale locale = settings.getLanguage() != null &&
            !settings.getLanguage().isBlank() ?
                Locale.forLanguageTag(settings.getLanguage()) :
                Locale.getDefault();
        FormatUtils.configureLocale(locale, settings.getDefaultCurrency());
        FormatUtils.setCurrencyFormat(settings.getCurrencyFormat());

        if (stage != null && stage.getScene() != null)
        {
            UiThemePreference themePreference =
                UiThemePreference.fromStoredValue(settings.getTheme());
            ThemeManager.applyTheme(stage.getScene(),
                toThemeManagerValue(themePreference));
        }
    }

    private void applyTitle(Stage stage, SettingsModel settings)
    {
        if (stage == null || settings == null)
        {
            return;
        }

        String title = settings.getOrganizationName();
        stage.setTitle(title != null && !title.isBlank() ?
            title + " - Nonprofit Bookkeeping" : "Nonprofit Bookkeeping");
    }

    private void applyAccountDetailsDefaults(MainApplicationView mainView,
        SettingsModel settings)
    {
        if (mainView == null || settings == null)
        {
            return;
        }

        mainView.applyAccountDetailsDefaults(
            settingsService.resolveDefaultReportPeriod(),
            settingsService.resolveFiscalYearStart(),
            settings.isEnableYearToDateOption(),
            settings.isEnableFullYearOption(),
            settings.isEnableLastMonthOption());
    }

    private String toThemeManagerValue(UiThemePreference themePreference)
    {
        return switch (themePreference)
        {
            case DARK -> "dark";
            case LIGHT -> "light";
            case SYSTEM_DEFAULT -> "system";
        };
    }
}
