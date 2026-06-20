package org.nonprofitbookkeeping.ui;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.ui.RecordServicePanelRegistry;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents the NavigationPane component in the nonprofit bookkeeping application.
 */
public class NavigationPane extends VBox
{
    private final TreeView<NavItem> tree;
    private final Map<AppPanelId, TreeItem<NavItem>> index = new EnumMap<>(AppPanelId.class);
    private final Consumer<AppPanelId> openPanel;
    private final Consumer<RecordServicePanelRegistry.PanelBinding> openRecordServicePanel;
    private final BiConsumer<String, String> openInspector;

    public NavigationPane(Consumer<AppPanelId> openPanel, BiConsumer<String, String> openInspector)
    {
        this(openPanel, openInspector, binding -> {
        });
    }

    public NavigationPane(Consumer<AppPanelId> openPanel,
        BiConsumer<String, String> openInspector,
        Consumer<RecordServicePanelRegistry.PanelBinding> openRecordServicePanel)
    {
        this.openPanel = openPanel;
        this.openInspector = openInspector;
        this.openRecordServicePanel = openRecordServicePanel;

        getStyleClass().add("nav");

        TreeItem<NavItem> root = new TreeItem<>(new NavItem(null, "Root", null, false));
        root.setExpanded(true);

        TreeItem<NavItem> ops = group(root, "Operations");
        add(ops, AppPanelId.DASHBOARD, "Dashboard");

        TreeItem<NavItem> ledger = group(ops, "Ledger");
        add(ledger, AppPanelId.LEDGER_REGISTER, "Ledger Register");
        add(ledger, AppPanelId.EVENT_ACCOUNTING, "Event Accounting");

        add(ops, AppPanelId.SCHEDULES, "Outstanding / Schedules");
        add(ops, AppPanelId.INVENTORY, "Inventory");

        TreeItem<NavItem> budget = group(ops, "Budget");
        add(budget, AppPanelId.BUDGET_EDITOR, "Budget Editor");
        add(budget, AppPanelId.BUDGET_VS_ACTUAL, "Budget vs Actual");

        TreeItem<NavItem> assets = group(ops, "Assets");
        add(assets, AppPanelId.ASSETS_REGISTER, "Asset Register");
        add(assets, AppPanelId.DEPRECIATION_RUNS, "Depreciation Runs");

        TreeItem<NavItem> outputs = group(root, "Outputs");
        add(outputs, AppPanelId.REPORTS_WORKSPACE, "Reports Workspace");

        TreeItem<NavItem> ref = group(root, "Reference");
        add(ref, AppPanelId.CHART_OF_ACCOUNTS, "Chart of Accounts");
        add(ref, AppPanelId.FUNDS, "Funds");
        add(ref, AppPanelId.DONORS, "Donors");

        TreeItem<NavItem> registry = group(root, "Record Services");
        Map<String, TreeItem<NavItem>> registryCategories = new LinkedHashMap<>();
        RecordServicePanelRegistry.all().values().stream()
            .sorted(Comparator.comparing(RecordServicePanelRegistry.PanelBinding::category)
                .thenComparing(RecordServicePanelRegistry.PanelBinding::displayName))
            .forEach(binding -> {
                TreeItem<NavItem> categoryNode =
                    registryCategories.computeIfAbsent(binding.category(), c -> group(registry, c));
                addRecordService(categoryNode, binding);
            });

        TreeItem<NavItem> admin = group(root, "Database & Company");
        add(admin, AppPanelId.DATABASE_ADMIN, "Database Administration");
        add(admin, AppPanelId.COMPANY_ADMIN, "Company Administration");

        TreeItem<NavItem> importExport = group(root, "Import/Export");
        add(importExport, AppPanelId.IMPORT_EXPORT, "Import/Export");
        add(importExport, AppPanelId.MONTHLY_CLOSE, "Monthly Close Checklist");

        TreeItem<NavItem> sys = group(root, "System");
        add(sys, AppPanelId.SETTINGS, "Settings");

        tree = new TreeView<>(root);
        tree.setShowRoot(false);

        tree.setCellFactory(tv -> new TreeCell<>()
        {
            @Override
            protected void updateItem(NavItem item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });

        tree.setOnMouseClicked(e ->
        {
            TreeItem<NavItem> sel = tree.getSelectionModel().getSelectedItem();
            if (sel == null || sel.getValue() == null) return;

            if (e.getClickCount() == 2)
            {
                NavItem item = sel.getValue();
                if (item.onOpen() != null)
                {
                    item.onOpen().run();
                }
                else if (item.panelId() != null)
                {
                    openPanel.accept(item.panelId());
                }
            }

            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY)
            {
                tree.getSelectionModel().select(sel);
                NavItem v = sel.getValue();
                openInspector.accept("Details: " + v.label(),
                    "Detail inspector placeholder for: " + v.label() + "\n\n(Details-first; journal is a drill-down.)");
            }
        });

        getChildren().add(tree);
    }

    public void highlight(AppPanelId id)
    {
        TreeItem<NavItem> ti = index.get(id);
        if (ti != null) tree.getSelectionModel().select(ti);
    }

    private TreeItem<NavItem> group(TreeItem<NavItem> parent, String label)
    {
        TreeItem<NavItem> g = new TreeItem<>(new NavItem(null, label, null, false));
        g.setExpanded(true);
        parent.getChildren().add(g);
        return g;
    }

    private void add(TreeItem<NavItem> parent, AppPanelId id, String label)
    {
        TreeItem<NavItem> ti = new TreeItem<>(new NavItem(id, label, null, false));
        parent.getChildren().add(ti);
        index.put(id, ti);
    }

    private void addRecordService(TreeItem<NavItem> parent, RecordServicePanelRegistry.PanelBinding binding)
    {
        String suffix = binding.proposedPanel() ? " (Proposed)" : " (Workspace)";
        String label = binding.displayName() + suffix;
        TreeItem<NavItem> ti = new TreeItem<>(new NavItem(null, label, () -> openRecordServicePanel.accept(binding),
            binding.proposedPanel()));
        parent.getChildren().add(ti);
    }

    public record NavItem(AppPanelId panelId, String label, Runnable onOpen, boolean proposed) {}
}
