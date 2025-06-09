/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * BeanShell.java
 * BeanShell
 */
package nonprofitbookkeeping.model;

import java.util.Map;

/**
 * A simple container for managing a static map of beans (objects) accessible by name.
 * This class provides a basic mechanism for a shared context or registry of objects.
 * Note: This implementation is not thread-safe for concurrent modifications to the beans map.
 */
public class BeanShell
{

	private static Map<String, Object> beans;

	/**
	 * Sets the map of beans.
	 * This will replace any existing map of beans.
	 * @param beans A map where keys are bean names (String) and values are bean instances (Object).
	 */
	public static void setBeans(Map<String, Object> beans)
	{
		BeanShell.beans = beans;
	}

	/**
	 * Gets the map of beans.
	 * @return The currently configured map of beans. This can be null if not set.
	 */
	public static Map<String, Object> getBeans()
	{
		return BeanShell.beans;
	}
	
}
