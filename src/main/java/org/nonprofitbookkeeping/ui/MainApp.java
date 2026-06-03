package org.nonprofitbookkeeping.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Launcher for the newer {@code org.nonprofitbookkeeping.ui} JavaFX UI systems.
 *
 * <p>Status: live through {@code org.nonprofitbookkeeping.ui.FxMain}. This
 * class selects between {@link MainWindowAlternate}, the default
 * dashboard-first UI, and {@link MainWindow}, the classic menu-bar UI, using
 * the {@code npbk.ui.variant} system property.</p>
 */
public class MainApp extends Application
{
    @Override
    public void start(Stage stage)
    {
        String uiVariant = System.getProperty("npbk.ui.variant", "alternate").trim().toLowerCase();
        boolean alternate = "alternate".equals(uiVariant);
        javafx.scene.Parent root = alternate ? new MainWindowAlternate() : new MainWindow();
        root = new MainWindowAlternate();
        Scene scene = new Scene(root, 1200, 800);

        URL stylesheet = getClass().getResource("/themes/light.css");
        if (stylesheet != null)
        {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        if (!alternate)
        {
            GlobalShortcuts.install(scene, (MainWindow) root);
        }

        stage.setTitle("SCA Ledger (H2 + Jakarta) — Prototype");
        stage.setScene(scene);
        stage.show();
    }
}
