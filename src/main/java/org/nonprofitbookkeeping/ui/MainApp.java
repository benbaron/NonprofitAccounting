package org.nonprofitbookkeeping.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.ui.bootstrap.SettingsInitializationService;
import nonprofitbookkeeping.ui.bootstrap.SettingsStartupCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton desktop application shell.
 *
 * Design goal:
 * - “Office-like” top menu + toolbar
 * - Left navigation tree
 * - Center workspace with panels
 * - Right-side inspector panel
 */
public class MainApp extends Application
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);
    private final SettingsStartupCoordinator settingsStartupCoordinator;

    public MainApp()
    {
        this(new SettingsStartupCoordinator(new SettingsService(),
            new SettingsInitializationService()));
    }

    MainApp(SettingsStartupCoordinator settingsStartupCoordinator)
    {
        this.settingsStartupCoordinator = settingsStartupCoordinator;
    }

    @Override
    public void start(Stage stage)
    {
        MainWindow root = new MainWindow();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());

        GlobalShortcuts.install(scene, root);

        stage.setTitle("SCA Ledger (H2 + Jakarta) — Prototype");
        stage.setScene(scene);
        applyStartupSettings(stage);
        stage.show();
    }

    private void applyStartupSettings(Stage stage)
    {
        try
        {
            settingsStartupCoordinator.ensureSettingsLoaded(stage, null);
        }
        catch (IOException ex)
        {
            // Keep startup resilient when persisted settings are unavailable.
            LOGGER.debug("Unable to load startup settings", ex);
        }
    }
}
