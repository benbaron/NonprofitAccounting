package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Asset register placeholder panel.
 */
public class AssetsRegisterPanel extends BorderPane
{

	/**
	 * Creates the asset register panel.
	 */
	public AssetsRegisterPanel()
	{
		setPadding(new Insets(8));
		Label title = new Label("Asset Register");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Asset");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, save);

		setTop(new VBox(6, title, actions, new Separator()));
		setCenter(new Label("TODO: Asset register (inputs + asset details)."));
	}

}
