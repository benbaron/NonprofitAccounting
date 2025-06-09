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
 * It requires an owner {@link Stage} to properly manage the help window, ensuring
 * the help dialog is appropriately parented.
 */
public class HelpAction implements EventHandler<ActionEvent> {

    /** The owner Stage for the help window, used to ensure proper window modality and positioning. */
    private final Stage ownerStage;

    /**
     * Constructs a new {@code HelpAction}.
     *
     * @param ownerStage The primary {@link Stage} of the JavaFX application, which will serve as
     *                   the owner for the help window. This parameter must not be null.
     * @throws IllegalArgumentException if {@code ownerStage} is null.
     */
    public HelpAction(Stage ownerStage) {
        if (ownerStage == null) {
            throw new IllegalArgumentException("Owner stage cannot be null.");
        }
        this.ownerStage = ownerStage;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles the action event, typically triggered by selecting a "Help" menu item or button.
     * This method creates and displays a new {@link Stage} containing a {@link WebView}
     * that loads and renders the help content from an HTML file.
     * The help file is expected to be located at the classpath resource path {@code "/help/help.html"}.
     * If the help file cannot be found or loaded, an error {@link Alert} is displayed to the user.
     * </p>
     *
     * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
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
