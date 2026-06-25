package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import nonprofitbookkeeping.model.Company;

/**
 * Classic-shell compatibility name for the shared company management panel.
 */
public class CompanySelectionPanelFX extends SharedCompanyManagementPanelFX
{
    @FunctionalInterface
    public interface OnCompanyOpenedHandler
    {
        void onCompanyOpened(Company company);
    }

    public CompanySelectionPanelFX()
    {
        super();
    }

    public CompanySelectionPanelFX(OnCompanyOpenedHandler handler)
    {
        this();
        setOnCompanyOpenedHandler(handler);
    }

    public void setOnCompanyOpenedHandler(OnCompanyOpenedHandler handler)
    {
        setOnCompanyOpened(handler == null ? null : handler::onCompanyOpened);
    }

    @Override
    public void setOnError(Consumer<String> handler)
    {
        super.setOnError(handler);
    }
}
