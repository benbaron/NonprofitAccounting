package org.nonprofitbookkeeping.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

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
    @Override
    public void start(Stage stage)
    {
        MainWindow root = new MainWindow();

        Scene scene = new Scene(root, 1200, 800);

        URL stylesheet = getClass().getResource("/themes/light.css");
        if (stylesheet != null)
        {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        GlobalShortcuts.install(scene, root);

        stage.setTitle("SCA Ledger (H2 + Jakarta) — Prototype");
        stage.setScene(scene);
        stage.show();
    }
}
