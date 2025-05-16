/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * BeanShell.java
 * BeanShell
 */
package nonprofitbookkeeping.model;

import java.util.Map;

/**
 * 
 */
public class BeanShell
{

	private static Map<String, Object> beans;

	/**
	 * @param beans the beans to set
	 */
	public static void setBeans(Map<String, Object> beans)
	{
		BeanShell.beans = beans;
	}

	/**
	 * @return the beans
	 */
	public static Map<String, Object> getBeans()
	{
		return BeanShell.beans;
	}
	
}
