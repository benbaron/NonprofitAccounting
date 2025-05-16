package nonprofitbookkeeping.model;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
@AllArgsConstructor // Generates a constructor with all fields
@ToString         // Automatically generates a toString() method
public class SummaryAccount implements Serializable
{
    /**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -7092733266234310917L;
	private String accountName = "";
	private double startValue = 0.0;
    
    public SummaryAccount()
    {
    	this.accountName = "";
    	this.startValue = 0.0;
    }
	/**
	 * @return the accountName
	 */
	public String getAccountName()
	{
		return this.accountName;
	}
	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
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
