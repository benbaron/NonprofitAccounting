
package nonprofitbookkeeping.ui;

import javafx.scene.Scene;

// TODO: Auto-generated Javadoc
/**
 * Simple helper for applying a UI theme across scenes.
 */
public final class ThemeManager
{
	
	/** The current theme. */
	private static String currentTheme = "System";
	
	/** The Constant LIGHT_CSS. */
	private static final String UI_SYSTEM_CSS =
		ThemeManager.class.getResource("/themes/ui-system.css").toExternalForm();

	/** The Constant LIGHT_CSS. */
	private static final String LIGHT_CSS =
		ThemeManager.class.getResource("/themes/light.css").toExternalForm();
	
	/** The Constant DARK_CSS. */
	private static final String DARK_CSS =
		ThemeManager.class.getResource("/themes/dark.css").toExternalForm();
	
	/**
	 * Instantiates a new theme manager.
	 */
	private ThemeManager()
	{
	}
	
	/**
	 * Returns the name of the current theme.
	 *
	 * @return the current theme
	 */
	public static String getCurrentTheme()
	{
		return currentTheme;
	}
	
	/**
	 * Sets and applies the theme to the given scene.
	 *
	 * @param scene the scene
	 * @param theme the theme
	 */
	public static void applyTheme(Scene scene, String theme)
	{
		currentTheme = theme == null ? "System" : theme;
		scene.getStylesheets().clear();
		scene.getStylesheets().add(UI_SYSTEM_CSS);
		
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
	
	/**
	 * Applies the current theme to the scene.
	 *
	 * @param scene the scene
	 */
	public static void applyTheme(Scene scene)
	{
		applyTheme(scene, currentTheme);
	}
	
}
