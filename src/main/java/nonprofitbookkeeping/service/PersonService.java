package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Person;
import nonprofitbookkeeping.persistence.PersonRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Service wrapper for basic person CRUD used by supplemental schedule pickers. */
public class PersonService
{
	private final PersonRepository repository;

	public PersonService()
	{
		this(new PersonRepository());
	}

	PersonService(PersonRepository repository)
	{
		this.repository = repository;
	}

	public List<Person> listPeople()
	{
		try
		{
			return this.repository.list();
		}
		catch (SQLException ex)
		{
			return new ArrayList<>();
		}
	}

	public Person save(Person person) throws SQLException
	{
		return this.repository.save(person);
	}

	public boolean delete(long id) throws SQLException
	{
		return this.repository.delete(id);
	}
}
