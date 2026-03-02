package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.UndepositedFundsItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UndepositedFundsRepository
{
    private static final String INSERT_SQL =
        "INSERT INTO undeposited_funds_item(" +
            "date_sent_received, date_transfer_or_check, " +
            "date_on_statement, name_of_person_business, " +
            "details_notes, from_to_card_merchant, " +
            "account_for_payment_or_deposit, amount, " +
            "date_reversed, reversal_approved_by" +
        ") VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_SQL =
        "UPDATE undeposited_funds_item SET " +
            "date_sent_received = ?, " +
            "date_transfer_or_check = ?, " +
            "date_on_statement = ?, " +
            "name_of_person_business = ?, " +
            "details_notes = ?, " +
            "from_to_card_merchant = ?, " +
            "account_for_payment_or_deposit = ?, " +
            "amount = ?, " +
            "date_reversed = ?, " +
            "reversal_approved_by = ? " +
        "WHERE id = ?";

    private static final String LIST_SQL =
        "SELECT id, date_sent_received, date_transfer_or_check, " +
            "date_on_statement, name_of_person_business, details_notes, " +
            "from_to_card_merchant, account_for_payment_or_deposit, amount, " +
            "date_reversed, reversal_approved_by " +
        "FROM undeposited_funds_item ORDER BY id";

    private static final String FIND_SQL =
        "SELECT id, date_sent_received, date_transfer_or_check, " +
            "date_on_statement, name_of_person_business, details_notes, " +
            "from_to_card_merchant, account_for_payment_or_deposit, amount, " +
            "date_reversed, reversal_approved_by " +
        "FROM undeposited_funds_item WHERE id = ?";

    private static final String DELETE_SQL =
        "DELETE FROM undeposited_funds_item WHERE id = ?";

    public UndepositedFundsItem insert(UndepositedFundsItem item)
        throws SQLException
    {
        try (Connection c = Database.get().getConnection();
            PreparedStatement ps =
                c.prepareStatement(INSERT_SQL, new String[] { "id" }))
        {
            bindWithoutId(ps, item);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next())
                {
                    item.setId(rs.getLong(1));
                }
            }
        }
        return item;
    }

    public void update(UndepositedFundsItem item) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
            PreparedStatement ps = c.prepareStatement(UPDATE_SQL))
        {
            int idx = bindWithoutId(ps, item);
            ps.setLong(idx, item.getId());
            ps.executeUpdate();
        }
    }

    public List<UndepositedFundsItem> list() throws SQLException
    {
        List<UndepositedFundsItem> items = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
            PreparedStatement ps = c.prepareStatement(LIST_SQL);
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    public Optional<UndepositedFundsItem> findById(long id)
        throws SQLException
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

    public boolean deleteById(long id) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
            PreparedStatement ps = c.prepareStatement(DELETE_SQL))
        {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static int bindWithoutId(PreparedStatement ps,
        UndepositedFundsItem item) throws SQLException
    {
        int idx = 1;
        ps.setString(idx++, item.getDate_sent_received());
        ps.setString(idx++, item.getDate_transfer_or_check());
        ps.setString(idx++, item.getDate_on_statement());
        ps.setString(idx++, item.getName_of_person_business());
        ps.setString(idx++, item.getDetails_notes());
        ps.setString(idx++, item.getFrom_to_card_merchant());
        ps.setString(idx++, item.getAccount_for_payment_or_deposit());
        ps.setBigDecimal(idx++, item.getAmount());
        ps.setString(idx++, item.getDate_reversed());
        ps.setString(idx++, item.getReversal_approved_by());
        return idx;
    }

    private static UndepositedFundsItem mapRow(ResultSet rs)
        throws SQLException
    {
        UndepositedFundsItem item = new UndepositedFundsItem();
        item.setId(rs.getLong("id"));
        item.setDate_sent_received(rs.getString("date_sent_received"));
        item.setDate_transfer_or_check(
            rs.getString("date_transfer_or_check"));
        item.setDate_on_statement(rs.getString("date_on_statement"));
        item.setName_of_person_business(
            rs.getString("name_of_person_business"));
        item.setDetails_notes(rs.getString("details_notes"));
        item.setFrom_to_card_merchant(
            rs.getString("from_to_card_merchant"));
        item.setAccount_for_payment_or_deposit(
            rs.getString("account_for_payment_or_deposit"));
        item.setAmount(rs.getBigDecimal("amount"));
        item.setDate_reversed(rs.getString("date_reversed"));
        item.setReversal_approved_by(rs.getString("reversal_approved_by"));
        return item;
    }
}