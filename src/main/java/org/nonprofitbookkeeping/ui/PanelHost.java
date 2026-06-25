/*
 * 
 */
package org.nonprofitbookkeeping.ui;

import javafx.scene.layout.BorderPane;

import java.util.EnumMap;
import java.util.Map;

/** Represents the PanelHost component in the nonprofit bookkeeping application. */
public class PanelHost extends BorderPane
{
    interface PanelFactory
    {
        AppPanel create(AppPanelId id);
    }

    private final Map<AppPanelId, AppPanel> panels =
        new EnumMap<>(AppPanelId.class);
    private final PanelFactory panelFactory;
    private AppPanelId activeId;

    public PanelHost()
    {
        this(new DefaultPanelFactory(UiServiceRegistry.provider()));
    }

    public PanelHost(UiServiceProvider services)
    {
        this(new DefaultPanelFactory(services));
    }

    PanelHost(PanelFactory panelFactory)
    {
        this.panelFactory = panelFactory;
    }

    public SaveResult show(AppPanelId id)
    {
        SaveResult saveResult = SaveResult.noChanges();
        if (this.activeId != null && this.activeId != id)
        {
            saveResult = prepareActiveForNavigation();
            if (saveResult.failed())
            {
                return saveResult;
            }
        }
        AppPanel panel = this.panels.computeIfAbsent(id, this::create);
        this.activeId = id;
        setCenter(panel.root());
        return saveResult;
    }

    public String getActiveTitle()
    {
        AppPanel panel = getActive();
        return panel == null ? "(none)" : panel.title();
    }

    public SaveResult saveActive()
    {
        AppPanel panel = getActive();
        if (panel == null)
        {
            return SaveResult.noChanges("No active panel.");
        }
        if (panel instanceof AppPanel.SaveAware saveAware)
        {
            try
            {
                return saveAware.save();
            }
            catch (RuntimeException ex)
            {
                return SaveResult.failed("Save failed for " + panel.title() +
                    ": " + ex.getMessage(), ex);
            }
        }
        if (!isDirty(panel))
        {
            return SaveResult.noChanges(
                "No changes to save for " + panel.title() + ".");
        }
        try
        {
            panel.onSave();
            return SaveResult.saved("Saved " + panel.title() + ".");
        }
        catch (RuntimeException ex)
        {
            return SaveResult.failed("Save failed for " + panel.title() +
                ": " + ex.getMessage(), ex);
        }
    }

    public SaveResult prepareActiveForNavigation()
    {
        if (!canNavigateAway())
        {
            return SaveResult.failed("Active panel blocked navigation.", null);
        }
        return saveActive();
    }

    public boolean isActiveDirty()
    {
        return isDirty(getActive());
    }

    public void newItemActive()
    {
        AppPanel panel = getActive();
        if (panel != null)
        {
            panel.onNew();
        }
    }

    public void copySelectionActive()
    {
        AppPanel panel = getActive();
        if (panel != null)
        {
            panel.onCopy();
        }
    }

    public void pasteActive()
    {
        AppPanel panel = getActive();
        if (panel != null)
        {
            panel.onPaste();
        }
    }

    public void deleteActive()
    {
        AppPanel panel = getActive();
        if (panel != null)
        {
            panel.onDelete();
        }
    }

    public void cancelActive()
    {
        AppPanel panel = getActive();
        if (panel != null)
        {
            panel.onCancel();
        }
    }

    private AppPanel getActive()
    {
        return this.activeId == null ? null : this.panels.get(this.activeId);
    }

    private AppPanel create(AppPanelId id)
    {
        return this.panelFactory.create(id);
    }

    private boolean canNavigateAway()
    {
        AppPanel panel = getActive();
        return !(panel instanceof NavigationGuardPanel guard) ||
            guard.canNavigateAway();
    }

    private boolean isDirty(AppPanel panel)
    {
        return panel instanceof DirtyAwarePanel dirtyAwarePanel &&
            dirtyAwarePanel.isDirty();
    }

    interface DirtyAwarePanel
    {
        boolean isDirty();
    }

    interface NavigationGuardPanel
    {
        boolean canNavigateAway();
    }

    public static class DefaultPanelFactory implements PanelFactory
    {
        private final UiServiceProvider services;

        public DefaultPanelFactory(UiServiceProvider services)
        {
            this.services = java.util.Objects.requireNonNull(services,
                "services");
        }

        public AppPanel create(AppPanelId id)
        {
            return switch (id)
            {
                case DASHBOARD -> new AlternateDashboardPanel(
                    this.services.sessionContext(), this.services);
                case LEDGER_REGISTER -> new LedgerRegisterPanel();
                case EVENT_ACCOUNTING -> new EventAccountingPanel(this.services);
                case SCHEDULES -> new SchedulesPanel(this.services);
                case INVENTORY -> new AssetsRegisterPanel("Inventory");
                case BUDGET_EDITOR -> new BudgetEditorPanel();
                case BUDGET_VS_ACTUAL -> new BudgetVsActualPanel();
                case ASSETS_REGISTER -> new AssetsRegisterPanel();
                case DEPRECIATION_RUNS -> new DepreciationRunsPanel();
                case REPORT_LIBRARY, REPORTS_WORKSPACE ->
                    new ReportsWorkspacePanel();
                case CHART_OF_ACCOUNTS -> new ChartOfAccountsPanel();
                case FUNDS -> new FundsPanel();
                case DONORS -> new DonorManagementPanel();
                case RECONCILIATION -> new AlternateReconciliationPanel();
                case DATABASE_ADMIN ->
                    new AlternateDatabaseAdminPanel(this.services);
                case COMPANY_ADMIN ->
                    new AlternateCompanyAdminPanel(this.services);
                case IMPORT_EXPORT -> new AlternateImportExportPanel();
                case DEVELOPER_TOOLS -> new DeveloperToolsPanel();
                case MONTHLY_CLOSE ->
                    new MonthlyCloseChecklistPanel(this.services);
                case SETTINGS -> new SettingsPanel(this.services);
            };
        }
    }
}
