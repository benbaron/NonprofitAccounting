package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;

public class SkeletonJournalPanel extends StackPane {
    public SkeletonJournalPanel() {
        Label label = new Label("Journal Panel Content Area");
        setPadding(new Insets(20));
        getChildren().add(label);
    }
}
