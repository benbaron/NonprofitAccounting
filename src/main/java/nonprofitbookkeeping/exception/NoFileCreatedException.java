/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * NoFileCreatedException.java
 * NoFileCreatedException
 */
package nonprofitbookkeeping.exception;

/**
 * 
 */
public class NoFileCreatedException extends Exception
{
	String reason;
	
	/**  
	 * Constructor NoFileCreatedException
	 * @param string
	 */
	public NoFileCreatedException(String string)
	{
		this.reason = string;
	}

	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 968938015058824291L;
	
}
