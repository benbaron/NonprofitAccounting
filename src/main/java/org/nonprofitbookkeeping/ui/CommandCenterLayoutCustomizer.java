package org.nonprofitbookkeeping.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Applies the approved scrolling, collapsible layout to Command Center. */
final class CommandCenterLayoutCustomizer
{
    private static final String CUSTOMIZED_KEY =
        "alternate.command.center.customized";

    private CommandCenterLayoutCustomizer()
    {
    }

    static void apply(MainWindowAlternate window)
    {
        VBox commandCenter = findCommandCenter(window);
        if (commandCenter == null || Boolean.TRUE.equals(
            commandCenter.getProperties().get(CUSTOMIZED_KEY)))
        {
            return;
        }

        List<VBox> groups = commandCenter.getChildren().stream()
            .filter(VBox.class::isInstance)
            .map(VBox.class::cast)
            .toList();
        if (groups.isEmpty())
        {
            return;
        }

        VBox sections = new VBox(8);
        for (VBox group : groups)
        {
            String title = group.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .map(Label::getText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElse("Commands");

            VBox actions = new VBox(6);
            group.getChildren().stream()
                .filter(Button.class::isInstance)
                .forEach(actions.getChildren()::add);
            TitledPane section = new TitledPane(title, actions);
            section.setAnimated(false);
            section.setExpanded(!"Database".equals(title) &&
                !"Company".equals(title));
            sections.getChildren().add(section);
        }

        ScrollPane scroll = new ScrollPane(sections);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        List<Node> heading = new ArrayList<>();
        commandCenter.getChildren().stream()
            .filter(node -> node instanceof Label || node instanceof Separator)
            .limit(2)
            .forEach(heading::add);
        commandCenter.getChildren().setAll(heading);
        commandCenter.getChildren().add(scroll);
        commandCenter.getProperties().put(CUSTOMIZED_KEY, Boolean.TRUE);
    }

    private static VBox findCommandCenter(Node node)
    {
        if (node instanceof VBox box && box.getChildren().stream()
            .filter(Label.class::isInstance)
            .map(Label.class::cast)
            .anyMatch(label -> "Command Center".equals(label.getText())))
        {
            return box;
        }
        if (node instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                VBox found = findCommandCenter(child);
                if (found != null)
                {
                    return found;
                }
            }
        }
        if (node instanceof TitledPane pane && pane.getContent() != null)
        {
            return findCommandCenter(pane.getContent());
        }
        if (node instanceof ScrollPane pane && pane.getContent() != null)
        {
            return findCommandCenter(pane.getContent());
        }
        return null;
    }
}
