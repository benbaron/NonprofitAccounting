package nonprofitbookkeeping.model;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
@AllArgsConstructor // Generates a constructor with all fields
@ToString         // Automatically generates a toString() method
/**
 * Represents a summary for a single account, typically including its name and a starting value or balance.
 * This class uses Lombok annotations for generating getters, setters, an all-args constructor,
 * and a {@code toString()} method.
 */
public class SummaryAccount implements Serializable
{
    /**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -7092733266234310917L;
	/** The name of the account being summarized. Initialized to an empty string. */
	@JsonProperty private String accountName = "";
	/** The starting value or balance of the account for the summary period. Initialized to 0.0. */
	@JsonProperty private double startValue = 0.0;
    
    /**
     * Default constructor.
     * Initializes {@code accountName} to an empty string and {@code startValue} to 0.0.
     * Lombok's {@code @AllArgsConstructor} also provides a constructor with all fields.
     */
    public SummaryAccount()
    {
    	this.accountName = "";
    	this.startValue = 0.0;
    }

    // Explicit getters and setters are generally not needed due to Lombok @Getter/@Setter,
	// but are documented as they exist.

	/**
	 * Gets the name of the account.
	 * @return The account name.
	 */
	public String getAccountName()
	{
		return this.accountName;
	}
	/**
	 * Sets the name of the account.
	 * @param accountName The account name to set.
	 */
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}
	/**
	 * Gets the starting value or balance of the account.
	 * @return The starting value.
	 */
	public double getStartValue()
	{
		return this.startValue;
	}
	/**
	 * Sets the starting value or balance of the account.
	 * @param startValue The starting value to set.
	 */
	public void setStartValue(double startValue)
	{
		this.startValue = startValue;
	}
}
