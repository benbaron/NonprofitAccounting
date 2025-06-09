/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * NoFileCreatedException.java
 * NoFileCreatedException
 */
package nonprofitbookkeeping.exception;

/**
 * Exception thrown when an operation that is expected to create a file fails to do so.
 * This might occur if, for instance, a file save operation is initiated but
 * the user cancels the file chooser dialog, or an I/O error prevents file creation.
 */
public class NoFileCreatedException extends Exception
{
	String reason;
	
	/**  
	 * Constructs a NoFileCreatedException with the specified detail message.
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 */
	public NoFileCreatedException(String message)
	{
		super(message); // Pass the message to the superclass constructor
		this.reason = message;
	}

	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 968938015058824291L;
	
}
