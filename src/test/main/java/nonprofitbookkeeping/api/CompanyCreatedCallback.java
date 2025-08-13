
package nonprofitbookkeeping.api;

import nonprofitbookkeeping.model.CompanyProfileModel;

@FunctionalInterface public interface CompanyCreatedCallback
{
	/**
	 * Called when a company profile model has been created.
	 * @param created The created company profile model.
	 */
	void onCreatedProfileModel(CompanyProfileModel created);
	
}
