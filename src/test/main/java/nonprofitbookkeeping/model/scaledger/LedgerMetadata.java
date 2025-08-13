package nonprofitbookkeeping.model.scaledger;

/**
 * Represents metadata associated with a ledger or a set of ledger entries.
 * This can include information such as branch name, the fiscal year, and state-specific
 * subsidiary information.
 */
public class LedgerMetadata
{
    /** The name of the branch associated with the ledger data. Initialized to "somebranch" by default in field declaration, but constructor overrides. */
    private String branchName = "somebranch";
    /** The fiscal year or relevant date for the ledger data. Initialized to current date by default in field declaration, but constructor overrides. */
    private java.util.Date year = new java.util.Date();
    /** Information about a subsidiary within a specific state. Initialized to "somestate" by default in field declaration, but constructor overrides. */
    private String subsidiaryInState = "somestate";
    
    /**
     * Constructs a new LedgerMetadata object.
     * Initializes {@code branchName} and {@code subsidiaryInState} to empty strings,
     * and {@code year} to the current date.
     * Prints "bean created" to standard output upon instantiation.
     */
    public LedgerMetadata()
    {
    	setBranchName("");
        setYear(new java.util.Date());
        setSubsidiaryInState("");
	System.out.println("bean created"); // This might be for debugging purposes.
    }

	/**
	 * Gets the branch name associated with this ledger metadata.
	 * @return The branch name.
	 */
	public String getBranchName()
	{
		return this.branchName;
	}

	/**
	 * Sets the branch name for this ledger metadata.
	 * @param branchName The branch name to set.
	 */
	public void setBranchName(String branchName)
	{
		this.branchName = branchName;
	}

	/**
	 * Gets the year (or relevant date) associated with this ledger metadata.
	 * @return The {@link java.util.Date} representing the year/date.
	 */
	public java.util.Date getYear()
	{
		return this.year;
	}

	/**
	 * Sets the year (or relevant date) for this ledger metadata.
	 * @param year The {@link java.util.Date} to set as the year/date.
	 */
	public void setYear(java.util.Date year)
	{
		this.year = year;
	}

	/**
	 * Gets the subsidiary in-state information.
	 * @return The subsidiary in-state string.
	 */
	public String getSubsidiaryInState()
	{
		return this.subsidiaryInState;
	}

	/**
	 * Sets the subsidiary in-state information.
	 * @param subsidiaryInState The subsidiary in-state string to set.
	 */
	public void setSubsidiaryInState(String subsidiaryInState)
	{
		this.subsidiaryInState = subsidiaryInState;
	}
}
