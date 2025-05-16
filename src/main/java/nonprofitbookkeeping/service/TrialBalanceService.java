/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * TrialBalanceService.java
 * TrialBalanceService
 */
package nonprofitbookkeeping.service;

import java.time.LocalDate;
import java.util.Map;

import nonprofitbookkeeping.api.TrialBalanceResultIntf;
import nonprofitbookkeeping.api.TrialBalanceServiceIntf;
import nonprofitbookkeeping.model.Ledger;

/**
 * 
 */
public class TrialBalanceService implements TrialBalanceServiceIntf
{

	/**
	 * Override @see nonprofitbookkeeping.service.TrialBalanceServiceIntf#getDebitSums() 
	 */
	@Override public Map<String, Double> getDebitSums()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Override @see nonprofitbookkeeping.service.TrialBalanceServiceIntf#getCreditSums() 
	 */
	@Override public Map<String, Double> getCreditSums()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Override @see nonprofitbookkeeping.service.TrialBalanceServiceIntf#isBalanced() 
	 */
	@Override public boolean isBalanced()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param ledger
	 * @param from
	 * @param to
	 * @return
	 */
	public static TrialBalanceResultIntf compute(Ledger ledger, LocalDate from, LocalDate to)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
