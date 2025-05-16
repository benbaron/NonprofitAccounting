package nonprofitbookkeeping.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
@AllArgsConstructor // Generates a constructor with all fields
@ToString         // Automatically generates a toString() method
public class SummaryFund implements Serializable
{
    /**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5590898658697260597L;
	private String fundName = "";
	private double startValue = 0.0;
    
    public SummaryFund()
    {
    	this.fundName = "";
    	this.startValue = 0.0;
    }

	/**
	 * @return the fundName
	 */
	public String getFundName()
	{
		return this.fundName;
	}

	/**
	 * @param fundName the fundName to set
	 */
	public void setFundName(String fundName)
	{
		this.fundName = fundName;
	}

	/**
	 * @return the startValue
	 */
	public double getStartValue()
	{
		return this.startValue;
	}

	/**
	 * @param startValue the startValue to set
	 */
	public void setStartValue(double startValue)
	{
		this.startValue = startValue;
	}
}
