/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * Customer.java
 * Customer
 */
package nonprofitbookkeeping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: Auto-generated Javadoc
/**
 * Represents a customer in the nonprofit bookkeeping system.
 * This class stores information about customers, such as their ID and name.
 * It uses Lombok annotations for boilerplate code generation (getters, setters, constructors).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer
{
	/**
	 * The unique identifier for the customer.
	 */
	private String id;

	/**
	 * The name of the customer.
	 */
	private String name;
	


	/**
	 * Constructor Customer.
	 *
	 * @param object the object
	 * @param string the string
	 */
        public Customer(Object object, String string)
        {
                this.id = (object == null) ? null : object.toString();
                this.name = string;
        }

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
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
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}



	
}
