/**
 * 
 */

package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Account Side: debit or credit
 */
public enum AccountSide
{
	/**
	 * Represents the debit side of an account.
	 * Typically increases asset and expense accounts.
	 */
	DEBIT,
	/**
	 * Represents the credit side of an account.
	 * Typically increases liability, equity, and revenue accounts.
	 */
	CREDIT,
	
	/** Represents an unknown or unspecified account side. Used as a default for JSON deserialization. */
	@JsonEnumDefaultValue UNKNOWN;
	
	/**
	 * Creates an AccountSide enum from a string value.
	 * This method is used by Jackson for JSON deserialization.
	 * It attempts to match the input string to an enum constant, case-sensitively.
	 * If no match is found, it returns {@link #UNKNOWN}.
	 * @param value The string value to convert to an AccountSide.
	 * @return The corresponding AccountSide enum constant, or {@link #UNKNOWN} if the value is not recognized.
	 */
	@JsonCreator public static AccountSide fromString(String value)
	{
		
		try
		{
			return AccountSide.valueOf(value);
		}
		catch (IllegalArgumentException ex)
		{
			return UNKNOWN;
		}
		
	}
	
}
