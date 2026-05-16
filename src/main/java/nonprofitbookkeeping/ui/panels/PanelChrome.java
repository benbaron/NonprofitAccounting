package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Shared heading/layout chrome to align panels with Journal Entry styling. */
public final class PanelChrome
{
    public static final Insets PANEL_PADDING = new Insets(16);

    private PanelChrome()
    {
    }

    public static Label heading(String text)
    {
        Label label = new Label(text);
        label.getStyleClass().add("journal-entry-heading");
        return label;
    }

    public static VBox topSection(String title, javafx.scene.Node... children)
    {
        VBox top = new VBox(10);
        top.getChildren().add(heading(title));
        top.getChildren().addAll(children);
        return top;
    }
}
