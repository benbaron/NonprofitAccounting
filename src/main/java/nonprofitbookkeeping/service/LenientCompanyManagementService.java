package nonprofitbookkeeping.service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Company-management service variant used by the UI.
 *
 * <p>Configuration warnings remain visible through the company summary, but
 * they do not prevent a user from opening the company to correct its setup.</p>
 */
public class LenientCompanyManagementService extends CompanyManagementService
{
    @Override
    public Company open(long companyId) throws SQLException, IOException
    {
        if (CompanyUiMetadataStore.isArchived(companyId))
        {
            throw new IllegalStateException(
                "Restore the archived company before opening it.");
        }

        load(companyId);
        CurrentCompany.loadFromPersistent(companyId);
        PreferencesService.setLastUsedCompanyId(companyId);
        CompanyUiMetadataStore.setLastOpened(companyId, Instant.now());
        return CurrentCompany.getCompany();
    }
}
