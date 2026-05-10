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

    public void show(AppPanelId id)
    {
        if (activeId != null && activeId != id)
        {
            saveActive();
        }
        AppPanel panel = panels.computeIfAbsent(id, this::create);
        activeId = id;
        setCenter(panel.root());
    }

    public String getActiveTitle()
    {
        AppPanel p = getActive();
        return p == null ? "(none)" : p.title();
    }

    public void saveActive() { AppPanel p = getActive(); if (p != null) p.onSave(); }
    public boolean isActiveDirty()
    {
        AppPanel p = getActive();
        return p instanceof DirtyAwarePanel dirtyAwarePanel && dirtyAwarePanel.isDirty();
    }
    public void newItemActive() { AppPanel p = getActive(); if (p != null) p.onNew(); }
    public void copySelectionActive() { AppPanel p = getActive(); if (p != null) p.onCopy(); }
    public void pasteActive() { AppPanel p = getActive(); if (p != null) p.onPaste(); }

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

    interface DirtyAwarePanel
    {
        boolean isDirty();
    }

    private static class DefaultPanelFactory implements PanelFactory
    {
        public AppPanel create(AppPanelId id)
        {
        return switch (id)
        {
            case DASHBOARD -> new DashboardPanel();

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
            case SETTINGS -> new SettingsPanel();
        };
        }
    }
}
