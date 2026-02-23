
package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/** 
 * Simple contact information for a donor. 
 */
public class DonorContact implements Serializable
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The id. */
	private String id;
	
	/** The name. */
	private String name;
	
	/** The email. */
	private String email;
	
	/** The phone. */
	private String phone;
	
	/**
	 * Instantiates a new donor contact.
	 */
	public DonorContact()
	{
		
		// Default constructor required for frameworks and serialization
	}
	
	/**
	 * Instantiates a new donor contact.
	 *
	 * @param id the id
	 * @param name the name
	 * @param email the email
	 * @param phone the phone
	 */
	public DonorContact(String id, String name, String email, String phone)
	{
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
		
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
	 * @param id the new id
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
	 * @param name the new name
	 */
	public void setName(String name)
	{
		this.name = name;
		
	}
	
	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail()
	{
		return this.email;
		
	}
	
	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email)
	{
		this.email = email;
		
	}
	
	/**
	 * Gets the phone.
	 *
	 * @return the phone
	 */
	public String getPhone()
	{
		return this.phone;
		
	}
	
	/**
	 * Sets the phone.
	 *
	 * @param phone the new phone
	 */
	public void setPhone(String phone)
	{
		this.phone = phone;
		
	}
	
	/**
	 * Override @see java.lang.Object#equals(java.lang.Object) 
	 */
	@Override
	public boolean equals(Object o)
	{
		
		if (this == o)
		{
			return true;
		}
		
		if (!(o instanceof DonorContact))
		{
			return false;
		}
		
		DonorContact that = (DonorContact) o;
		return Objects.equals(this.id, that.id) &&
			Objects.equals(this.name, that.name) &&
			Objects.equals(this.email, that.email) &&
			Objects.equals(this.phone, that.phone);
		
	}
	
	/**
	 * Override @see java.lang.Object#hashCode() 
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(this.id, this.name, this.email, this.phone);
		
	}
	
	/**
	 * Override @see java.lang.Object#toString() 
	 */
	@Override
	public String toString()
	{
		return "DonorContact{" + "id='" + this.id + '\'' + ", name='" +
			this.name + '\'' + ", email='" + this.email + '\'' + ", phone='" +
			this.phone + '\'' + '}';
		
	}
	
}
