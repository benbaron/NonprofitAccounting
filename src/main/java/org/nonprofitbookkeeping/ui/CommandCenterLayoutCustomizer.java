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

            List<Button> buttons = group.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .toList();
            group.getChildren().removeAll(buttons);

            if (AlternateUiCommandCatalog.DATABASE_COMPANY.equals(title))
            {
                List<Button> databaseButtons = buttons.stream()
                    .filter(button -> !isCompanyCommand(button))
                    .toList();
                List<Button> companyButtons = buttons.stream()
                    .filter(CommandCenterLayoutCustomizer::isCompanyCommand)
                    .toList();
                sections.getChildren().add(section("Database",
                    databaseButtons, false));
                sections.getChildren().add(section("Company",
                    companyButtons, false));
            }
            else
            {
                sections.getChildren().add(section(title, buttons, true));
            }
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

    private static TitledPane section(String title, List<Button> buttons,
        boolean expanded)
    {
        VBox actions = new VBox(6);
        actions.getChildren().addAll(buttons);
        TitledPane section = new TitledPane(title, actions);
        section.setAnimated(false);
        section.setExpanded(expanded);
        return section;
    }

    private static boolean isCompanyCommand(Button button)
    {
        String text = button.getText();
        return text != null && text.toLowerCase().contains("company");
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
