package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.ui.AppPanel;

/**
 * Asset register placeholder panel.
 */
public class AssetsRegisterPanel implements AppPanel
{
	private final BorderPane root = new BorderPane();

	/**
	 * Creates the asset register panel.
	 */
	public AssetsRegisterPanel()
	{
		root.setPadding(new Insets(8));
		Label title = new Label("Asset Register");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Asset");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, save);

		root.setTop(new VBox(6, title, actions, new Separator()));
		root.setCenter(new Label("TODO: Asset register (inputs + asset details)."));
	}

	@Override
	public String title()
	{
		return "Asset Register";
	}

	@Override
	public Node root()
	{
		return root;
	}
}
