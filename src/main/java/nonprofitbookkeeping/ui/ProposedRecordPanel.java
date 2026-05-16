package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.ui.AppPanel;

/**
 * Generic panel used to document and host proposed record-management surfaces.
 */
public class ProposedRecordPanel implements AppPanel
{
    private final String title;
    private final BorderPane root = new BorderPane();

    /**
     * Creates a proposed panel placeholder.
     *
     * @param title panel title
     * @param description panel description/todo text
     */
    public ProposedRecordPanel(String title, String description)
    {
        this.title = title;
        root.setPadding(new Insets(16));
        Label header = new Label(title);
        header.getStyleClass().add("journal-entry-heading");
        root.setTop(new VBox(6, header, new Separator()));
        root.setCenter(new Label(description));
    }

    @Override
    public String title()
    {
        return title;
    }

    @Override
    public Node root()
    {
        return root;
    }
}
