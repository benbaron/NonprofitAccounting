package nonprofitbookkeeping.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
@AllArgsConstructor // Generates a constructor with all fields
@ToString         // Automatically generates a toString() method
/**
 * Represents a summary for a single fund, typically including its name and a starting value or balance.
 * This class uses Lombok annotations for generating getters, setters, an all-args constructor,
 * and a {@code toString()} method.
 */
public class SummaryFund implements Serializable
{
    /**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 5590898658697260597L;
	/** The name of the fund being summarized. Initialized to an empty string. */
	@JsonProperty private String fundName = "";
	/** The starting value or balance of the fund for the summary period. Initialized to 0.0. */
	@JsonProperty private double startValue = 0.0;
    
    /**
     * Default constructor.
     * Initializes {@code fundName} to an empty string and {@code startValue} to 0.0.
     * Lombok's {@code @AllArgsConstructor} also provides a constructor with all fields.
     */
    public SummaryFund()
    {
    	this.fundName = "";
    	this.startValue = 0.0;
    }

    // Explicit getters and setters are generally not needed due to Lombok @Getter/@Setter,
	// but are documented as they exist.

	/**
	 * Gets the name of the fund.
	 * @return The fund name.
	 */
	public String getFundName()
	{
		return this.fundName;
	}

	/**
	 * Sets the name of the fund.
	 * @param fundName The fund name to set.
	 */
	public void setFundName(String fundName)
	{
		this.fundName = fundName;
	}

	/**
	 * Gets the starting value or balance of the fund.
	 * @return The starting value.
	 */
	public double getStartValue()
	{
		return this.startValue;
	}

	/**
	 * Sets the starting value or balance of the fund.
	 * @param startValue The starting value to set.
	 */
	public void setStartValue(double startValue)
	{
		this.startValue = startValue;
	}
}
