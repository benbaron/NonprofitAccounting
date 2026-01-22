package nonprofitbookkeeping.ui.javafx.supplemental;

public class PersonRef
{
	private final long personId;
	private final String name;

	public PersonRef(long personId, String name)
	{
		this.personId = personId;
		this.name = name;
	}

	public long getPersonId()
	{
		return this.personId;
	}

	public String getName()
	{
		return this.name;
	}

	@Override
	public String toString()
	{
		return this.name == null ? ("Person #" + this.personId) : this.name;
	}
}
