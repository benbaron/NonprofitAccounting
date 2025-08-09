
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/* jGnash, a personal finance application Copyright 2001-2020 Craig Cavanaugh
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>. */


/**
 * Account type enumeration.
 * 
 * @author Craig Cavanaugh
 */
public enum AccountType
{
	
	/** Represents an asset account. Assets are what a company owns. */
	ASSET,
	/** Represents a bank account. This is a more specific type of asset account. */
	BANK,
	/** Represents a cash account. This is a specific type of asset account for physical currency. */
	CASH,
	/** Represents a checking account. This is a specific type of bank account. */
	CHECKING,
	/** Represents a credit account, typically a liability. */
	CREDIT,
	/** Represents an equity account. Equity is the net worth of a company (Assets - Liabilities). */
	EQUITY,
	/** Represents an expense account. Expenses are costs incurred in the course of business. */
	EXPENSE,
	/** Represents an income account. Income is revenue earned by the company. */
	INCOME,
	/** Represents an investment account. This is a type of asset account. */
	INVEST,
	/** Represents a simple investment account. */
	SIMPLEINVEST,
	/** Represents a liability account. Liabilities are what a company owes. */
	LIABILITY,
	/** Represents a money market account. This is a type of bank account. */
	MONEYMKRT,
	/** Represents a mutual fund account. This is a type of investment account. */
	MUTUAL,
	/** Represents the root account in a chart of accounts hierarchy. */
	ROOT,
	/** Represents a fixed asset account. Fixed assets are long-term tangible assets. */
	FIXED_ASSET, 
	/** Represents a long-term liability account. */
	LONG_TERM_LIABILITY,
	/** Represents a credit card account. This is a specific type of liability account. */
	CREDITCARD,
	/** Represents an unknown or unspecified account type. Used as a default for JSON deserialization. */
	@JsonEnumDefaultValue UNKNOWN;
	
	/**
	 * Creates an AccountType enum from a string value.
	 * This method is used by Jackson for JSON deserialization.
	 * It attempts to match the input string to an enum constant, case-sensitively.
	 * If no match is found, it returns {@link #UNKNOWN}.
	 * @param value The string value to convert to an AccountType.
	 * @return The corresponding AccountType enum constant, or {@link #UNKNOWN} if the value is not recognized.
	 */
	@JsonCreator public static AccountType fromString(String value)
	{
		
		try
		{
			return AccountType.valueOf(value);
		}
		catch (IllegalArgumentException ex)
		{
			return UNKNOWN;
		}
		
	}

        /**
         * Returns the uppercase string representation of this account type.
         *
         * @return the enum name in uppercase form.
         */
        public String toUpperCase()
        {
                return name().toUpperCase();
        }
	
	
}
