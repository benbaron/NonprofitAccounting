
package nonprofitbookkeeping.ui.javafx.supplemental;


/**
 * The Class PersonRef.
 */
public class PersonRef
{
	
	/** The person id. */
	private final long personId;
	
	/** The name. */
	private final String name;
	
	/**
	 * Instantiates a new person ref.
	 *
	 * @param personId the person id
	 * @param name the name
	 */
	public PersonRef(long personId, String name)
	{
		this.personId = personId;
		this.name = name;
		
	}
	
	/**
	 * Gets the person id.
	 *
	 * @return the person id
	 */
	public long getPersonId()
	{
		return this.personId;
		
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
		
	}
	
	/**
	 * Override @see java.lang.Object#toString() 
	 */
	@Override
	public String toString()
	{
		return this.name == null ? ("Person #" + this.personId) : this.name;
		
	}
	
}
