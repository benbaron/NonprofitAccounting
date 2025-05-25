
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import nonprofitbookkeeping.model.*;

/**
 * Thin façade over {@link ChartOfAccounts} that provides CRUD helpers used by
 * the UI layer as well as import/export utilities.
 */
public class ChartOfAccountsService
{
	
	private final ChartOfAccounts coa;
	
	public ChartOfAccountsService(ChartOfAccounts coa)
	{
		this.coa = coa;
	}
	
	/* ------------------------------------------------------------------ */
	public List<Account> roots()
	{
		return this.coa.getRootAccounts();
	}
	
	public List<Account> childrenOf(Account a)
	{
		return this.coa.getChildren(a);
	}
	
	public void addRoot(Account det)
	{
		this.coa.addAccount(det);
	}
	
	public void addChild(Account p, Account det)
	{
		this.coa.addSubAccount(p, det);
	}
	
	public void delete(Account target)
	{
		this.coa.removeAccount(target);
	}
	
	public static void update(	Account tgt, String name,
								String type, BigDecimal openingBal)
	{
		tgt.setName(name);
		tgt.setAccountType(type);
		tgt.setOpeningBalance(openingBal);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Finds the first account whose {@linkplain Account#getAccountNumber()
	 * account number} equals {@code num}.
	 *
	 * @param num search key
	 * @return an {@link Optional} describing the account if found
	 */
	public Optional<Account> findByNumber(String num)
	{
		return this.coa.getAccountNumberToAccountDetails().values()
			.stream()
			.filter(a -> a.getAccountNumber().equals(num))
			.findFirst();
	}
	
	/** Returns a live reference to the wrapped {@link ChartOfAccounts}. */
	public ChartOfAccounts asChart()
	{
		return this.coa;
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Replaces <em>all</em> accounts in the current chart with those from
	 * {@code imported}.  Child relationships are preserved.
	 *
	 * @param imported chart to copy from (must not be {@code null})
	 */
	public void replaceChart(ChartOfAccounts imported)
	{
		
		/* 1) remove every root from the current chart */
		this.coa.getRootAccounts().forEach(this.coa::removeAccount);
		
		/* 2) deep-copy each root (and its subtree) from the imported chart */
		imported.getRootAccounts().forEach(root -> copyRecursive(root, null));
	}
	
	/* helper ----------------------------------------------------------- */
	private void copyRecursive(Account src, Account parentDest)
	{		
		/* clone basic fields */
		Account clone = new Account();
		clone.setAccountNumber(src.getAccountNumber());
		clone.setName(src.getName());
		clone.setAccountType(src.getAccountType());
		clone.setOpeningBalance(src.getOpeningBalance());
		
		/* attach to chart */
		if (parentDest == null)
			this.coa.addAccount(clone);
		else
			this.coa.addSubAccount(parentDest, clone);
		
		/* recurse into children */
		for (Account child : this.coa.getChildren(src))
		{
			copyRecursive(child, clone);
		}
		
	}
	
}
