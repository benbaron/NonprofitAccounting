
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
	
	public HelpPanelFX(Stage primaryStage)
	{
		setPadding(new Insets(10));
		setTop(new Label("Help & Documentation"));
		setCenter(loadHelpContent());
	}
	
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
