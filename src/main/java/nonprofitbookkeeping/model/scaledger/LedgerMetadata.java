package nonprofitbookkeeping.model.scaledger;

public class LedgerMetadata
{
    private String branchName = "somebranch";
    private java.util.Date year = new java.util.Date();
    private String subsidiaryInState = "somestate";
    
    public LedgerMetadata()
    {
    	setBranchName("");
        setYear(new java.util.Date());
        setSubsidiaryInState("");
    	System.out.println("bean created");
    }

	/**
	 * @return the branchName
	 */
	public String getBranchName()
	{
		return this.branchName;
	}

	/**
	 * @param branchName the branchName to set
	 */
	public void setBranchName(String branchName)
	{
		this.branchName = branchName;
	}

	/**
	 * @return the year
	 */
	public java.util.Date getYear()
	{
		return this.year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(java.util.Date year)
	{
		this.year = year;
	}

	/**
	 * @return the subsidiaryInState
	 */
	public String getSubsidiaryInState()
	{
		return this.subsidiaryInState;
	}

	/**
	 * @param subsidiaryInState the subsidiaryInState to set
	 */
	public void setSubsidiaryInState(String subsidiaryInState)
	{
		this.subsidiaryInState = subsidiaryInState;
	}
}
