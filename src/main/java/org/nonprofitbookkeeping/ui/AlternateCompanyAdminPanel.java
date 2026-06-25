package org.nonprofitbookkeeping.ui;

import java.io.File;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.CompanyManagementService;
import nonprofitbookkeeping.ui.panels.DeveloperToolsPanelFX;
import nonprofitbookkeeping.ui.panels.SharedCompanyManagementPanelFX;

/** Panel-host adapter for the shared company management workspace. */
public class AlternateCompanyAdminPanel implements AppPanel
{
    private final SharedCompanyManagementPanelFX panel;

    public AlternateCompanyAdminPanel(UiServiceProvider provider)
    {
        AlternateDataContextService context;
        try
        {
            context = provider.companyAdministration();
        }
        catch (IllegalStateException ex)
        {
            context = new AlternateDataContextService();
        }
        this.panel = new SharedCompanyManagementPanelFX(
            new CompanyManagementService(), new AlternateHost(context));
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

    private static final class AlternateHost
        implements SharedCompanyManagementPanelFX.Host
    {
        private final AlternateDataContextService context;

        private AlternateHost(AlternateDataContextService context)
        {
            this.context = context;
        }

        @Override
        public String activeDatabaseLabel()
        {
            return this.context.activeDatabaseBasePath() == null
                ? "No database open"
                : this.context.activeDatabaseBasePath().toString();
        }

        @Override
        public void switchDatabase(Window owner) throws Exception
        {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open H2 Database");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("H2 Database",
                    "*.mv.db", "*.db"));
            File selected = chooser.showOpenDialog(owner);
            if (selected != null)
            {
                this.context.openDatabase(selected.toPath());
            }
        }

        @Override
        public void openCompany(long id, String label) throws Exception
        {
            this.context.openCompany(id, label);
        }

        @Override
        public void closeActiveCompany()
        {
            CurrentCompany.close();
            this.context.clearActiveCompanyContext();
        }

        @Override
        public Long activeCompanyId()
        {
            return this.context.activeCompanyId();
        }

        @Override
        public void openDeveloperTools(Window owner)
        {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Developer Tools");
            DeveloperToolsPanelFX tools = new DeveloperToolsPanelFX(
                (companyId, label) ->
                    this.context.openCompany(companyId, label));
            dialog.getDialogPane().setContent(tools);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(700, 420);
            if (owner != null)
            {
                dialog.initOwner(owner);
            }
            dialog.showAndWait();
        }
    }
}
