package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repository for CRUD operations on the {@code person} table. */
public class PersonRepository
{
	private static final String LIST_SQL =
		"SELECT id, name, email, phone FROM person ORDER BY name, id";
	private static final String FIND_SQL =
		"SELECT id, name, email, phone FROM person WHERE id = ?";
	private static final String INSERT_SQL =
		"INSERT INTO person(name, email, phone) VALUES (?,?,?)";
	private static final String UPDATE_SQL =
		"UPDATE person SET name = ?, email = ?, phone = ? WHERE id = ?";
	private static final String DELETE_SQL =
		"DELETE FROM person WHERE id = ?";

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

	public Person save(Person person) throws SQLException
	{
		if (person.getId() <= 0)
		{
			return insert(person);
		}
		update(person);
		return person;
	}

	public boolean delete(long id) throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(DELETE_SQL))
		{
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		}
	}

	private Person insert(Person person) throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(
				INSERT_SQL, Statement.RETURN_GENERATED_KEYS))
		{
			ps.setString(1, person.getName());
			ps.setString(2, person.getEmail());
			ps.setString(3, person.getPhone());
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys())
			{
				if (keys.next())
				{
					person.setId(keys.getLong(1));
				}
			}
		}

		return person;
	}

	private void update(Person person) throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(UPDATE_SQL))
		{
			ps.setString(1, person.getName());
			ps.setString(2, person.getEmail());
			ps.setString(3, person.getPhone());
			ps.setLong(4, person.getId());
			ps.executeUpdate();
		}
	}

	private static Person mapRow(ResultSet rs) throws SQLException
	{
		Person person = new Person();
		person.setId(rs.getLong("id"));
		person.setName(rs.getString("name"));
		person.setEmail(rs.getString("email"));
		person.setPhone(rs.getString("phone"));
		return person;
	}
}
