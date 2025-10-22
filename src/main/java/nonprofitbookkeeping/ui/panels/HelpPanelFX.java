
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import nonprofitbookkeeping.ui.help.HelpContent;

/**
 * JavaFX version of {@code HelpPanel}. Attempts to load an embedded HTML help
 * page ("/help/index.html"). If unavailable, falls back to displaying a simple
 * Markdown-like text explaining keyboard shortcuts and where to find docs.
 */
public class HelpPanelFX extends BorderPane
{
        private static final Logger LOGGER = Logger.getLogger(HelpPanelFX.class.getName());

	
	/**
	 * Constructs a new {@code HelpPanelFX}.
	 * Initializes the panel layout, sets a title label ("Help & Documentation"),
	 * and loads the help content (HTML or fallback text) into the center of the panel.
	 *
	 * @param primaryStage The primary stage of the application. This parameter is currently not used within the constructor.
	 */
	public HelpPanelFX(Stage primaryStage)
	{
		setPadding(new Insets(10));
		setTop(new Label("Help & Documentation"));
		setCenter(loadHelpContent());
	}
	
	/**
	 * Attempts to load help content from an embedded HTML file ({@code /help/index.html}).
	 * If the HTML file is found and successfully read, its content is loaded into a {@link WebView}
	 * which is then wrapped in a {@link ScrollPane}.
	 * If the HTML file cannot be loaded (e.g., not found, I/O error), it falls back to displaying
	 * a predefined string of fallback text (containing basic shortcuts and documentation info)
	 * within a {@link Label}, also wrapped in a {@link ScrollPane}.
	 *
	 * @return A {@link ScrollPane} containing either the loaded HTML content in a {@link WebView}
	 *         or the fallback help text in a {@link Label}.
	 */
        private ScrollPane loadHelpContent()
        {
                Optional<String> html = HelpContent.loadHelpDocument("/help/index.html");

                if (html.isPresent())
                {
                        try
                        {
                                WebView web = new WebView();
                                web.getEngine().loadContent(html.get());
                                ScrollPane pane = new ScrollPane(web);
                                pane.setFitToWidth(true);
                                pane.setFitToHeight(true);
                                return pane;
                        }
                        catch (Throwable ex)
                        {
                                LOGGER.log(Level.WARNING,
                                        "Falling back to text help because the WebView could not be created.", ex);
                        }
                }

                ScrollPane sp = new ScrollPane(createFallbackLabel());
                sp.setFitToWidth(true);
                sp.setFitToHeight(true);
                return sp;
        }

        private static Label createFallbackLabel()
        {
                Label label = new Label(HelpContent.fallbackText());
                label.setPadding(new Insets(10));
                label.setWrapText(true);
                return label;
        }

}
