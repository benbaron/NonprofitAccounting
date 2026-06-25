package org.nonprofitbookkeeping.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.ui.LedgerNavigationContext;
import nonprofitbookkeeping.ui.panels.JournalPanelFX;

/**
 * Replaces the alternate shell's generated navigation with a stable,
 * user-ordered, collapsible navigation rail.
 */
final class AlternateNavigationCustomizer
{
    private static final double COLLAPSE_THRESHOLD = 125.0;
    private static final double COLLAPSED_MIN_WIDTH = 58.0;
    private static final String ICON_PROPERTY = "alternate.nav.icon";
    private static final String TITLE_PROPERTY = "alternate.nav.title";

    private AlternateNavigationCustomizer()
    {
    }

    static void apply(MainWindowAlternate window)
    {
        if (!(window.getCenter() instanceof SplitPane splitPane) ||
            splitPane.getItems().size() < 2)
        {
            return;
        }

        Node originalNavigation = splitPane.getItems().get(0);
        Button originalProfile = findButton(originalNavigation, "Profile");
        Button originalCommandCenter = findButton(originalNavigation,
            "Command Center");

        StackPane workspace = splitPane.getItems().get(1) instanceof StackPane
            ? (StackPane) splitPane.getItems().get(1)
            : null;
        JournalPanelFX journal = createJournalWorkspace(window, workspace);

        Label navigationTitle = new Label("Navigation");
        navigationTitle.getStyleClass().add("alternate-navigation-title");

        VBox primary = group(
            navButton("◉", "Profile", () -> fire(originalProfile, window,
                AppPanelId.DASHBOARD, journal)),
            navButton("⌂", "Dashboard", () -> openPanel(window,
                AppPanelId.DASHBOARD, journal)),
            navButton("⌕", "Search", () -> {
                hideJournal(journal);
                window.openSearchPage();
            }),
            navButton("⚙", "Settings", () -> openPanel(window,
                AppPanelId.SETTINGS, journal)),
            navButton("☰", "Command Center", () -> fire(
                originalCommandCenter, window, AppPanelId.DASHBOARD,
                journal)));
        primary.getStyleClass().add("alternate-navigation-primary");

        VBox accountingButtons = group(
            navButton("▥", "Chart of Accounts", () -> openPanel(window,
                AppPanelId.CHART_OF_ACCOUNTS, journal)),
            navButton("✎", "Journal", () -> showJournal(window, workspace,
                journal)),
            navButton("≣", "Ledger", () -> openPanel(window,
                AppPanelId.LEDGER_REGISTER, journal)));
        TitledPane accounting = section("▤", "Accounting",
            accountingButtons, true);

        VBox utilityButtons = group(
            navButton("▣", "Open Database", () -> {
                hideJournal(journal);
                window.openDatabaseSelector();
            }),
            navButton("▦", "Open Company", () -> {
                hideJournal(journal);
                window.openCompanySelector();
            }),
            navButton("⌘", "Import & Tools", () -> openPanel(window,
                AppPanelId.IMPORT_EXPORT, journal)),
            navButton("⚒", "Developer Tools", () -> openPanel(window,
                AppPanelId.DEVELOPER_TOOLS, journal)));
        TitledPane utilities = section("⋮", "Utilities",
            utilityButtons, true);

        VBox navigation = new VBox(10, navigationTitle, primary,
            accounting, utilities);
        navigation.setPadding(new Insets(12));
        navigation.setMinWidth(COLLAPSED_MIN_WIDTH);
        navigation.setPrefWidth(280);
        navigation.getStyleClass().add("alternate-left-navigation");
        navigation.getProperties().put("alternate.nav.title.node",
            navigationTitle);

        splitPane.getItems().set(0, navigation);
        splitPane.setDividerPositions(0.25);
        SplitPane.setResizableWithParent(navigation, true);

        navigation.widthProperty().addListener((obs, oldWidth, newWidth) ->
            applyCollapsedState(navigation,
                newWidth.doubleValue() < COLLAPSE_THRESHOLD));
        Platform.runLater(() -> applyCollapsedState(navigation,
            navigation.getWidth() > 0 &&
                navigation.getWidth() < COLLAPSE_THRESHOLD));
    }

    private static VBox group(Node... children)
    {
        VBox group = new VBox(6, children);
        group.setMinWidth(0);
        return group;
    }

    private static JournalPanelFX createJournalWorkspace(
        MainWindowAlternate window, StackPane workspace)
    {
        if (workspace == null)
        {
            return null;
        }
        JournalPanelFX[] holder = new JournalPanelFX[1];
        holder[0] = new JournalPanelFX(transactionId -> {
            LedgerNavigationContext.requestTransaction(transactionId);
            hideJournal(holder[0]);
            window.openPanel(AppPanelId.LEDGER_REGISTER);
        });
        holder[0].setVisible(false);
        holder[0].setManaged(false);
        StackPane.setMargin(holder[0], new Insets(8));
        workspace.getChildren().add(holder[0]);
        return holder[0];
    }

