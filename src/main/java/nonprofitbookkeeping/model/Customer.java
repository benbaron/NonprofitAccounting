/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * Customer.java
 * Customer
 */
package nonprofitbookkeeping.model;

// TODO: Auto-generated Javadoc
/**
 * Represents a customer in the nonprofit bookkeeping system.
 * This class stores information about customers, such as their ID and name.
 */
public class Customer extends AbstractRecordModel
{
	/**
	 * Default constructor.
	 */
	public Customer()
	{
		super();
	}

	/**
	 * Constructs a customer with id and name.
	 *
	 * @param id the id
	 * @param name the name
	 */
	public Customer(String id, String name)
	{
		super(id, name);
	}

	/**
	 * Constructor Customer.
	 *
	 * @param object the object
	 * @param string the string
	 */
        public Customer(Object object, String string)
        {
                super((object == null) ? null : object.toString(), string);
        }
}
