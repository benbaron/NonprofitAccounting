package org.nonprofitbookkeeping.service;

import java.math.BigDecimal;

import org.nonprofitbookkeeping.model.FundType;

/**
 * A simple “as of” fund balance result row.
 * The meaning of this balance depends on which accounts are included in the
 * query.
 */
public class FundBalanceRow
{
    private final String fundCode;
    private final String fundName;
    private final FundType fundType;
    private final BigDecimal balance;

    public FundBalanceRow(String fundCode, String fundName,
        BigDecimal balance)
    {
        this(fundCode, fundName, FundType.OTHER, balance);
    }

    public FundBalanceRow(String fundCode, String fundName,
        FundType fundType, BigDecimal balance)
    {
        this.fundCode = fundCode;
        this.fundName = fundName;
        this.fundType = fundType == null ? FundType.OTHER : fundType;
        this.balance = balance == null ? BigDecimal.ZERO : balance;
    }

    public String getFundCode()
    {
        return this.fundCode;
    }

    public String getFundName()
    {
        return this.fundName;
    }

    public FundType getFundType()
    {
        return this.fundType;
    }

    public BigDecimal getBalance()
    {
        return this.balance;
    }
}
