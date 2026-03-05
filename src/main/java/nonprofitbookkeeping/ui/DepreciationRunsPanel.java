package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Depreciation run placeholder panel.
 */
public class DepreciationRunsPanel extends BorderPane
{

	/**
	 * Creates the depreciation runs panel.
	 */
	public DepreciationRunsPanel()
	{
		setPadding(new Insets(8));
		Label title = new Label("Depreciation Runs");
		title.getStyleClass().add("panel-title");

		Button run = new Button("Run Depreciation");
		Button preview = new Button("Preview Journal");
		HBox actions = new HBox(8, run, preview);

		setTop(new VBox(6, title, actions, new Separator()));
		setCenter(new Label(
			"TODO: Depreciation run wizard + posting preview (outputs + automation)."));
	}

}
