package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import javafx.application.Platform;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.LenientCompanyManagementService;
import nonprofitbookkeeping.ui.MainApplicationView;

/**
 * Classic-shell compatibility wrapper for the shared company-management
 * surface.
 */
public class CompanySelectionPanelFX extends CompanyManagementPanelFX
{
    @FunctionalInterface
    public interface OnCompanyOpenedHandler
    {
        void onCompanyOpened(Company company);
    }

    private Consumer<String> errorHandler = message -> { };

    public CompanySelectionPanelFX()
    {
        super(new LenientCompanyManagementService());
        parentProperty().addListener((observable, oldParent, newParent) -> {
            if (newParent instanceof MainApplicationView mainView)
            {
                Platform.runLater(() -> {
                    if (mainView.getCenter() == this)
                    {
                        mainView.showPanel(
                            MainApplicationView.PanelType.DASHBOARD);
                    }
                });
            }
        });
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

    /** Retained for source compatibility with existing callers. */
    public void setOnError(Consumer<String> handler)
    {
        this.errorHandler = handler == null ? message -> { } : handler;
    }

    /** Retained for source compatibility. */
    public void refreshCompanyList()
    {
        super.refreshCompanyList();
    }
}
