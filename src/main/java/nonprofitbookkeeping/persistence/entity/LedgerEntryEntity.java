
package nonprofitbookkeeping.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a ledger entry. Core transaction information is
 * stored in this table while split details live in {@link SupplementalRecordEntity}.
 */
@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	
	private String entryDate;
	private String checkNumber;
	private String cleared;
	private String toFrom;
	private String memoString;
	private String budgetTracking;
	
	@OneToMany(mappedBy = "ledgerEntry", cascade = CascadeType.ALL,
		orphanRemoval = true) private List<
			SupplementalRecordEntity> supplementalRecords = new ArrayList<>();
	
	/** Default constructor. */
	public LedgerEntryEntity()
	{
	
	}
	
	public Long getId()
	{
		return id;
		
	}
	
	public void setId(Long id)
	{
		this.id = id;
		
	}
	
	public String getEntryDate()
	{
		return entryDate;
		
	}
	
	public void setEntryDate(String entryDate)
	{
		this.entryDate = entryDate;
		
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
		
	}
	
	public void setCheckNumber(String checkNumber)
	{
		this.checkNumber = checkNumber;
		
	}
	
	public String getCleared()
	{
		return cleared;
		
	}
	
	public void setCleared(String cleared)
	{
		this.cleared = cleared;
		
	}
	
	public String getToFrom()
	{
		return toFrom;
		
	}
	
	public void setToFrom(String toFrom)
	{
		this.toFrom = toFrom;
		
	}
	
	public String getMemoString()
	{
		return memoString;
		
	}
	
	public void setMemoString(String memoString)
	{
		this.memoString = memoString;
		
	}
	
	public String getBudgetTracking()
	{
		return budgetTracking;
		
	}
	
	public void setBudgetTracking(String budgetTracking)
	{
		this.budgetTracking = budgetTracking;
		
	}
	
	public List<SupplementalRecordEntity> getSupplementalRecords()
	{
		return supplementalRecords;
		
	}
	
	public void setSupplementalRecords(
		List<SupplementalRecordEntity> supplementalRecords)
	{
		this.supplementalRecords = supplementalRecords;
		
	}
	
}
