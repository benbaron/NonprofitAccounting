/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * Customer.java
 * Customer
 */
package nonprofitbookkeeping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	 * Constructor Customer
	 * @param string
	 * @param string2
	 */
	public Customer(String string, String string2)
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}



	
}
