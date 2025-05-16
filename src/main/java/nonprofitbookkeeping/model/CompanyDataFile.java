
package nonprofitbookkeeping.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

@Getter // Automatically generates getter methods
@Setter // Automatically generates setter methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@ToString // Automatically generates a toString() method
/**
 * Company Data File. 
 * This is a wrapper around the Ledger type.
 */
public class CompanyDataFile implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6728014646115467637L;

	private static final ReadOnlyObjectWrapper<CompanyDataFile> companyObs =
		new ReadOnlyObjectWrapper<>();
	
	private CompanyProfileModel companyProfile;
	private Ledger ledger;
	
	/**
	 * @return
	 */
	public Ledger getLedger()
	{
		return this.ledger;
	}
	
	/**
	 * @return
	 */
	public CompanyProfileModel getCompanyProfile()
	{
		return this.companyProfile;
	}
	
	/**
	 * @return the companyobs
	 */
	public static ReadOnlyObjectWrapper<CompanyDataFile> getCompanyobs()
	{
		return companyObs;
	}
	
	/**
	 * For setting a listener/observer
	 * @return the property
	 */
	public static ReadOnlyObjectProperty<CompanyDataFile> 
		getCompanyDataFileProperty()
	{
		return companyObs.getReadOnlyProperty();
	}
	
	/**
	 * getCompanyDataFile
	 * @return
	 */
	public static CompanyDataFile getCompanyDataFile()
	{
		// return the CompanyDataFile from the wrapper
		return companyObs.get();
	}
	
	public static void setCompanyDataFile(CompanyDataFile cdf)
	{
		companyObs.set(cdf);
	}
	
}
