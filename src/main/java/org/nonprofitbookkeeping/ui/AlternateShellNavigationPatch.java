package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/** Applies focused navigation corrections after the alternate rail is built. */
final class AlternateShellNavigationPatch
{
    private static final String INSTALLED_KEY =
        "alternate.shell.navigation.patch.installed";

    private AlternateShellNavigationPatch()
    {
    }

    static void apply(MainWindowAlternate window)
    {
        VBox navigation = findStyledVBox(window, "alternate-left-navigation");
        if (navigation == null || Boolean.TRUE.equals(
            navigation.getProperties().get(INSTALLED_KEY)))
        {
            return;
        }

        VBox primary = findStyledVBox(navigation,
            "alternate-navigation-primary");
        if (primary != null && findButton(primary, "Companies") == null)
        {
            Button companies = new Button("▦  Companies");
            companies.setMinWidth(0);
            companies.setMaxWidth(Double.MAX_VALUE);
            companies.setAlignment(Pos.CENTER_LEFT);
            companies.setTooltip(new Tooltip("Companies"));
            companies.setAccessibleText("Companies");
            companies.getProperties().put("alternate.nav.icon", "▦");
            companies.getProperties().put("alternate.nav.title", "Companies");
            companies.setOnAction(event ->
                window.openPanel(AppPanelId.COMPANY_ADMIN));
            int dashboardIndex = indexOfButton(primary, "Dashboard");
            primary.getChildren().add(Math.min(dashboardIndex + 1,
                primary.getChildren().size()), companies);
        }

        Button commandCenter = findButton(navigation, "Command Center");
        if (commandCenter != null)
        {
            EventHandler<ActionEvent> original = commandCenter.getOnAction();
            commandCenter.setOnAction(event -> {
                if (original != null)
                {
                    original.handle(event);
                }
                Platform.runLater(() ->
                    CommandCenterLayoutCustomizer.apply(window));
            });
        }

        navigation.getProperties().put(INSTALLED_KEY, Boolean.TRUE);
    }

    private static int indexOfButton(VBox box, String text)
    {
        for (int index = 0; index < box.getChildren().size(); index++)
        {
            Node child = box.getChildren().get(index);
            if (child instanceof Button button && button.getText() != null &&
                button.getText().contains(text))
            {
                return index;
            }
        }
        return box.getChildren().size() - 1;
    }

    private static Button findButton(Node root, String text)
    {
        if (root instanceof Button button && button.getText() != null &&
            button.getText().contains(text))
        {
            return button;
        }
        if (root instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                Button found = findButton(child, text);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }

    private static VBox findStyledVBox(Node root, String styleClass)
    {
        if (root instanceof VBox box &&
            box.getStyleClass().contains(styleClass))
        {
            return box;
        }
        if (root instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                VBox found = findStyledVBox(child, styleClass);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }
}
