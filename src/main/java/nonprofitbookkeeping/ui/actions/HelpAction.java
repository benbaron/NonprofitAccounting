/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * HelpAction.java
 * HelpAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import java.awt.Color; // Added import for Color

/**
 * Provides a simple placeholder icon typically used for a "Help" action or button.
 * This icon displays a blue square with a white question mark ("?") centered within it.
 * It implements the {@link javax.swing.Icon} interface.
 */
public class HelpAction implements Icon
{
	private static final int ICON_WIDTH = 16;
	private static final int ICON_HEIGHT = 16;

	/**
	 * Paints the placeholder help icon at the specified location.
	 * The icon is a blue rectangle with a white question mark.
	 *
	 * @param c The component to which the icon is painted (not directly used in this simple icon).
	 * @param g The {@link Graphics} context to use for painting.
	 * @param x The x-coordinate of the top-left corner of the icon.
	 * @param y The y-coordinate of the top-left corner of the icon.
	 */
	@Override public void paintIcon(Component c, Graphics g, int x, int y)
	{
		g.setColor(Color.BLUE);
		g.fillRect(x, y, getIconWidth(), getIconHeight());
		g.setColor(Color.WHITE);
		// Basic centering for "?" - may need adjustment depending on font metrics in actual use
		g.drawString("?", x + 5, y + getIconHeight() - 4); 
	}
	
	/**
	 * Gets the width of this icon.
	 *
	 * @return The fixed width of the icon (16 pixels).
	 */
	@Override public int getIconWidth()
	{
		return ICON_WIDTH;
	}
	
	/**
	 * Gets the height of this icon.
	 *
	 * @return The fixed height of the icon (16 pixels).
	 */
	@Override public int getIconHeight()
	{
		return ICON_HEIGHT;
	}
	
}
