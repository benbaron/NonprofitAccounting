package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;


/**
 * The Class Summary.
 */
@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
@AllArgsConstructor // Generates a constructor with all fields
@ToString         // Automatically generates a toString() method
/**
 * Represents a summary of financial data, typically including summaries of accounts and funds.
 * This class uses Lombok annotations for generating getters, setters, an all-args constructor,
 * and a {@code toString()} method.
 */
public class Summary implements Serializable
{
    /**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 2708260956061642813L;

	
	/** A list of account summaries. Initialized to an empty ArrayList. */
	@JsonProperty private List<SummaryAccount> accounts = new ArrayList<>();
	/** A list of fund summaries. Initialized to an empty ArrayList. */
	@JsonProperty private List<SummaryFund> funds = new ArrayList<>();
    
	/**  
	 * Default constructor.
	 * Initializes an empty Summary object.
	 * Lombok's {@code @AllArgsConstructor} will also create a constructor with all fields.
	 */
	public Summary()
	{
	}

	// Explicit getters and setters are generally not needed due to Lombok @Getter/@Setter,
	// but are documented as they exist.

	/**
	 * Gets the list of account summaries.
	 * @return A list of {@link SummaryAccount} objects.
	 */
	public List<SummaryAccount> getAccounts()
	{
		return this.accounts;
	}
	/**
	 * Sets the list of account summaries.
	 * @param accounts A list of {@link SummaryAccount} objects to set.
	 */
	public void setAccounts(List<SummaryAccount> accounts)
	{
		this.accounts = accounts;
	}
	/**
	 * Gets the list of fund summaries.
	 * @return A list of {@link SummaryFund} objects.
	 */
	public List<SummaryFund> getFunds()
	{
		return this.funds;
	}
	/**
	 * Sets the list of fund summaries.
	 * @param funds A list of {@link SummaryFund} objects to set.
	 */
	public void setFunds(List<SummaryFund> funds)
	{
		this.funds = funds;
	}
}