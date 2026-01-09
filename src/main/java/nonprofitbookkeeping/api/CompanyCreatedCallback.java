
package nonprofitbookkeeping.api;

import nonprofitbookkeeping.model.CompanyProfileModel;

@FunctionalInterface
public interface CompanyCreatedCallback
{
	/**
	 * Called when a company profile model has been created.
	 *
	 * @param created The created company profile model.
	 * @param seedDemoData {@code true} when the caller requested demo data to be
	 *                     populated for the new company.
	 */
	void onCreatedProfileModel(CompanyProfileModel created,
		boolean seedDemoData);
	
}
