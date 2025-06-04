package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;

public class SkeletonReportsPanel extends StackPane {
    public SkeletonReportsPanel() {
        Label label = new Label("Reports Panel Content Area");
        setPadding(new Insets(20));
        getChildren().add(label);
    }
}
