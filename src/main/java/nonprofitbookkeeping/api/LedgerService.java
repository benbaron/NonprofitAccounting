// File: src/main/java/nonprofitbookkeeping/api/LedgerService.java

package nonprofitbookkeeping.api;

import nonprofitbookkeeping.ui.helpers.ActionCancelledException;
import nonprofitbookkeeping.ui.helpers.NoFileCreatedException;

/**
 * Defines core ledger operations for creating and managing transactions,
 * retrieving balances, and computing trial balances over date ranges.
 */
public interface LedgerService
{
	/**
	 * Persist the current ledger state to disk.
	 * @throws ActionCancelledException 
	 * @throws NoFileCreatedException 
	 */
	public void save() throws ActionCancelledException, NoFileCreatedException;
	
	
}
