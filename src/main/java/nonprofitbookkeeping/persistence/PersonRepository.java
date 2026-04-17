package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Repository for CRUD operations on the {@code person} table. */
@ApplicationScoped
public class PersonRepository
{
	
	/** The Constant LIST_SQL. */
	private static final String LIST_SQL =
		"SELECT id, name, email, phone, type FROM person ORDER BY name, id";
	
	/** The Constant FIND_SQL. */
	private static final String FIND_SQL =
		"SELECT id, name, email, phone, type FROM person WHERE id = ?";
	
	/** The Constant INSERT_SQL. */
	private static final String INSERT_SQL =
		"INSERT INTO person(name, email, phone, type) VALUES (?,?,?,?)";
	
	/** The Constant UPDATE_SQL. */
	private static final String UPDATE_SQL =
		"UPDATE person SET name = ?, email = ?, phone = ?, type = ? WHERE id = ?";

	private static final String NAME_TYPE_SQL =
		"SELECT name, type FROM person WHERE id = ?";
	
	/** The Constant DELETE_SQL. */
	private static final String DELETE_SQL =
		"DELETE FROM person WHERE id = ?";

	/**
	 * List.
	 *
	 * @return the list
	 * @throws SQLException the SQL exception
	 */
	public List<Person> list() throws SQLException
	{
		List<Person> people = new ArrayList<>();

		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(LIST_SQL);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				people.add(mapRow(rs));
			}
		}

		return people;
	}

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the optional
	 * @throws SQLException the SQL exception
	 */
	public Optional<Person> findById(long id) throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(FIND_SQL))
		{
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return Optional.of(mapRow(rs));
				}
			}
		}

		return Optional.empty();
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
		if (person.getId() <= 0)
		{
			return insert(person);
		}
		update(person);
		return person;
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
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(DELETE_SQL))
		{
			Person current = findById(c, id);
			ps.setLong(1, id);
			boolean deleted = ps.executeUpdate() > 0;
			if (deleted)
			{
				CounterpartySyncAdapter.deletePerson(c, current == null ? null : current.getName(),
					current == null ? null : current.getType());
			}
			return deleted;
		}
	}

	/**
	 * Insert.
	 *
	 * @param person the person
	 * @return the person
	 * @throws SQLException the SQL exception
	 */
	private Person insert(Person person) throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(
				INSERT_SQL, Statement.RETURN_GENERATED_KEYS))
		{
			ps.setString(1, person.getName());
			ps.setString(2, person.getEmail());
			ps.setString(3, person.getPhone());
			ps.setString(4, normalizeType(person.getType()));
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys())
			{
				if (keys.next())
				{
					person.setId(keys.getLong(1));
				}
			}
			CounterpartySyncAdapter.syncPerson(c, person);
		}

		return person;
	}

	/**
	 * Update.
	 *
	 * @param person the person
	 * @throws SQLException the SQL exception
	 */
	private void update(Person person) throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(UPDATE_SQL))
		{
			Person existing = findById(c, person.getId());
			ps.setString(1, person.getName());
			ps.setString(2, person.getEmail());
			ps.setString(3, person.getPhone());
			ps.setString(4, normalizeType(person.getType()));
			ps.setLong(5, person.getId());
			ps.executeUpdate();
			if (existing != null
				&& (!Objects.equals(existing.getName(), person.getName())
				|| !normalizeType(existing.getType()).equals(normalizeType(person.getType()))))
			{
				CounterpartySyncAdapter.deletePerson(c, existing.getName(), existing.getType());
			}
			CounterpartySyncAdapter.syncPerson(c, person);
		}
	}

	private static Person findById(Connection c, long id) throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement(NAME_TYPE_SQL))
		{
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery())
			{
				if (!rs.next())
				{
					return null;
				}
				Person person = new Person();
				person.setId(id);
				person.setName(rs.getString(1));
				person.setType(rs.getString(2));
				return person;
			}
		}
	}

	/**
	 * Map row.
	 *
	 * @param rs the rs
	 * @return the person
	 * @throws SQLException the SQL exception
	 */
	private static Person mapRow(ResultSet rs) throws SQLException
	{
		Person person = new Person();
		person.setId(rs.getLong("id"));
		person.setName(rs.getString("name"));
		person.setEmail(rs.getString("email"));
		person.setPhone(rs.getString("phone"));
		person.setType(normalizeType(rs.getString("type")));
		return person;
	}

	private static String normalizeType(String type)
	{
		return (type == null || type.isBlank()) ? "DONOR" : type.trim().toUpperCase();
	}
}
