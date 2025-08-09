/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * ActionCancelledException.java
 * ActionCancelledException
 */
package nonprofitbookkeeping.exception;

/**
 * Exception thrown when an action is cancelled by the user,
 * for example, by closing a file dialog without selecting a file.
 */
public class ActionCancelledException extends Exception
{
	String reason = null;
	/**  
	 * Constructs an ActionCancelledException with the specified detail message.
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 */
	public ActionCancelledException(String message)
	{
		super(message); // Pass the message to the superclass constructor
		this.reason = message;
	}

	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 7129115303272050087L;
	
}
