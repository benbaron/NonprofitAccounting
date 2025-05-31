package nonprofitbookkeeping.model.ofx;

import java.math.BigDecimal;
import nonprofitbookkeeping.model.Account;

public class InvestmentTransaction extends Transaction
{

	/**  
	 * Constructor InvestmentTransaction
	 * @param fitId
	 * @param dtPosted
	 * @param memo
	 * @param transactionName
	 * @param transactionType
	 * @param securityNode
	 * @param quantity
	 * @param price
	 * @param fees
	 */
	public InvestmentTransaction(String fitId, String dtPosted, String memo, String transactionName,
		String transactionType, SecurityNode securityNode, BigDecimal quantity, BigDecimal price,
		BigDecimal fees)
	{
		// TODO Auto-generated constructor stub
	}

	public static SecurityNode getSecurityNode()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static BigDecimal getQuantity()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static BigDecimal getPrice()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static BigDecimal getFees()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static BigDecimal getTotal(Account account)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public BigDecimal getTotal()
	{
		// TODO Auto-generated method stub
		return null;
	}


	
}
