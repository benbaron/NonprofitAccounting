package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.UndepositedFundsItem;
import nonprofitbookkeeping.persistence.UndepositedFundsRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Class UndepositedFundsService.
 */
public class UndepositedFundsService
{
    private final UndepositedFundsRepository repository;
    private final List<UndepositedFundsItem> items;

    public UndepositedFundsService()
    {
        this(new UndepositedFundsRepository());
    }

    UndepositedFundsService(UndepositedFundsRepository repository)
    {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.items = new ArrayList<>();
    }

    public List<UndepositedFundsItem> listItems()
    {
        reload();
        return new ArrayList<>(this.items);
    }

    public UndepositedFundsItem addItem(UndepositedFundsItem item)
    {
        if (item == null)
        {
            return null;
        }

        try
        {
            UndepositedFundsItem saved = this.repository.insert(item);
            reload();
            return saved;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to insert undeposited funds item", e);
        }
    }

    public void updateItem(UndepositedFundsItem item)
    {
        if (item == null || item.getId() == null)
        {
            return;
        }

        try
        {
            this.repository.update(item);
            reload();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to update undeposited funds item", e);
        }
    }

    public boolean deleteItem(Long id)
    {
        if (id == null)
        {
            return false;
        }

        try
        {
            boolean removed = this.repository.deleteById(id);
            reload();
            return removed;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to delete undeposited funds item", e);
        }
    }

    private void reload()
    {
        try
        {
            this.items.clear();
            this.items.addAll(this.repository.list());
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to load undeposited funds items", e);
        }
    }
}
