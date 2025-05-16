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
	DEBIT,
	CREDIT,
	
	@JsonEnumDefaultValue UNKNOWN;
	
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
