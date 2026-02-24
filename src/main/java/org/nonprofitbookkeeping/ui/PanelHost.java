package org.nonprofitbookkeeping.ui;

import javafx.scene.layout.BorderPane;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents the PanelHost component in the nonprofit bookkeeping application.
 */
public class PanelHost extends BorderPane
{
    private final Map<AppPanelId, AppPanel> panels = new EnumMap<>(AppPanelId.class);
    private final Consumer<AppPanelId> navigator;
    private final BiConsumer<String, String> inspector;
    private AppPanelId activeId;

    public PanelHost(Consumer<AppPanelId> navigator, BiConsumer<String, String> inspector)
    {
        this.navigator = navigator;
        this.inspector = inspector;
    }

    public void show(AppPanelId id)
    {
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
    public void postValidateActive() { AppPanel p = getActive(); if (p != null) p.onPostValidate(); }
    public void newItemActive() { AppPanel p = getActive(); if (p != null) p.onNew(); }
    public void copySelectionActive() { AppPanel p = getActive(); if (p != null) p.onCopy(); }
    public void pasteActive() { AppPanel p = getActive(); if (p != null) p.onPaste(); }

    private AppPanel getActive() { return activeId == null ? null : panels.get(activeId); }

    private AppPanel create(AppPanelId id)
    {
        return switch (id)
        {
            case DASHBOARD -> new DashboardPanel();

            case LEDGER_REGISTER -> new LedgerRegisterPanel(
                () -> navigator.accept(AppPanelId.TXN_EDITOR), inspector);
            case TXN_EDITOR -> new TransactionEditorPanel(
                () -> navigator.accept(AppPanelId.LEDGER_REGISTER));

            case SCHEDULES -> new SchedulesPanel();

            case BUDGET_EDITOR -> new BudgetEditorPanel();
            case BUDGET_VS_ACTUAL -> new BudgetVsActualPanel();

            case ASSETS_REGISTER -> new AssetsRegisterPanel();
            case DEPRECIATION_RUNS -> new DepreciationRunsPanel();

            case REPORT_LIBRARY -> new ReportLibraryPanel();

            case CHART_OF_ACCOUNTS -> new ChartOfAccountsPanel();
            case FUNDS -> new FundsPanel();
            case SETTINGS -> new SettingsPanel();
        };
    }
}
