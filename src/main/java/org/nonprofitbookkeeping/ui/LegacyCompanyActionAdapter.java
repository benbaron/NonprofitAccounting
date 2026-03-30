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
    private final java.util.function.Supplier<Stage> stageSupplier;

    LegacyCompanyActionAdapter(java.util.function.Supplier<Stage> stageSupplier)
    {
        this.stageSupplier = stageSupplier;
    }

    @Override
    public void openCompany(Runnable onCompanyOpened)
    {
        new OpenCompanyFileActionFX(stageSupplier.get(), onCompanyOpened);
    }

    @Override
    public void createOrEditCompany()
    {
        new CreateOrEditCompanyActionFX(stageSupplier.get());
    }

    @Override
    public void saveCompany()
    {
        new SaveCompanyFileAction(stageSupplier.get());
    }

    @Override
    public boolean closeCompany()
    {
        return new CloseCompanyFileAction(stageSupplier.get()).isClosed();
    }
}
