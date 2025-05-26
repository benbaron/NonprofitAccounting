
package nonprofitbookkeeping.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Thread-safe in-memory journal store.
 */
public class JournalService
{
	
	private final Map<String, AccountingTransaction> store =
		new ConcurrentHashMap<>();
	
	public void add(AccountingTransaction tx)
	{
		put(tx);
	}
	
	public void update(AccountingTransaction tx)
	{
		put(tx);
	}
	
	public void delete(String id)
	{
		this.store.remove(id);
	}
	
	public AccountingTransaction get(String id)
	{
		return this.store.get(id);
	}
	
	public Collection<AccountingTransaction> list()
	{
		return this.store.values();
	}
	
	/* ---- helper ---- */
	private void put(AccountingTransaction tx)
	{
		if (tx == null || tx.getId() == null)
		{
			throw new IllegalArgumentException("Null tx or id");
		}
		this.store.put(tx.getId(), tx);
	}
	
}
