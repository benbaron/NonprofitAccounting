
package nonprofitbookkeeping.ui;

import javafx.scene.Scene;

/**
 * Simple helper for applying a UI theme across scenes.
 */
public final class ThemeManager
{
	private static String currentTheme = "System";
	
	private static final String LIGHT_CSS =
		ThemeManager.class.getResource("/themes/light.css").toExternalForm();
	private static final String DARK_CSS =
		ThemeManager.class.getResource("/themes/dark.css").toExternalForm();
	
	private ThemeManager()
	{
	}
	
	/** Returns the name of the current theme. */
	public static String getCurrentTheme()
	{
		return currentTheme;
	}
	
	/** Sets and applies the theme to the given scene. */
	public static void applyTheme(Scene scene, String theme)
	{
		currentTheme = theme == null ? "System" : theme;
		scene.getStylesheets().clear();
		
		switch(currentTheme.toLowerCase())
		{
			case "dark":
				scene.getStylesheets().add(DARK_CSS);
				break;
				
			case "light":
			case "system":
			default:
				scene.getStylesheets().add(LIGHT_CSS);
				break;
		}
		
	}
	
	/** Applies the current theme to the scene. */
	public static void applyTheme(Scene scene)
	{
		applyTheme(scene, currentTheme);
	}
	
}
