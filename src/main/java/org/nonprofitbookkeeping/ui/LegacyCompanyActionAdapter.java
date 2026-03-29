package org.nonprofitbookkeeping.ui;

import javafx.stage.Stage;
import nonprofitbookkeeping.ui.actions.CloseCompanyFileAction;
import nonprofitbookkeeping.ui.actions.CreateOrEditCompanyActionFX;
import nonprofitbookkeeping.ui.actions.OpenCompanyFileActionFX;
import nonprofitbookkeeping.ui.actions.SaveCompanyFileAction;

/**
 * B-shell adapter that reuses legacy company action implementations.
 */
final class LegacyCompanyActionAdapter implements CompanyActionAdapter
{
    @Override
    public void openCompany(Stage owner, Runnable onCompanyOpened)
    {
        new OpenCompanyFileActionFX(owner, onCompanyOpened);
    }

    @Override
    public void createOrEditCompany(Stage owner)
    {
        new CreateOrEditCompanyActionFX(owner);
    }

    @Override
    public void saveCompany(Stage owner)
    {
        new SaveCompanyFileAction(owner);
    }

    @Override
    public boolean closeCompany(Stage owner)
    {
        return new CloseCompanyFileAction(owner).isClosed();
    }
}
