/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * NoFileException.java
 * NoFileException
 */
package nonprofitbookkeeping.service;

/**
 * Thrown when no file is available.
 */
public class NoFileException extends Exception
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -4430741390829154968L;
	final String s;
	
	/**  
	 * Constructor NoFileException
	 * @param string
	 */
	public NoFileException(String string)
	{
		this.s = string;
	}
	
}
