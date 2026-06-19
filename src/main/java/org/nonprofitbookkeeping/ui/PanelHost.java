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
        this(new DefaultPanelFactory());
    }

    PanelHost(PanelFactory panelFactory)
    {
        this.panelFactory = panelFactory;
    }

    public SaveResult show(AppPanelId id)
    {
        SaveResult saveResult = SaveResult.noChanges();
        if (activeId != null && activeId != id)
        {
            saveResult = prepareActiveForNavigation();
            if (saveResult.failed())
            {
                return saveResult;
            }
        }
        AppPanel panel = panels.computeIfAbsent(id, this::create);
        activeId = id;
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

    private AppPanel getActive() { return activeId == null ? null : panels.get(activeId); }

    /**
     * Creates the AppPanel (Classic way)
     *
     * @param id the id
     * @return the app panel
     */
    private AppPanel create(AppPanelId id)
    {
        return panelFactory.create(id);
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

    private static class DefaultPanelFactory implements PanelFactory
    {
        public AppPanel create(AppPanelId id)
        {
        return switch (id)
        {
            case DASHBOARD -> new AlternateDashboardPanel(UiServiceRegistry.provider().sessionContext(), UiServiceRegistry.provider());

            case LEDGER_REGISTER -> new LedgerRegisterPanel();

            case SCHEDULES -> new SchedulesPanel();
            case INVENTORY -> new InventoryPanel();

            case BUDGET_EDITOR -> new BudgetEditorPanel();
            case BUDGET_VS_ACTUAL -> new BudgetVsActualPanel();

            case ASSETS_REGISTER -> new AssetsRegisterPanel();
            case DEPRECIATION_RUNS -> new DepreciationRunsPanel();

            case REPORT_LIBRARY, REPORTS_WORKSPACE -> new ReportLibraryPanel();

            case CHART_OF_ACCOUNTS -> new ChartOfAccountsPanel();
            case FUNDS -> new FundsPanel();

            case DATABASE_ADMIN -> new AlternateDatabaseAdminPanel(UiServiceRegistry.provider());
            case COMPANY_ADMIN -> new AlternateCompanyAdminPanel(UiServiceRegistry.provider());
            case IMPORT_EXPORT -> new PlaceholderAppPanel("Import/Export",
                "Import/export workspace is not implemented in PanelHost yet.");
            case SETTINGS -> new SettingsPanel();
        };
        }
    }
}
