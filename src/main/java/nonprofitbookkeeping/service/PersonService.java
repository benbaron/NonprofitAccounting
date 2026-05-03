package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Person;
import nonprofitbookkeeping.persistence.PersonRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/** 
 * Service wrapper for basic person CRUD used by supplemental schedule pickers.
 */
public class PersonService
{
	
	/** The repository. */
	private final PersonRepository repository;

	/**
	 * Instantiates a new person service.
	 */
	public PersonService()
	{
		this(new PersonRepository());
	}

	/**
	 * Instantiates a new person service.
	 *
	 * @param repository the repository
	 */
	PersonService(PersonRepository repository)
	{
		this.repository = repository;
	}

	/**
	 * List people.
	 *
	 * @return the list
	 */
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

	/**
	 * Save.
	 *
	 * @param person the person
	 * @return the person
	 * @throws SQLException the SQL exception
	 */
	public Person save(Person person) throws SQLException
	{
		return this.repository.save(person);
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return true, if successful
	 * @throws SQLException the SQL exception
	 */
	public boolean delete(long id) throws SQLException
	{
		return this.repository.delete(id);
	}
}
