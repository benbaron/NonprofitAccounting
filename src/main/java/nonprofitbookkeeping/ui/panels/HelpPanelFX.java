
package nonprofitbookkeeping.ui.panels;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * JavaFX version of {@code HelpPanel}. Attempts to load an embedded HTML help
 * page ("/help/index.html"). If unavailable, falls back to displaying a simple
 * Markdown-like text explaining keyboard shortcuts and where to find docs.
 */
public class HelpPanelFX extends BorderPane
{
	
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
		try (InputStream in = getClass().getResourceAsStream("/help/index.html"))
		{
			if (in != null)
			{
				String html = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
					.lines().collect(Collectors.joining("\n"));
				WebView web = new WebView();
				web.getEngine().loadContent(html);
				return new ScrollPane(web);
			}
			
		}
		catch (Exception ignored)
		{
		}
		
		// Fallback text
		String fallback = "Nonprofit Bookkeeping\n\n" +
			"Keyboard shortcuts:\n  • Ctrl+S — Save current record\n  • Ctrl+O — Open company file\n  • F1 — Open this help window\n\n" +
			"Full documentation is available in the docs/ folder shipped with the application.";
		ScrollPane sp = new ScrollPane(new Label(fallback));
		sp.setFitToWidth(true);
		return sp;
	}
	
}
