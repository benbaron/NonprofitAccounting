
package nonprofitbookkeeping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents an immutable collection of available accounts.
 */
public class ChartOfAccounts implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6545569795380871696L;

	ArrayList<Account> chartOfAccounts = new ArrayList<>();
	/**
	 * 
	 * Constructor ChartOfAccounts
	 */
	public ChartOfAccounts()
	{

	}

	/**
	 * @return
	 */
	public List<Account> getRootAccounts()
	{
		ArrayList<Account> roots = new ArrayList<>();
		for (Account root : roots)
		{
			if (!root.hasParent())
			{
				roots.add(root);
			}
		}
		return roots;
	}

	/**
	 * @param a
	 * @return
	 */
	public List<Account> getChildren(Account a)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param det
	 */
	public void addAccount(Account det)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * @param parent
	 * @param det
	 */
	public void addSubAccount(Account parent, Account det)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param target
	 */
	public void removeAccount(Account target)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * @return
	 */
	public String getNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
