package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;

public class SkeletonCoaPanel extends StackPane {
    public SkeletonCoaPanel() {
        Label label = new Label("Chart of Accounts Panel Content Area");
        setPadding(new Insets(20));
        getChildren().add(label);
    }
}
