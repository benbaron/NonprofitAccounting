
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
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
	
	/**
	 * 
	 * @return
	 */
	public List<Account> roots()
	{
		return this.coa.getRootAccounts();
	}
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	public List<Account> childrenOf(Account a)
	{
		return this.coa.getChildren(a);
	}
	
	/**
	 * 
	 * @param det
	 */
	public void addRoot(Account det)
	{
		this.coa.addAccount(det);
	}
	
	/**
	 * 
	 * @param parent
	 * @param det
	 */
	public void addChild(Account parent, Account det)
	{
		this.coa.addSubAccount(parent, det);
	}
	

	/**
	 * 
	 * @param target
	 */
	public void delete(Account target)
	{
		this.coa.removeAccount(target);
	}
	

	/**
	 * 
	 * @param tgt
	 * @param name
	 * @param type
	 * @param openingBal
	 */
	public static void update(	Account tgt,
						String name,
						String type,
						BigDecimal openingBal)
	{
		tgt.setName(name);
		tgt.setAccountType(type);
		tgt.setOpeningBalance(openingBal);
	}
	
	/**
	 * 
	 * @param num
	 * @return
	 */
	public static Optional<Account> findByNumber(String num)
	{
// return coa.getAccountNumberToAccountDetails().values()
// .stream()
// .filter(a -> a.getAccountNumber().equals(num))
// .findFirst();
		return null;
	}
	
}
