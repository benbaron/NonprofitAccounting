
package nonprofitbookkeeping.ui;

import javafx.scene.Scene;

// TODO: Auto-generated Javadoc
/**
 * Simple helper for applying a UI theme across scenes.
 */
public final class ThemeManager
{
	
	/** The current theme. */
	private static UiThemePreference currentTheme = UiThemePreference.SYSTEM_DEFAULT;
	
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
	public static UiThemePreference getCurrentTheme()
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
		applyTheme(scene, UiThemePreference.fromStoredValue(theme));
	}

	public static void applyTheme(Scene scene, UiThemePreference theme)
	{
		currentTheme = theme == null ? UiThemePreference.SYSTEM_DEFAULT : theme;
		scene.getStylesheets().clear();

		switch(currentTheme)
		{
			case DARK:
				scene.getStylesheets().add(DARK_CSS);
				break;

			case LIGHT:
			case SYSTEM_DEFAULT:
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
