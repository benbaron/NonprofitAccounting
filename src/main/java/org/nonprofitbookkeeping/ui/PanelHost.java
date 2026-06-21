/*
 * 
 */
package org.nonprofitbookkeeping.ui;

import javafx.scene.layout.BorderPane;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents the PanelHost component in the nonprofit bookkeeping application.
 */
public class PanelHost extends BorderPane
{
    interface PanelFactory
    {
        AppPanel create(AppPanelId id);
    }

    private final Map<AppPanelId, AppPanel> panels = new EnumMap<>(AppPanelId.class);
    private final PanelFactory panelFactory;
    private AppPanelId activeId;


    /**
     * Show the AppPanel (by ID) Alternate way
     *
     * @param id the id
     */

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
        AppPanel p = getActive();
        return p == null ? "(none)" : p.title();
    }

    public SaveResult saveActive()
    {
        AppPanel p = getActive();
        if (p == null)
        {
            return SaveResult.noChanges("No active panel.");
        }
        if (p instanceof AppPanel.SaveAware saveAware)
        {
            try
            {
                return saveAware.save();
            }
            catch (RuntimeException ex)
            {
                return SaveResult.failed("Save failed for " + p.title() + ": " + ex.getMessage(), ex);
            }
        }
        if (!isDirty(p))
        {
            return SaveResult.noChanges("No changes to save for " + p.title() + ".");
        }
        try
        {
            p.onSave();
            return SaveResult.saved("Saved " + p.title() + ".");
        }
        catch (RuntimeException ex)
        {
            return SaveResult.failed("Save failed for " + p.title() + ": " + ex.getMessage(), ex);
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
        AppPanel p = getActive();
        return isDirty(p);
    }
    public void newItemActive() { AppPanel p = getActive(); if (p != null) p.onNew(); }
    public void copySelectionActive() { AppPanel p = getActive(); if (p != null) p.onCopy(); }
    public void pasteActive() { AppPanel p = getActive(); if (p != null) p.onPaste(); }
    public void deleteActive() { AppPanel p = getActive(); if (p != null) p.onDelete(); }
    public void cancelActive() { AppPanel p = getActive(); if (p != null) p.onCancel(); }

    private AppPanel getActive() { return this.activeId == null ? null : this.panels.get(this.activeId); }

    /**
     * Creates the AppPanel (Classic way)
     *
     * @param id the id
     * @return the app panel
     */
    private AppPanel create(AppPanelId id)
    {
        return this.panelFactory.create(id);
    }

    private boolean canNavigateAway()
    {
        AppPanel p = getActive();
        return !(p instanceof NavigationGuardPanel guard) || guard.canNavigateAway();
    }

    private boolean isDirty(AppPanel p)
    {
        return p instanceof DirtyAwarePanel dirtyAwarePanel && dirtyAwarePanel.isDirty();
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
            this.services = java.util.Objects.requireNonNull(services, "services");
        }

        public AppPanel create(AppPanelId id)
        {
        return switch (id)
        {
            case DASHBOARD -> new AlternateDashboardPanel(this.services.sessionContext(), this.services);

            case LEDGER_REGISTER -> new LedgerRegisterPanel();
            case EVENT_ACCOUNTING -> new EventAccountingPanel(this.services);

            case SCHEDULES -> new SchedulesPanel(this.services);
            case INVENTORY -> new AssetsRegisterPanel("Inventory");

            case BUDGET_EDITOR -> new BudgetEditorPanel();
            case BUDGET_VS_ACTUAL -> new BudgetVsActualPanel();

            case ASSETS_REGISTER -> new AssetsRegisterPanel();
            case DEPRECIATION_RUNS -> new DepreciationRunsPanel();

            case REPORT_LIBRARY, REPORTS_WORKSPACE -> new ReportsWorkspacePanel();

            case CHART_OF_ACCOUNTS -> new ChartOfAccountsPanel();
            case FUNDS -> new FundsPanel();
            case DONORS -> new DonorManagementPanel();
            case RECONCILIATION -> new AlternateReconciliationPanel();

            case DATABASE_ADMIN -> new AlternateDatabaseAdminPanel(this.services);
            case COMPANY_ADMIN -> new AlternateCompanyAdminPanel(this.services);
            case IMPORT_EXPORT -> new AlternateImportExportPanel();
            case MONTHLY_CLOSE -> new MonthlyCloseChecklistPanel(this.services);
            case SETTINGS -> new SettingsPanel(this.services);
        };
        }
    }
}
