package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Window;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CompanyManagementPanelFX;

/** New-shell adapter for the shared company selection and administration UI. */
public class AlternateCompanyAdminPanel implements AppPanel
{
    private final CompanyManagementPanelFX panel;

    public AlternateCompanyAdminPanel(UiServiceProvider provider)
    {
        this.panel = new CompanyManagementPanelFX();
        this.panel.setOnCompanyOpened(company -> {
            Long id = PreferencesService.getLastUsedCompanyId();
            if (id != null)
            {
                provider.sessionContext().openCompany(id,
                    company == null ? "Company " + id : company.getName());
            }
        });
        this.panel.setOpenDatabaseAction(() -> Platform.runLater(() -> {
            MainWindowAlternate shell = findAlternateShell();
            if (shell != null)
            {
                shell.openDatabaseSelector();
            }
        }));
    }

    @Override
    public String title()
    {
        return "Company Administration";
    }

    @Override
    public Node root()
    {
        return this.panel;
    }

    private MainWindowAlternate findAlternateShell()
    {
        for (Window window : Window.getWindows())
        {
            if (window.isShowing() && window.getScene() != null &&
                window.getScene().getRoot() instanceof MainWindowAlternate shell)
            {
                return shell;
            }
        }
        return null;
    }
}
