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
	
	// Additional fields like contactInfo, address, etc., could be added later.
}
