package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;

public class SkeletonDashboardPanel extends StackPane {
    public SkeletonDashboardPanel() {
        Label label = new Label("Dashboard Panel Content Area");
        setPadding(new Insets(20));
        getChildren().add(label);
    }
}
