package org.nonprofitbookkeeping.ui;

import javafx.stage.Stage;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.ui.bootstrap.SettingsInitializationService;
import nonprofitbookkeeping.ui.bootstrap.SettingsStartupCoordinator;
import nonprofitbookkeeping.util.FormatUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainAppStartupSettingsIntegrationTest
{
    @BeforeAll
    static void setupFx()
    {
        FxTestSupport.initToolkitOrSkip();
    }

    @Test
    void startupAppliesPersistedThemeLocaleAndTitle() throws Exception
    {
        Path dbFile = Files.createTempDirectory("main-app-startup").resolve("ledger");
        Database.init(dbFile);
        Database.get().ensureSchema();

        SettingsService persistedSettings = new SettingsService();
        SettingsModel settingsModel = persistedSettings.getSettings();
        settingsModel.setOrganizationName("Rivendell Relief");
        settingsModel.setLanguage("fr-FR");
        settingsModel.setDefaultCurrency("EUR");
        settingsModel.setCurrencyFormat("¤#,##0.00");
        settingsModel.setTheme("dark");
        persistedSettings.saveSettings(null);

        MainApp app = new MainApp(new SettingsStartupCoordinator(
            new SettingsService(), new SettingsInitializationService()));

        Stage stage = FxTestSupport.onFx(() -> {
            Stage s = new Stage();
            app.start(s);
            return s;
        });

        assertEquals("Rivendell Relief - Nonprofit Bookkeeping", stage.getTitle());
        assertTrue(stage.getScene().getStylesheets().stream()
                .anyMatch(sheet -> sheet.contains("dark.css")));
        assertTrue(FormatUtils.formatCurrency(new BigDecimal("12.34")).contains("€"));

        FxTestSupport.onFx(() -> {
            stage.hide();
            return null;
        });
    }
}
