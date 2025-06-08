/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * NoFileException.java
 * NoFileException
 */
package nonprofitbookkeeping.exception;

/**
 * Thrown when no file is available.
 */
public class NoFileException extends Exception
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -4430741390829154968L;
	final String s;
	
	/**  
	 * Constructs a NoFileException with the specified detail message.
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 */
	public NoFileException(String message)
	{
		super(message); // Pass the message to the superclass constructor
		this.s = message;
	}
	
}
