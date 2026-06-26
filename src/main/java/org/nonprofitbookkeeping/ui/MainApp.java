package org.nonprofitbookkeeping.ui;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Launcher for the newer {@code org.nonprofitbookkeeping.ui} JavaFX UI systems.
 *
 * <p>The classic {@link MainWindow} shell remains the default because it is the
 * canonical menu/navigation/panel-host UI. Set {@code npbk.ui.variant} to
 * {@code alternate} to launch the dashboard-first candidate shell.</p>
 */
public class MainApp extends Application
{
    @Override
    public void start(Stage stage)
    {
        String uiVariant = System.getProperty("npbk.ui.variant", "alternate")
            .trim().toLowerCase();

        boolean alternate = "alternate".equals(uiVariant);

        javafx.scene.Parent root;
        if (alternate)
        {
            MainWindowAlternate alternateWindow = new MainWindowAlternate();
            removeAlternateHeaderRangeControls(alternateWindow);
            AlternateNavigationCustomizer.apply(alternateWindow);
            AlternateShellNavigationPatch.apply(alternateWindow);
            root = alternateWindow;
        }
        else
        {
            root = new MainWindow();
        }

        Scene scene = new Scene(root, 1200, 800);

        addStylesheet(scene, "/themes/light.css");
        addStylesheet(scene, "/themes/ui-system.css");

        if (!alternate)
        {
            GlobalShortcuts.install(scene, (MainWindow) root);
        }

        stage.setTitle("SCA Ledger (H2 + Jakarta) — Prototype");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Removes the unused global period/range controls from the alternate UI
     * while preserving its title, active-company subtitle, and flexible spacer.
     * The dashboard retains its own As-of date control.
     */
    static void removeAlternateHeaderRangeControls(
        MainWindowAlternate alternateWindow)
    {
        Node top = alternateWindow.getTop();
        if (top instanceof HBox header && header.getChildren().size() > 2)
        {
            header.getChildren().remove(2, header.getChildren().size());
        }
    }

    private void addStylesheet(Scene scene, String resourcePath)
    {
        URL stylesheet = getClass().getResource(resourcePath);
        if (stylesheet != null)
        {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
