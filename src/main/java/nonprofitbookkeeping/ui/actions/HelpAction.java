package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.GraphicsEnvironment;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nonprofitbookkeeping.ui.help.HelpContent;

/**
 * Handles the action of displaying help content in a JavaFX application.
 * This class implements {@link EventHandler} for {@link ActionEvent} to trigger
 * the display of help information, typically loaded from an HTML file into a WebView.
 * It requires an owner {@link Stage} to properly manage the help window, ensuring
 * the help dialog is appropriately parented.
 */
public class HelpAction implements EventHandler<ActionEvent>
{
        private static final Logger LOGGER = LoggerFactory.getLogger(HelpAction.class);

        /** The owner Stage for the help window, used to ensure proper window modality and positioning. */
        private final Stage ownerStage;

        /**
         * Constructs a new {@code HelpAction}.
         *
         * @param ownerStage The primary {@link Stage} of the JavaFX application, which will serve as
         *                   the owner for the help window. This parameter must not be null.
         * @throws IllegalArgumentException if {@code ownerStage} is null.
         */
        public HelpAction(Stage ownerStage)
        {
                if (ownerStage == null)
                {
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
        @Override public void handle(ActionEvent event)
        {
                if (isHeadlessEnvironment())
                {
                        LOGGER.debug("Help view skipped because the runtime is headless.");
                        return;
                }

                URL helpFileUrl = getClass().getResource("/help/help.html");

                if (helpFileUrl == null)
                {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Help File Not Found");
                        alert.setHeaderText("Could not load help content.");
                        alert.setContentText(
                                "The help file is missing. Please ensure '/help/help.html' is in the application resources.");
                        alert.initOwner(this.ownerStage);
                        alert.showAndWait();
                        return;
                }

                try
                {
                        Stage helpStage = new Stage();
                        helpStage.setTitle("Help");
                        helpStage.initOwner(this.ownerStage);

                        Scene scene = buildHelpScene(helpFileUrl);
                        helpStage.setScene(scene);
                        helpStage.show();
                }
                catch (RuntimeException ex)
                {
                        LOGGER.warn("Unable to display the help window", ex);
                }
        }

        /**
         * Allows subclasses (such as tests) to override the headless detection logic.
         */
        protected boolean isHeadlessEnvironment()
        {
                return GraphicsEnvironment.isHeadless();
        }

        private Scene buildHelpScene(URL helpFileUrl)
        {
                try
                {
                        WebView webView = createWebView();
                        WebEngine engine = webView.getEngine();
                        engine.load(helpFileUrl.toExternalForm());
                        return new Scene(webView, 800, 600);
                }
                catch (Throwable ex)
                {
                        LOGGER.warn(
                                "Falling back to text help because the WebView could not be created or loaded.",
                                ex);
                        TextArea fallback = new TextArea(HelpContent.fallbackText());
                        fallback.setEditable(false);
                        fallback.setWrapText(true);
                        fallback.setFocusTraversable(false);
                        BorderPane container = new BorderPane(fallback);
                        container.setPadding(new Insets(10));
                        return new Scene(container, 600, 500);
                }
        }

        /**
         * Factory method used to create {@link WebView} instances. Subclasses may override
         * this method in tests to simulate WebView failures.
         */
        protected WebView createWebView()
        {
                return new WebView();
        }
}
