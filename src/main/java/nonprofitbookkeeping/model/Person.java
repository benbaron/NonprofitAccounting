package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.util.Objects;

/** Basic person record used for supplemental schedule counterparty selection. */
public class Person implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String email;
	private String phone;

	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

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

	@Override
	public int hashCode()
	{
		return Objects.hash(this.id, this.name, this.email, this.phone);
	}
}
