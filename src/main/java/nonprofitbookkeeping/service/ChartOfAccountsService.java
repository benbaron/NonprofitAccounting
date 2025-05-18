/* ────────────────────────────────────────────────────────────── */
/* ChartOfAccountsService.java – simple façade around the model */
/* ────────────────────────────────────────────────────────────── */

package nonprofitbookkeeping.service;

import java.util.List;
import java.util.Optional;

import nonprofitbookkeeping.model.*;

public class ChartOfAccountsService
{
	
	private final ChartOfAccounts coa;
	
	public ChartOfAccountsService(ChartOfAccounts coa)
	{
		this.coa = coa;
	}
	
	/* ------------- simple CRUD wrappers ---------------------- */
	public List<Account> roots()
	{
		return coa.getRootAccounts();
	}
	
	public List<Account> childrenOf(Account a)
	{
		return coa.getChildren(a);
	}
	
	public void addRoot(Account det)
	{
		coa.addAccount(det);
	}
	
	public void addChild(Account parent, Account det)
	{
		coa.addSubAccount(parent, det);
	}
	
	public static void update(Account target, String name, String type)
	{
		target.setName(name);
		target.setAccountType(type);
	}
	
	public void delete(Account target)
	{
		coa.removeAccount(target);
	}
	
	public Optional<Account> findByNumber(String num)
	{
//		return coa.getAccountNumberToAccountDetails().values()
//			.stream()
//			.filter(a -> a.getAccountNumber().equals(num))
//			.findFirst();
		return null;
	}
	
}
