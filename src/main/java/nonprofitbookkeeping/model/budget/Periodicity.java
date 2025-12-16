package nonprofitbookkeeping.model.budget;

import java.math.BigDecimal;

/**
 * Defines the frequency or period for which budget amounts are specified or broken down.
 * This enum is used in {@link BudgetLine} to indicate how {@code periodicAmounts} should be interpreted.
 */
public enum Periodicity {
    /**
     * Indicates that the budget amount is specified for an entire year.
     * If periodic amounts are used, there might be one, or it might be derived from the total.
     */
    ANNUAL,
    /**
     * Indicates that the budget amount is broken down into quarters.
     * If periodic amounts are used, there would typically be four amounts.
     */
    QUARTERLY,
    /**
     * Indicates that the budget amount is broken down into months.
     * If periodic amounts are used, there would typically be twelve amounts.
     */
    MONTHLY;

	/**
	 * @param bigDecimal
	 * @return
	 */
	public BigDecimal multiply(BigDecimal bigDecimal)
	{
		// TODO Auto-generated method stub
		return null;
		
	}
}
