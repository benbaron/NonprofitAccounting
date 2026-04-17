package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Keeps legacy person/donor records synchronized with the canonical counterparty table
 * used by the JPA model.
 */
final class CounterpartySyncAdapter
{
    private static final String UPSERT_SQL =
        "MERGE INTO counterparty(display_name, kind, email, phone, is_active) KEY(display_name, kind) VALUES (?,?,?,?,TRUE)";

    private static final String DELETE_SQL =
        "DELETE FROM counterparty WHERE display_name = ? AND kind = ?";

    private static final String DELETE_BY_KIND_SQL =
        "DELETE FROM counterparty WHERE kind = ?";

    private CounterpartySyncAdapter()
    {
    }

    static void syncPerson(Connection c, Person person) throws SQLException
    {
        if (person == null || person.getName() == null || person.getName().isBlank())
        {
            return;
        }
        sync(c, person.getName(), normalizeKind(person.getType()), person.getEmail(), person.getPhone());
    }

    static void syncDonor(Connection c, DonorContact donor) throws SQLException
    {
        if (donor == null || donor.getName() == null || donor.getName().isBlank())
        {
            return;
        }
        sync(c, donor.getName(), "DONOR", donor.getEmail(), donor.getPhone());
    }

    static void deletePerson(Connection c, String name) throws SQLException
    {
        deletePerson(c, name, "DONOR");
    }

    static void deletePerson(Connection c, String name, String type) throws SQLException
    {
        delete(c, name, normalizeKind(type));
    }

    static void deleteDonor(Connection c, String name) throws SQLException
    {
        delete(c, name, "DONOR");
    }

    static void deleteKind(Connection c, String kind) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(DELETE_BY_KIND_SQL))
        {
            ps.setString(1, kind);
            ps.executeUpdate();
        }
    }

    private static void sync(Connection c, String name, String kind, String email, String phone)
        throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
        {
            ps.setString(1, name);
            ps.setString(2, kind);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.executeUpdate();
        }
    }

    private static void delete(Connection c, String name, String kind) throws SQLException
    {
        if (name == null || name.isBlank())
        {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement(DELETE_SQL))
        {
            ps.setString(1, name);
            ps.setString(2, kind);
            ps.executeUpdate();
        }
    }

    private static String normalizeKind(String type)
    {
        return (type == null || type.isBlank()) ? "DONOR" : type.trim().toUpperCase();
    }
}
