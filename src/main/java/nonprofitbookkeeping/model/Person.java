package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/** 
 * Basic person record used for supplemental schedule counterparty selection. 
 */
public class Person implements Serializable
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private long id;
	
	/** The name. */
	private String name;
	
	/** The email. */
	private String email;
	
	/** The phone. */
	private String phone;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId()
	{
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(long id)
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
		if (!(o instanceof Person))
		{
			return false;
		}
		Person person = (Person) o;
		return this.id == person.id
			&& Objects.equals(this.name, person.name)
			&& Objects.equals(this.email, person.email)
			&& Objects.equals(this.phone, person.phone);
	}

	/**
	 * Override @see java.lang.Object#hashCode() 
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(this.id, this.name, this.email, this.phone);
	}
}
