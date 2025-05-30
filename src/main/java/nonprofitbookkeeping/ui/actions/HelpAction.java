package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.net.URL; // For getResource

/**
 * Handles the action of displaying help content in a JavaFX application.
 * This class implements {@link EventHandler} for {@link ActionEvent} to trigger
 * the display of help information, typically loaded from an HTML file into a WebView.
 * It requires an owner {@link Stage} to properly manage the help window.
 */
public class HelpAction implements EventHandler<ActionEvent> {

    private final Stage ownerStage;

    /**
     * Constructs a new HelpAction.
     *
     * @param ownerStage The primary stage of the JavaFX application, which will own
     *                   the help window. Must not be null.
     * @throws IllegalArgumentException if ownerStage is null.
     */
    public HelpAction(Stage ownerStage) {
        if (ownerStage == null) {
            throw new IllegalArgumentException("Owner stage cannot be null.");
        }
        this.ownerStage = ownerStage;
    }

    /**
     * Handles the action event to display the help content.
     * This method attempts to load and display an HTML file (`/help/help.html`)
     * in a new window with a {@link WebView}. If the help file cannot be found,
     * an error alert is shown.
     *
     * @param event The {@link ActionEvent} that triggered this handler.
     */
    @Override
    public void handle(ActionEvent event) {
        Stage helpStage = new Stage();
        helpStage.setTitle("Help");
        helpStage.initOwner(this.ownerStage);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        URL helpFileUrl = getClass().getResource("/help/help.html");

        if (helpFileUrl == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Help File Not Found");
            alert.setHeaderText("Could not load help content.");
            alert.setContentText("The help file is missing. Please ensure '/help/help.html' is in the application resources.");
            alert.initOwner(this.ownerStage);
            alert.showAndWait();
            return;
        }

        engine.load(helpFileUrl.toExternalForm());

        Scene scene = new Scene(webView, 800, 600);
        helpStage.setScene(scene);
        helpStage.show();
    }
}