    private static void showJournal(MainWindowAlternate window,
        StackPane workspace, JournalPanelFX journal)
    {
        if (workspace == null || journal == null)
        {
            return;
        }
        for (Node child : workspace.getChildren())
        {
            boolean selected = child == journal;
            child.setVisible(selected);
            child.setManaged(selected);
        }
        journal.refreshData();
        setShellTitle(window, "Journal");
    }

    private static void openPanel(MainWindowAlternate window, AppPanelId panel,
        JournalPanelFX journal)
    {
        hideJournal(journal);
        window.openPanel(panel);
    }

    private static void hideJournal(JournalPanelFX journal)
    {
        if (journal != null)
        {
            journal.setVisible(false);
            journal.setManaged(false);
        }
    }

    private static void setShellTitle(MainWindowAlternate window, String title)
    {
        Label label = findStyledLabel(window.getTop(),
            "alternate-shell-title");
        if (label != null)
        {
            label.setText(title);
        }
    }

    private static Label findStyledLabel(Node node, String styleClass)
    {
        if (node instanceof Label label &&
            label.getStyleClass().contains(styleClass))
        {
            return label;
        }
        if (node instanceof javafx.scene.Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                Label result = findStyledLabel(child, styleClass);
                if (result != null)
                {
                    return result;
                }
            }
        }
        return null;
    }

    private static void fire(Button original, MainWindowAlternate window,
        AppPanelId fallback, JournalPanelFX journal)
    {
        hideJournal(journal);
        if (original != null)
        {
            original.fire();
        }
        else
        {
            window.openPanel(fallback);
        }
    }

    private static Button navButton(String icon, String title, Runnable action)
    {
        Button button = new Button(icon + "  " + title);
        button.setMinWidth(0);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setTooltip(new Tooltip(title));
        button.setAccessibleText(title);
        button.getProperties().put(ICON_PROPERTY, icon);
        button.getProperties().put(TITLE_PROPERTY, title);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static TitledPane section(String icon, String title, Node content,
        boolean expanded)
    {
        TitledPane pane = new TitledPane(title, content);
        pane.setMinWidth(0);
        pane.setExpanded(expanded);
        pane.setAnimated(false);
        pane.setTooltip(new Tooltip(title));
        pane.getProperties().put(ICON_PROPERTY, icon);
        pane.getProperties().put(TITLE_PROPERTY, title);
        return pane;
    }

    static void applyCollapsedState(VBox navigation, boolean collapsed)
    {
        Object titleNode = navigation.getProperties().get(
            "alternate.nav.title.node");
        if (titleNode instanceof Label title)
        {
            title.setVisible(!collapsed);
            title.setManaged(!collapsed);
        }
        navigation.setPadding(collapsed ? new Insets(8, 4, 8, 4) :
            new Insets(12));

        for (Button button : collectButtons(navigation))
        {
            Object iconValue = button.getProperties().get(ICON_PROPERTY);
            Object titleValue = button.getProperties().get(TITLE_PROPERTY);
            if (iconValue == null || titleValue == null)
            {
                continue;
            }
            String icon = iconValue.toString();
            String title = titleValue.toString();
            button.setText(collapsed ? icon : icon + "  " + title);
            button.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            button.setTooltip(new Tooltip(title));
        }

        for (TitledPane pane : collectSections(navigation))
        {
            Object iconValue = pane.getProperties().get(ICON_PROPERTY);
            Object titleValue = pane.getProperties().get(TITLE_PROPERTY);
            if (iconValue == null || titleValue == null)
            {
                continue;
            }
            String icon = iconValue.toString();
            String title = titleValue.toString();
            pane.setText(collapsed ? icon : title);
            pane.setTooltip(new Tooltip(title));
        }
    }

    private static List<Button> collectButtons(Node root)
    {
        List<Button> buttons = new ArrayList<>();
        collect(root, Button.class, buttons);
        return buttons;
    }

    private static List<TitledPane> collectSections(Node root)
    {
        List<TitledPane> sections = new ArrayList<>();
        collect(root, TitledPane.class, sections);
        return sections;
    }

    private static <T extends Node> void collect(Node node, Class<T> type,
        List<T> result)
    {
        if (type.isInstance(node))
        {
            result.add(type.cast(node));
        }
        if (node instanceof javafx.scene.Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                collect(child, type, result);
            }
        }
        if (node instanceof TitledPane pane && pane.getContent() != null &&
            !pane.getChildrenUnmodifiable().contains(pane.getContent()))
        {
            collect(pane.getContent(), type, result);
        }
    }

    private static Button findButton(Node root, String title)
    {
        return collectButtons(root).stream()
            .filter(button -> button.getText() != null &&
                button.getText().contains(title))
            .findFirst()
            .orElse(null);
    }
}
