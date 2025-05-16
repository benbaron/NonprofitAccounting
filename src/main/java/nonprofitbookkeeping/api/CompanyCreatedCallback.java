
package nonprofitbookkeeping.api;

import nonprofitbookkeeping.model.CompanyProfileModel;

@FunctionalInterface public interface CompanyCreatedCallback
{
	void onCompanyCreated(CompanyProfileModel created);
	
}
