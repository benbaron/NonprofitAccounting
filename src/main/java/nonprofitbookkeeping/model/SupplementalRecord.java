
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;

/**
 * Represents additional key/value information that can be associated with an
 * {@link AccountingTransaction}.  This data is stored in its own table to allow
 * easy expansion of metadata without altering the core transaction schema.
 */
@Entity
@Table(name = "supplemental_records")
public class SupplementalRecord implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY) private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_id")
	@JsonIgnore private AccountingTransaction transaction;
	
	@JsonProperty private String recordKey;
	
	@JsonProperty private String recordValue;
	
	public SupplementalRecord()
	{
	
	}
	
	public SupplementalRecord(AccountingTransaction transaction,
		String recordKey, String recordValue)
	{
		this.transaction = transaction;
		this.recordKey = recordKey;
		this.recordValue = recordValue;
		
	}
	
	public int getId()
	{
		return id;
		
	}
	
	public AccountingTransaction getTransaction()
	{
		return transaction;
		
	}
	
	public void setTransaction(AccountingTransaction transaction)
	{
		this.transaction = transaction;
		
	}
	
	public String getRecordKey()
	{
		return recordKey;
		
	}
	
	public void setRecordKey(String recordKey)
	{
		this.recordKey = recordKey;
		
	}
	
	public String getRecordValue()
	{
		return recordValue;
		
	}
	
	public void setRecordValue(String recordValue)
	{
		this.recordValue = recordValue;
		
	}
	
}
