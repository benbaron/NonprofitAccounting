/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * ActionCancelledException.java
 * ActionCancelledException
 */
package nonprofitbookkeeping.exception;

/**
 * 
 */
public class ActionCancelledException extends Exception
{
	String reason = null;
	/**  
	 * Constructor ActionCancelledException
	 * @param string
	 */
	public ActionCancelledException(String string)
	{
		this.reason = string;
	}

	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 7129115303272050087L;
	
}
